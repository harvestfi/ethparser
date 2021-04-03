package pro.belbix.ethparser.codegen;


import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.MethodDecoder.extractLogIndexedValues;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import javax.xml.bind.DatatypeConverter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.codegen.abi.StaticAbiMap;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.service.AbiProviderService;
import pro.belbix.ethparser.web3.MethodDecoder;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;

@Log4j2
@Service
public class SimpleContractGenerator {

  private static final String UPGRADED_EVENT = "0xbc7cd75a20ee27fd9adebab32041f755214dbc6bffa90cc0225b39da2e5c2d3b";
  private static final String IMPLEMENTATION = "implementation";
  private static final String IMPLEMENTATION_HASH = "0x5c60da1b";
  private static final String IMPLEMENTATION_0X = "getFunctionImplementation";
  private static final String IMPLEMENTATION_0X_HASH = "0x972fdd26";
  private static final String TYPE_FUNCTION = "function";
  private static final String TYPE_EVENT = "event";
  private static final String TUPLE = "tuple";
  private static final String PURE = "pure";
  private static final String VIEW = "view";

  private final AbiProviderService abiProviderService;
  private final AppProperties appProperties;
  private final FunctionsUtils functionsUtils;
  private final Web3Functions web3Functions;

  private final Map<String, TreeMap<Integer, GeneratedContract>> contracts = new HashMap<>();

  public SimpleContractGenerator(AppProperties appProperties, FunctionsUtils functionsUtils,
      Web3Functions web3Functions) {
    this.appProperties = appProperties;
    this.functionsUtils = functionsUtils;
    this.web3Functions = web3Functions;
    this.abiProviderService = new AbiProviderService(appProperties.getNetwork());
  }

  public GeneratedContract getContract(String address, int block) {
    return getContract(address, block, null);
  }

