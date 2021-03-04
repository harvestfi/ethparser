package pro.belbix.ethparser.web3.abi.gen;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import org.web3j.tx.Contract;
import org.web3j.utils.Strings;

@Log4j2
public class ContractGenerator {

    public static void main(String[] args) {
        findAllContractPaths().forEach(ContractGenerator::generateFromAbi);
    }

    private static void generateFromAbi(File abiFile) {
        try {
            String path = abiFile.getPath();
            String folder = path.substring(path.indexOf("contracts"))
                .replace(abiFile.getName(), "");
            folder = folder.substring(0, folder.length() - 1);
            generate(
                abiFile,
                new File("./src/main/java"),
                "Wrapped" + Objects.requireNonNull(abiFile.getName())
                    .replace(".abi", ""),
                "pro.belbix.ethparser.web3.abi"
                    + "." + folder.replace("\\", ".")
            );
        } catch (Exception e) {
            log.error("Error generate", e);
        }
    }

    private static List<File> findAllContractPaths() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(
                MethodHandles.lookup().getClass().getClassLoader());
            Resource[] resources = resolver.getResources("classpath:contracts/**/*.abi");
            List<File> files = new ArrayList<>();
            for (Resource resource : resources) {
                log.info(resource.getFile().getPath());
                files.add(resource.getFile());
            }
            return files;
        } catch (Exception e) {
            log.error("Error load paths", e);
        }
        return List.of();
    }

    private static void generate(
        File abiFile,
        File destinationDir,
        String contractName,
        String basePackageName
    ) throws IOException, ClassNotFoundException {
        String binary = Contract.BIN_NOT_PROVIDED;
        List<AbiDefinition> functionDefinitions = loadContractDefinition(abiFile);

        if (!functionDefinitions.isEmpty()) {

            String className = Strings.capitaliseFirstLetter(contractName);
            System.out.print("Generating " + basePackageName + "." + className + " ... ");

            new SolidityFunctionWrapper(
                true,
                false,
                true,
                Address.DEFAULT_LENGTH / Byte.SIZE)
                .generateJavaFiles(
                    Contract.class,
                    contractName,
                    binary,
                    functionDefinitions,
                    destinationDir.toString(),
                    basePackageName,
                    null);

            System.out.println("File written to " + destinationDir.toString() + "\n");
        } else {
            System.out.println("Ignoring empty ABI file: " + abiFile.getName() + ".abi" + "\n");
        }
    }

    private static List<AbiDefinition> loadContractDefinition(File absFile) throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        AbiDefinition[] abiDefinition = objectMapper.readValue(absFile, AbiDefinition[].class);
        return Arrays.asList(abiDefinition);
    }
}
