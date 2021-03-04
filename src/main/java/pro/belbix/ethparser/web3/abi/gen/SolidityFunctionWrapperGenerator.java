package pro.belbix.ethparser.web3.abi.gen;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.web3j.codegen.SolidityFunctionWrapper;

import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import org.web3j.tx.Contract;
import org.web3j.utils.Files;
import org.web3j.utils.Strings;

import static org.web3j.codegen.Console.exitError;

public class SolidityFunctionWrapperGenerator extends FunctionWrapperGenerator {
    public static final String COMMAND_SOLIDITY = "solidity";
    public static final String COMMAND_GENERATE = "generate";
    public static final String COMMAND_PREFIX = COMMAND_SOLIDITY + " " + COMMAND_GENERATE;

    /*
     * Usage: solidity generate [-hV] [-jt] [-st] -a=<abiFile> [-b=<binFile>]
     * -o=<destinationFileDir> -p=<packageName>
     * -h, --help                 Show this help message and exit.
     * -V, --version              Print version information and exit.
     * -a, --abiFile=<abiFile>    abi file with contract definition.
     * -b, --binFile=<binFile>    bin file with contract compiled code in order to
     * generate deploy methods.
     * -o, --outputDir=<destinationFileDir>
     * destination base directory.
     * -p, --package=<packageName>
     * base package name.
     * -jt, --javaTypes       use native java types.
     * Default: true
     * -st, --solidityTypes   use solidity types.
     */

    private final File binFile;
    private final File abiFile;

    private final String contractName;

    private final int addressLength;

    private final boolean generateSendTxForCalls;

    public SolidityFunctionWrapperGenerator(
        File binFile,
        File abiFile,
        File destinationDir,
        String contractName,
        String basePackageName,
        boolean useJavaNativeTypes,
        boolean useJavaPrimitiveTypes,
        int addressLength) {

        this(
            binFile,
            abiFile,
            destinationDir,
            contractName,
            basePackageName,
            useJavaNativeTypes,
            useJavaPrimitiveTypes,
            false,
            Contract.class,
            addressLength);
    }

    protected SolidityFunctionWrapperGenerator(
        File binFile,
        File abiFile,
        File destinationDir,
        String contractName,
        String basePackageName,
        boolean useJavaNativeTypes,
        boolean useJavaPrimitiveTypes,
        boolean generateSendTxForCalls,
        Class<? extends Contract> contractClass,
        int addressLength) {

        super(
            contractClass,
            destinationDir,
            basePackageName,
            useJavaNativeTypes,
            useJavaPrimitiveTypes);

        this.binFile = binFile;
        this.abiFile = abiFile;
        this.contractName = contractName;
        this.addressLength = addressLength;
        this.generateSendTxForCalls = generateSendTxForCalls;
    }

    protected List<AbiDefinition> loadContractDefinition(File absFile) throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        AbiDefinition[] abiDefinition = objectMapper.readValue(absFile, AbiDefinition[].class);
        return Arrays.asList(abiDefinition);
    }

    public final void generate() throws IOException, ClassNotFoundException {
        String binary = Contract.BIN_NOT_PROVIDED;
        if (binFile != null) {
            byte[] bytes = Files.readBytes(binFile);
            binary = new String(bytes);
        }
        List<AbiDefinition> functionDefinitions = loadContractDefinition(abiFile);

        if (!functionDefinitions.isEmpty()) {

            String className = Strings.capitaliseFirstLetter(contractName);
            System.out.print("Generating " + basePackageName + "." + className + " ... ");

            new SolidityFunctionWrapper(
                useJavaNativeTypes,
                useJavaPrimitiveTypes,
                generateSendTxForCalls,
                addressLength)
                .generateJavaFiles(
                    contractClass,
                    contractName,
                    binary,
                    functionDefinitions,
                    destinationDirLocation.toString(),
                    basePackageName,
                    null);

            System.out.println("File written to " + destinationDirLocation.toString() + "\n");
        } else {
            System.out.println("Ignoring empty ABI file: " + abiFile.getName() + ".abi" + "\n");
        }
    }
}
