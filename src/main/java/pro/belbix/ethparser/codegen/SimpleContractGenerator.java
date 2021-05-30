package pro.belbix.ethparser.codegen;


import static pro.belbix.ethparser.web3.MethodDecoder.extractLogIndexedValues;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.IMPLEMENTATION;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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
import pro.belbix.ethparser.dto.v0.ContractSourceCodeDTO;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.properties.NetworkProperties;
import pro.belbix.ethparser.repositories.eth.ContractSourceCodeRepository;
import pro.belbix.ethparser.service.AbiProviderService;
import pro.belbix.ethparser.web3.MethodDecoder;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;

@Log4j2
@Service
public class SimpleContractGenerator {

  private static final String UPGRADED_EVENT = "0xbc7cd75a20ee27fd9adebab32041f755214dbc6bffa90cc0225b39da2e5c2d3b";
  private static final String IMPLEMENTATION_HASH = "0x5c60da1b";
  private static final String IMPLEMENTATION_0X = "getFunctionImplementation";
  private static final String IMPLEMENTATION_0X_HASH = "0x972fdd26";
  private static final String TYPE_FUNCTION = "function";
  private static final String TYPE_EVENT = "event";
  private static final String TUPLE = "tuple";
  private static final String PURE = "pure";
  private static final String VIEW = "view";

  private final AbiProviderService abiProviderService;
  private final FunctionsUtils functionsUtils;
  private final Web3Functions web3Functions;
  private final NetworkProperties networkProperties;
  private final ContractDbService contractDbService;
  private final ContractSourceCodeRepository contractSourceCodeRepository;
  private final AppProperties appProperties;

  private final Map<String, TreeMap<Long, GeneratedContract>> contracts = new HashMap<>();

  public SimpleContractGenerator(FunctionsUtils functionsUtils,
      Web3Functions web3Functions,
      NetworkProperties networkProperties,
      ContractDbService contractDbService,
      ContractSourceCodeRepository contractSourceCodeRepository,
      AppProperties appProperties
  ) {
    this.functionsUtils = functionsUtils;
    this.web3Functions = web3Functions;
    this.networkProperties = networkProperties;
    this.contractDbService = contractDbService;
    this.abiProviderService = new AbiProviderService();
    this.contractSourceCodeRepository = contractSourceCodeRepository;
    this.appProperties = appProperties;
  }


  public GeneratedContract getContract(String address, Long block, String selector,
      String network) {
    GeneratedContract generatedContract = findInCache(address, block);
    if (generatedContract != null) {
      return generatedContract;
    }
    return generateContract(address, block, false, selector, network)
        .map(newContract -> {
          log.info("Generated {} {}", newContract.getName(), newContract.getAddress());
          var implementations
              = contracts.computeIfAbsent(address, k -> new TreeMap<>());

          // for reducing memory usage don't save the same proxy impl
          if (!implementations.isEmpty() && block != null) {
            var existContract = implementations.floorEntry(block).getValue();
            if (existContract.isProxy()
                && equalContracts(newContract, existContract)) {
              return newContract;
            }
          }
          if (block != null) {
            implementations.put(block, newContract);
          }
          return newContract;
        }).orElse(null);
  }

  private boolean equalContracts(GeneratedContract newContract, GeneratedContract existContract) {
    return newContract.getAddress().equalsIgnoreCase(existContract.getAddress());
  }