  public GeneratedContract getContract(String address, int block, String selector) {
    GeneratedContract generatedContract = findInCache(address, block);
    if (generatedContract != null) {
      return generatedContract;
    }
    return generateContract(address, block, false, selector)
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

  private Optional<GeneratedContract> generateContract(
      String address,
      long block,
      boolean isProxy,
      String selector
  ) {

    boolean isOverride = isOverrideAbi(address);
    String abi = null;
    String contractName = "UNKNOWN";
    boolean etherscanIsProxy = false;
    String etherscanProxyImpl = "";

    AbiProviderService.SourceCodeResult sourceCode =
        abiProviderService.contractSourceCode(address, getAbiProviderKey());

    if (sourceCode == null) {
      if (!isOverride) {
        return Optional.empty();
      }
    } else {
      etherscanIsProxy = "1".equals(sourceCode.getProxy());
      if(etherscanIsProxy) {
        etherscanProxyImpl = sourceCode.getImplementation();
      }
      abi = sourceCode.getAbi();
      contractName = sourceCode.getContractName();
    }

    abi = resolveAbi(address, abi);

    List<AbiDefinition> abis = abiToDefinition(abi);
    GeneratedContract contract = new GeneratedContract(
        contractName,
        address,
        abiToEvents(abis),
        abiToFunctions(abis)
    );

    if (!isProxy && (etherscanIsProxy || isProxy(abis))) {
      log.info("Detect proxy {}", address);
      String proxyAddress = readProxyAddressOnChain(address, block, contract, selector);
      if (proxyAddress == null) {
        if(etherscanIsProxy) {
          log.info("Try to generate proxy from etherscan implementation");
          // only last implementation but it's better than nothing
          return generateContract(etherscanProxyImpl, block, true, selector);
        }
        log.error("Can't reach proxy impl adr for {} at {}", address, block);
        return Optional.empty();
      }
      return generateContract(proxyAddress, block, true, selector);
    }

    contract.setProxy(isProxy);

    return Optional.of(contract);
  }

  private String getAbiProviderKey() {
    switch (appProperties.getNetwork()) {
      case ETH_NETWORK:
        return appProperties.getEtherscanApiKey();
      case BSC_NETWORK:
        return appProperties.getEtherscanApiKey();
      default:
        throw new IllegalStateException("Unknown network " + appProperties.getNetwork());
    }
  }

  private String resolveAbi(String address, String abi) {
    if (StaticAbiMap.MAP.containsKey(address.toLowerCase())) {
      return StaticAbiMap.MAP.get(address.toLowerCase());
    }
    return abi;
  }

  private boolean isOverrideAbi(String address) {
    return StaticAbiMap.MAP.containsKey(address.toLowerCase());
  }

  private String readProxyAddressOnChain(
      String address,
      long block,
      GeneratedContract contract,
      String selector
  ) {
    // open zeppelin proxy doesn't have public call_implementation
    // some contracts have event but didn't call it
    String proxyImpl =
        findLastProxyUpgrade(address, (int) block, contract.getEvent(UPGRADED_EVENT));
    if (proxyImpl != null) {
      return proxyImpl;
    }
    // EIP-897 DelegateProxy concept
    if (contract.getFunction(IMPLEMENTATION_HASH) != null) {
      proxyImpl = proxyAddressFromFunc(address, block);
      if (proxyImpl != null) {
        return proxyImpl;
      }
    }

    //0xProxy https://github.com/0xProject/0x-protocol-specification/blob/master/exchange-proxy/exchange-proxy.md
    if (contract.getFunction(IMPLEMENTATION_0X_HASH) != null && selector != null) {
      selector = selector.replace("0x", "");
      byte[] selectorB = DatatypeConverter.parseHexBinary(selector);
      return functionsUtils.callAddressByNameBytes4(IMPLEMENTATION_0X, selectorB, address, block)
          .orElse(null);
    }

    // manual proxy implementation can't be detected onchain
    return null;
  }

  private String proxyAddressFromFunc(String address, long block) {
    return functionsUtils.callAddressByName(IMPLEMENTATION, address, block)
        .orElse(null);
  }

  private String findLastProxyUpgrade(String address, Integer block, Event event) {
    List<LogResult> logResults = web3Functions.fetchContractLogs(
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

  private static Map<String, FunctionWrapper> abiToFunctions(List<AbiDefinition> abis) {
    Map<String, FunctionWrapper> functionsByMethodId = new HashMap<>();

    abis.forEach(abi -> {
      try {
        FunctionWrapper function = abiToFunction(abi);
        if (function == null) {
          return;
        }
        String methodId = MethodDecoder
            .createMethodId(function.getFunction().getName(),
                function.getInput());
        functionsByMethodId.put(methodId, function);
      } catch (Exception e) {
        log.error("Error abi to function");
      }
    });

    return functionsByMethodId;
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
        log.error("Error abi to event {}", abi.getName(), e);
      }
    });
    return eventsByHash;
  }

  private static FunctionWrapper abiToFunction(AbiDefinition abi) throws ClassNotFoundException {
    if (!abi.getType().equals(TYPE_FUNCTION)) {
      return null;
    }
    boolean isView = abi.isConstant()
        || PURE.equals(abi.getStateMutability())
        || VIEW.equals(abi.getStateMutability());

    List<TypeReference<?>> output = new ArrayList<>();
    for (AbiDefinition.NamedType namedType : abi.getOutputs()) {
      if (namedType.getType().startsWith(TUPLE)) {
        //todo parse tuples
        return null;
      }
      if (!namedType.getComponents().isEmpty()) {
        //todo parse multi components
        log.error("Multi components {}", abi);
        return null;
      }
      output.add(buildTypeReference(namedType.getType(), namedType.isIndexed()));
    }

    List<TypeReference<?>> input = new ArrayList<>();
    for (AbiDefinition.NamedType namedType : abi.getInputs()) {
      if (namedType.getType().startsWith(TUPLE)) {
        //todo parse tuples
        return null;
      }
      if (!namedType.getComponents().isEmpty()) {
        //todo parse multi components
        log.error("Multi components {}", abi);
        return null;
      }
      input.add(buildTypeReference(namedType.getType(), false));
    }

    Function function = new Function(abi.getName(), Collections.emptyList(), output);
    return new FunctionWrapper(function, isView, input);
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

  private static List<AbiDefinition> abiToDefinition(String abi) {
    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    AbiDefinition[] abiDefinition;
    try {
      abiDefinition = objectMapper.readValue(abi, AbiDefinition[].class);
    } catch (IOException e) {
      log.error("abiToDefinition error for: {}", abi);
      throw new RuntimeException(e);
    }
    return Arrays.asList(abiDefinition);
  }

}
