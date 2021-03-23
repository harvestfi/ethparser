package pro.belbix.ethparser.codegen;


import static pro.belbix.ethparser.web3.MethodDecoder.extractLogIndexedValues;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.codegen.abi.StaticAbiMap;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.service.EtherscanService;
import pro.belbix.ethparser.web3.MethodDecoder;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;

@Log4j2
@Service
public class SimpleContractGenerator {

  private static final String UPGRADED_EVENT = "0xbc7cd75a20ee27fd9adebab32041f755214dbc6bffa90cc0225b39da2e5c2d3b";
  private static final String IMPLEMENTATION = "implementation";
  private static final String TYPE_FUNCTION = "function";
  private static final String TYPE_EVENT = "event";
  private static final String TUPLE = "tuple";
  private static final String PURE = "pure";
  private static final String VIEW = "view";

  private final EtherscanService etherscanService = new EtherscanService();
  private final AppProperties appProperties;
  private final FunctionsUtils functionsUtils;
  private final Web3Service web3Service;

  private final Map<String, TreeMap<Integer, GeneratedContract>> contracts = new HashMap<>();

  public SimpleContractGenerator(AppProperties appProperties, FunctionsUtils functionsUtils,
      Web3Service web3Service) {
    this.appProperties = appProperties;
    this.functionsUtils = functionsUtils;
    this.web3Service = web3Service;
  }

  public GeneratedContract getContract(String address, int block) {
    GeneratedContract generatedContract = findInCache(address, block);
    if (generatedContract != null) {
      return generatedContract;
    }
    return generateContract(address, block, false)
        .map(newContract -> {
          log.info("Generated {} {}", newContract.getName(), newContract.getAddress());
          var implementations
              = contracts.computeIfAbsent(address, k -> new TreeMap<>());

          // for reducing memory usage don't save the same proxy impl
          if (!implementations.isEmpty()) {
            var existContract = implementations.floorEntry(block).getValue();
            if (existContract.isProxy()
                && equalContracts(newContract, existContract)) {
              return newContract;
            }
          }

          implementations.put(block, newContract);
          return newContract;
        }).orElse(null);
  }

  private boolean equalContracts(GeneratedContract newContract, GeneratedContract existContract) {
    return newContract.getAddress().equalsIgnoreCase(existContract.getAddress());
  }

  private Optional<GeneratedContract> generateContract(String address, long block,
      boolean isProxy) {
    EtherscanService.ResponseSourceCode sourceCode =
        etherscanService.contractSourceCode(address, appProperties.getEtherscanApiKey());
    if (sourceCode == null || sourceCode.getResult() == null || sourceCode.getResult()
        .isEmpty()) {
      log.error("Empty etherscan response for {}", address);
      return Optional.empty();
    }
    EtherscanService.SourceCodeResult result = sourceCode.getResult().get(0);
    String abi = resolveAbi(address, result.getAbi());
    List<AbiDefinition> abis = ContractGenerator.abiToDefinition(abi);
    GeneratedContract contract = new GeneratedContract(
        result.getContractName(),
        address,
        abiToEvents(abis),
        abiToFunctions(abis)
    );
    if (!isProxy && isProxy(abis)) {
      String proxyAddress = readProxyAddress(address, block, contract);
      if (proxyAddress == null) {
        log.error("Can't reach proxy impl adr for {} at {}", address, block);
        return Optional.empty();
      }
      return generateContract(proxyAddress, block, true);
    }

    contract.setProxy(isProxy);

    return Optional.of(contract);
  }

  private String resolveAbi(String address, String abi) {
    if (StaticAbiMap.MAP.containsKey(address.toLowerCase())) {
      return StaticAbiMap.MAP.get(address.toLowerCase());
    }
    return abi;
  }

  private String readProxyAddress(String address, long block, GeneratedContract contract) {
    // open zeppelin proxy doesn't have public call_implementation
    // some contracts have event but didn't call it
    String proxyImpl =
        findLastProxyUpgrade(address, (int) block, contract.getEvent(UPGRADED_EVENT));
    if (proxyImpl != null) {
      return proxyImpl;
    }
    // EIP-897 DelegateProxy concept
    return proxyAddressFromFunc(address, block);
  }

  private String proxyAddressFromFunc(String address, long block) {
    return functionsUtils.callAddressByName(IMPLEMENTATION, address, block)
        .orElse(null);
  }

