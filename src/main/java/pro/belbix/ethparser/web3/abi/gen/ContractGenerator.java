package pro.belbix.ethparser.web3.abi.gen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import org.web3j.tx.Contract;
import org.web3j.utils.Strings;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.service.EtherscanService;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.contracts.ContractUtils;

@Service
@Log4j2
public class ContractGenerator {

    private final EtherscanService etherscanService = new EtherscanService();
    private final Map<String, String> contractToWrapper = new HashMap<>();

    private final AppProperties appProperties;
    private final ContractLoader contractLoader;

    @Value("${contract-generator.contract:}")
    private String contract;
    @Value("${contract-generator.category:}")
    private String category;
    @Value("${contract-generator.destinationRootPackage:pro.belbix.ethparser.web3.abi.contracts}")
    private String destinationRootPackage;
    @Value("${contract-generator.destinationDir:./../../src/main/java}")
    private String destinationDir;
    @Value("${contract-generator.destinationDir:./../../src/main/resources}")
    private String resourcesDir;

    private Instant lastCall = Instant.now();

    public ContractGenerator(AppProperties appProperties,
        ContractLoader contractLoader) {
        this.appProperties = appProperties;
        this.contractLoader = contractLoader;
    }

    public void start() {
        log.info("Start Contract Generator {} {} {} {}",
            contract, category, destinationRootPackage, destinationDir);
        contractLoader.load();
        if (contract != null && !contract.isBlank()) {
            generateFromAddress(contract, category);
        }

        ContractUtils.getAllVaultAddresses()
            .forEach(address -> generateFromAddress(address, "harvest"));

        ContractUtils.getAllPoolAddresses()
            .forEach(address -> generateFromAddress(address, "harvest"));

        ContractUtils.getAllUniPairs().stream()
            .map(u -> u.getContract().getAddress())
            .forEach(address -> generateFromAddress(address, "uniswap"));

        ContractUtils.getAllTokens().stream()
            .map(u -> u.getContract().getAddress())
            .forEach(address -> generateFromAddress(address, "erc20"));
        generateMappingClass();
    }

    void generateFromAddress(String address, String subPackage) {
        try {

            // avoid etherscan throttling
            long diff = Duration.between(lastCall, Instant.now()).toMillis();
            if (diff < 200) {
                Thread.sleep(200 - diff);
            }
            EtherscanService.ResponseSourceCode sourceCode =
                etherscanService.contractSourceCode(address, appProperties.getEtherscanApiKey());
            if (sourceCode == null || sourceCode.getResult() == null || sourceCode.getResult()
                .isEmpty()) {
                log.error("Empty response for {}", address);
                return;
            }
            EtherscanService.SourceCodeResult result = sourceCode.getResult().get(0);

            String className = generate(
                toContractDefinition(result.getAbi()),
                new File(destinationDir),
                result.getContractName(),
                destinationRootPackage + "." + subPackage
            );
            contractToWrapper.put(address, className);
            lastCall = Instant.now();
        } catch (Exception e) {
            log.error("Error while generate contract for address {}", address, e);
        }
    }

    private String generate(
        List<AbiDefinition> functionDefinitions,
        File destinationDir,
        String contractName,
        String basePackageName
    ) {
        String className = Strings.capitaliseFirstLetter(contractName);
        log.info("Generating " + basePackageName + "." + className + " ... ");

        try {
            new SolidityFunctionWrapper(
                true,
                false,
                true,
                Address.DEFAULT_LENGTH / Byte.SIZE)
                .generateJavaFiles(
                    Contract.class,
                    contractName,
                    Contract.BIN_NOT_PROVIDED,
                    functionDefinitions,
                    destinationDir.toString(),
                    basePackageName,
                    null);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return basePackageName + "." + className;
    }

    private List<AbiDefinition> toContractDefinition(String abi) {
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        AbiDefinition[] abiDefinition;
        try {
            abiDefinition = objectMapper.readValue(abi, AbiDefinition[].class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Arrays.asList(abiDefinition);
    }

    private void generateMappingClass() {
        try {
            List<Object> values = new ArrayList<>();
            values.add(ContractGenerator.class);
            StringBuilder sbFormat = new StringBuilder();
            contractToWrapper.forEach((contract, name) -> {
                values.add(contract);
                values.add(ClassName.bestGuess(name));
                sbFormat.append("$S, $T.class").append(",\n");
            });
            sbFormat.setLength(sbFormat.length() - 2);
            sbFormat.append("\n");

            TypeSpec.Builder classBuilder = TypeSpec.classBuilder("WrapperMapper")
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("<p>Auto generated code.\n"
                    + "<p><strong>Do not modify!</strong>\n")
                .addField(FieldSpec.builder(ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ParameterizedTypeName.get(
                        ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(Object.class)
                    )
                ), "contractToWrapper")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                    .initializer("$T.createMap(\n"
                        + sbFormat.toString()
                        + ")", values.toArray(new Object[0]))
                    .build());

            JavaFile javaFile = JavaFile.builder(destinationRootPackage, classBuilder.build())
                .indent("    ")
                .skipJavaLangImports(true)
                .build();

            javaFile.writeTo(new File(destinationDir));
        } catch (Exception e) {
            log.error("Error generate map class", e);
        }
    }

    public static Map<String, Class<?>> createMap(Object... values) {
        if (values.length == 0 || values.length % 2 != 0) {
            throw new IllegalStateException("Wrong strings: " + Arrays.toString(values));
        }
        Map<String, Class<?>> result = new HashMap<>();
        for (int i = 0; i < values.length / 2; i++) {
            result.put((String) values[i], (Class<?>) values[i + 1]);
        }
        return result;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDestinationRootPackage(String destinationRootPackage) {
        this.destinationRootPackage = destinationRootPackage;
    }

    public void setDestinationDir(String destinationDir) {
        this.destinationDir = destinationDir;
    }
}
