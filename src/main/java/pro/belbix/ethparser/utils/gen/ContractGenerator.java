package pro.belbix.ethparser.utils.gen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
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
import org.web3j.abi.datatypes.Function;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import org.web3j.tx.Contract;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.Strings;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.service.EtherscanService;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.contracts.ContractUtils;

@Service
@Log4j2
public class ContractGenerator {

    final static Credentials STUB_CREDENTIALS =
        Credentials.create("8da4ef21b864d2cc526dbdb2a120bd2874c36c9d0a1fb7f8c63d7f7a8b41de8f");
    private final EtherscanService etherscanService = new EtherscanService();
    private final Map<String, String> contractToWrapper = new HashMap<>();

    private final AppProperties appProperties;
    private final ContractLoader contractLoader;
    private final Web3Service web3Service;

    @Value("${contract-generator.contract:}")
    private String contract;
    @Value("${contract-generator.category:}")
    private String category;
    @Value("${contract-generator.destinationRootPackage:pro.belbix.ethparser.web3.abi.generated}")
    private String destinationRootPackage;
    @Value("${contract-generator.destinationDir:./../../src/main/java}")
    private String destinationDir;

    private Instant lastCall = Instant.now();

    public ContractGenerator(AppProperties appProperties,
        ContractLoader contractLoader, Web3Service web3Service) {
        this.appProperties = appProperties;
        this.contractLoader = contractLoader;
        this.web3Service = web3Service;
    }

    public void start() {
        log.info("Start Contract Generator {} {} {} {}",
            contract, category, destinationRootPackage, destinationDir);
        if (contract != null && !contract.isBlank()) {
            generateFromAddress(contract, category);
        }
        contractLoader.load();
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

    String generateFromAddress(String address, String subPackage) {
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
                return null;
            }
            EtherscanService.SourceCodeResult result = sourceCode.getResult().get(0);

            List<AbiDefinition> abiDefinitions = toContractDefinition(result.getAbi());

            String className = generate(
                abiDefinitions,
                new File(destinationDir),
                result.getContractName(),
                destinationRootPackage + "." + subPackage
            );
            contractToWrapper.put(address, className);
            lastCall = Instant.now();
            if (isProxy(abiDefinitions)) {
                log.info("Detected proxy contract, parse implementation");
                String implAddress = readProxy(address, className);
                if (implAddress != null) {
                    String implClassName = generateFromAddress(implAddress, subPackage);
                    contractToWrapper.put(address, implClassName);
                } else {
                    log.error("Can't fetch implementation for proxy {}", address);
                }
            }
            return className;
        } catch (Exception e) {
            log.error("Error while generate contract for address {}", address, e);
            return null;
        }
    }

    private boolean isProxy(List<AbiDefinition> abiDefinitions) {
        for (AbiDefinition abiDefinition : abiDefinitions) {
            if ("implementation".equals(abiDefinition.getName())) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private String readProxy(String proxyAddress, String className) {
        try {
            Class cls = new URLClassLoader(
                new URL[]{new File(destinationDir).toURI().toURL()}
            ).loadClass(className);
            for (Method method : cls.getDeclaredMethods()) {
                if (!"call_implementation".equals(method.getName())) {
                    continue;
                }
                Method load = cls.getDeclaredMethod("load",
                    String.class, Web3j.class, Credentials.class, ContractGasProvider.class);
                Object proxyInstance = load
                    .invoke(null, proxyAddress, web3Service.getWeb3(), STUB_CREDENTIALS, null);
                RemoteFunctionCall<String> call =
                    (RemoteFunctionCall<String>) method.invoke(proxyInstance);
                String proxyImplementationAddress = call.send();
                return proxyImplementationAddress;
            }
            return null;
        } catch (Exception e) {
            log.error("Error load generated class {}", className, e);
            return null;
        }
    }

    private String generate(
        List<AbiDefinition> functionDefinitions,
        File destinationDir,
        String contractName,
        String basePackageName
    ) {
        String className = Strings.capitaliseFirstLetter(contractName)
            + "_" + abiHashCodePostfix(functionDefinitions);
        log.info("Generating " + basePackageName + "." + className + " ... ");

        try {
            new SolidityFunctionWrapper(
                true,
                false,
                true,
                Address.DEFAULT_LENGTH / Byte.SIZE)
                .generateJavaFiles(
                    Contract.class,
                    className,
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

    static String abiHashCodePostfix(List<AbiDefinition> functionDefinitions) {
        StringBuilder sb = new StringBuilder();
        for (AbiDefinition abiDefinition : functionDefinitions) {
            sb.append(abiDefinition.hashCode());
        }
        return (sb.toString().hashCode() + "").replace("-", "0");
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

    public static Map<String, Function> createFuncMap(Object... values) {
        if (values.length == 0 || values.length % 2 != 0) {
            throw new IllegalStateException("Wrong strings: " + Arrays.toString(values));
        }
        Map<String, Function> result = new HashMap<>();
        for (int i = 0; i < values.length / 2; i++) {
            result.put((String) values[i], (Function) values[i + 1]);
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