  private String findLastProxyUpgrade(String address, Integer block, Event event) {
    List<LogResult> logResults = web3Service.fetchContractLogs(
        List.of(address),
        null,
        block,
        UPGRADED_EVENT);
    if (logResults == null || logResults.isEmpty()) {
      return null;
    }
    Log ethLog = (Log) logResults.get(logResults.size() - 1).get();
    List<Type> types = extractLogIndexedValues(
        ethLog.getTopics(), ethLog.getData(), event.getParameters());
    if (types == null || types.isEmpty()) {
      log.error("Empty types for {}", ethLog);
      return null;
    }
    return (String) types.get(0).getValue();
  }

  private GeneratedContract findInCache(String address, int block) {
    TreeMap<Integer, GeneratedContract> contractByBlocks = contracts.get(address);
    if (contractByBlocks == null) {
      return null;
    }
    GeneratedContract contract = contractByBlocks.firstEntry().getValue();
    // for non proxy contracts the implementation doesn't depend on block
    if (!contract.isProxy()) {
      return contract;
    }
    Integer floorBlock = contractByBlocks.floorKey(block);
    if (floorBlock != null) {
      return contractByBlocks.get(floorBlock);
    }
    //if we don't have implementation freshest than current block need to check
    return null;
  }

  private static boolean isProxy(List<AbiDefinition> abiDefinitions) {
    for (AbiDefinition abiDefinition : abiDefinitions) {
      if (IMPLEMENTATION.equals(abiDefinition.getName())
          && TYPE_FUNCTION.equals(abiDefinition.getType())
          && VIEW.equals(abiDefinition.getStateMutability())
          && abiDefinition.getInputs().isEmpty()
          && abiDefinition.getOutputs().size() == 1
          && "address".equals(abiDefinition.getOutputs().get(0).getType())
      ) {
        return true;
      }
    }
    return false;
  }

  private static Map<String, Function> abiToFunctions(List<AbiDefinition> abis) {
    Map<String, Function> functionsByHash = new HashMap<>();

    abis.forEach(abi -> {
      try {
        Function function = abiToFunction(abi);
        if (function == null) {
          return;
        }
        String hash = MethodDecoder // todo change when will parse not only output
            .createMethodFullHex(function.getName(), function.getOutputParameters());
        functionsByHash.put(hash, function);
      } catch (Exception e) {
        log.error("Error abi to function");
      }
    });

    return functionsByHash;
  }

  private static Map<String, Event> abiToEvents(List<AbiDefinition> abis) {
    Map<String, Event> eventsByHash = new HashMap<>();
    abis.forEach(abi -> {
      try {
        Event event = abiToEvent(abi);
        if (event == null) {
          return;
        }
        String hash = MethodDecoder
            .createMethodFullHex(event.getName(), event.getParameters());
        eventsByHash.put(hash, event);
      } catch (Exception e) {
        log.error("Error abi to event {}", abi, e);
      }
    });
    return eventsByHash;
  }

  private static Function abiToFunction(AbiDefinition abi) throws ClassNotFoundException {
    if (!abi.getType().equals(TYPE_FUNCTION)
        || !(abi.isConstant()
        || PURE.equals(abi.getStateMutability())
        || VIEW.equals(abi.getStateMutability()))
        || !abi.getInputs().isEmpty() // todo parse functions with inputs
    ) {
      return null;
    }

    List<TypeReference<?>> output = new ArrayList<>();
    for (AbiDefinition.NamedType namedType : abi.getOutputs()) {
      if (namedType.getType().startsWith(TUPLE)) {
        //todo parse tuples
        return null;
      }
      output.add(buildTypeReference(namedType.getType(), namedType.isIndexed()));
    }

    return new Function(abi.getName(), Collections.emptyList(), output);
  }

  private static Event abiToEvent(AbiDefinition abi) throws ClassNotFoundException {
    if (!abi.getType().equals(TYPE_EVENT)) {
      return null;
    }

    List<TypeReference<?>> types = new ArrayList<>();
    for (AbiDefinition.NamedType namedType : abi.getInputs()) {
      if (namedType.getType().startsWith(TUPLE)) {
        //todo parse tuples
        return null;
      }
      types.add(buildTypeReference(namedType.getType(), namedType.isIndexed()));
    }
    return new Event(abi.getName(), types);
  }

  static TypeReference<?> buildTypeReference(String typeDeclaration, boolean indexed)
      throws ClassNotFoundException {
    return (TypeReference<?>) TypeReference.makeTypeReference(
        trimStorageDeclaration(typeDeclaration), indexed, false);
  }

  private static String trimStorageDeclaration(String type) {
    if (type.endsWith(" storage") || type.endsWith(" memory")) {
      return type.split(" ")[0];
    } else {
      return type;
    }
  }

}