  private Optional<GeneratedContract> generateContract(
      String address,
      Long block,
      boolean isProxy,
      String selector,
      String network
  ) {

    boolean isOverride = isOverrideAbi(address);
    boolean etherscanIsProxy;

    AbiProviderService.SourceCodeResult sourceCode;
    ContractSourceCodeDTO cashedSource =
        contractSourceCodeRepository.findByAddressNetwork(address, network);

    if (cashedSource == null) {
      sourceCode = abiProviderService.contractSourceCode(
          address, networkProperties.get(network).getAbiProviderKey(), network);
      ContractSourceCodeDTO csdto = ContractSourceModelConverter.toDTO(sourceCode);
      csdto.setAddress(address);
      csdto.setNetwork(network);
      contractSourceCodeRepository.save(csdto);
    } else {
      long contractAgeSeconds =
          (new Date().getTime() - cashedSource.getUpdatedAt().getTime()) / 1000;
      // refresh contract
      if (contractAgeSeconds > appProperties.getContractRefreshSeconds()) {
        log.info("Refresh cached contract sources code {}", cashedSource.getContractName());
        sourceCode = abiProviderService.contractSourceCode(
            address, networkProperties.get(network).getAbiProviderKey(), network);
        ContractSourceModelConverter.updateDTO(cashedSource, sourceCode);
        cashedSource.setUpdatedAt(new Date());
        contractSourceCodeRepository.save(cashedSource);
      } else {
        log.info("Used cached contract sources code {}", cashedSource.getContractName());
        sourceCode = ContractSourceModelConverter.toSourceCodeResult(cashedSource);
      }
    }
    if ("UNKNOWN".equals(sourceCode.getContractName()) && !isOverride) {
      return Optional.empty();
    }

    String abi = resolveAbi(address, sourceCode.getAbi());
    List<AbiDefinition> abis = abiToDefinition(abi);
    GeneratedContract contract = new GeneratedContract(
        sourceCode.getContractName(),
        address,
        abiToEvents(abis),
        getEventNameHashMapFromAbi(abis),
        abiToFunctions(abis)
    );

    etherscanIsProxy = "1".equals(sourceCode.getProxy());
    if (!isProxy && (etherscanIsProxy || isProxy(abis))) {
      log.info("Detect proxy {}", address);
      String proxyAddress = readProxyAddressOnChain(address, block, contract, selector, network);
      if (proxyAddress == null) {
        if (etherscanIsProxy) {
          log.info("Try to generate proxy from etherscan implementation");
          // only last implementation but it's better than nothing
          return generateContract(sourceCode.getImplementation(), block, true, selector, network);
        }
        log.error("Can't reach proxy impl adr for {} at {}", address, block);
        return Optional.empty();
      }
      return generateContract(proxyAddress, block, true, selector, network);
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

  private boolean isOverrideAbi(String address) {
    return StaticAbiMap.MAP.containsKey(address.toLowerCase());
  }

  private String readProxyAddressOnChain(
      String address,
      Long block,
      GeneratedContract contract,
      String selector,
      String network
  ) {
    String proxyImpl;

    // EIP-897 DelegateProxy concept
    if (contract.getFunction(IMPLEMENTATION_HASH) != null) {
      proxyImpl = proxyAddressFromFunc(address, block, network);
      if (proxyImpl != null) {
        return proxyImpl;
      }
    }

    // open zeppelin proxy doesn't have public call_implementation
    // some contracts have event but didn't call it
    proxyImpl = findLastProxyUpgrade(address, block, contract.getEvent(UPGRADED_EVENT),
        network);
    if (proxyImpl != null) {
      return proxyImpl;
    }

    //0xProxy https://github.com/0xProject/0x-protocol-specification/blob/master/exchange-proxy/exchange-proxy.md
    if (contract.getFunction(IMPLEMENTATION_0X_HASH) != null && selector != null) {
      selector = selector.replace("0x", "");
      byte[] selectorB = DatatypeConverter.parseHexBinary(selector);
      return functionsUtils
          .callAddressByNameBytes4(IMPLEMENTATION_0X, selectorB, address, block, network)
          .orElse(null);
    }

    // manual proxy implementation can't be detected onchain
    return null;
  }

  private String proxyAddressFromFunc(String address, Long block, String network) {
    return functionsUtils.callAddressByName(IMPLEMENTATION, address, block, network)
        .orElse(null);
  }

  private String findLastProxyUpgrade(String address, Long _block, Event event, String network) {
    // todo collect full block range (now we not able to do it with eth node limitations)
    Long created = contractDbService.getContractByAddress(address, network)
        .map(ContractEntity::getCreated)
        .orElse(null);
    if (created == null) {
      return null;
    }
    int start = created.intValue() - 1;
    int end = created.intValue() + 10_000;

    //noinspection rawtypes
    List<LogResult> logResults = web3Functions.fetchContractLogs(
        List.of(address),
        start,
        end,
        network,
        UPGRADED_EVENT);
    if (logResults == null || logResults.isEmpty()) {
      return null;
    }
    Log ethLog = (Log) logResults.get(logResults.size() - 1).get();
    //noinspection rawtypes
    List<Type> types = extractLogIndexedValues(
        ethLog.getTopics(), ethLog.getData(), event.getParameters());
    if (types == null || types.isEmpty()) {
      log.error("Empty types for {}", ethLog);
      return null;
    }
    return (String) types.get(0).getValue();
  }

  private GeneratedContract findInCache(String address, Long block) {
    if (block == null) {
      return null;
    }
    TreeMap<Long, GeneratedContract> contractByBlocks = contracts.get(address);
    if (contractByBlocks == null || contractByBlocks.firstEntry() == null) {
      return null;
    }
    GeneratedContract contract = contractByBlocks.firstEntry().getValue();
    // for non proxy contracts the implementation doesn't depend on block
    if (!contract.isProxy()) {
      return contract;
    }
    Long floorBlock = contractByBlocks.floorKey(block);
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

  private static Map<String, String> getEventNameHashMapFromAbi(List<AbiDefinition> abis) {
    Map<String, String> nameToHash = new HashMap<>();
    abis.forEach(abi -> {
      try {
        Event event = abiToEvent(abi);
        if (event == null) {
          return;
        }
        String hash = MethodDecoder.createMethodFullHex(event.getName(), event.getParameters());
        nameToHash.put(event.getName(), hash);
      } catch (Exception e) {
        log.error("Error abi to event {}", abi.getName(), e);
      }
    });
    return nameToHash;
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
