package pro.belbix.ethparser.web3.abi;

import static java.math.BigInteger.ZERO;
import static org.web3j.abi.TypeReference.makeTypeReference;
import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;
import static pro.belbix.ethparser.utils.Caller.silentCall;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.BALANCE_OF;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.DECIMALS;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.GET_RESERVES;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.NAME;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOKEN0;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOKEN1;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ZERO_ADDRESS;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes4;
import org.web3j.abi.datatypes.generated.Uint112;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.web3.MethodDecoder;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;

@SuppressWarnings("rawtypes")
@Service
@Log4j2
public class FunctionsUtils {

  public final static double SECONDS_OF_YEAR = 31557600.0;
  public final static double SECONDS_IN_WEEK = 604800.0;
  public final static String TYPE_ADR = "adr";
  public final static String TYPE_STR = "str";
  public final static String TYPE_INT = "int";
  public final static String TYPE_BOOL = "bool";

  private final Map<String, Function> functionsCache = new HashMap<>();

  private final Web3Functions web3Functions;
  private final ContractDbService contractDbService;

  public FunctionsUtils(Web3Functions web3Functions,
      ContractDbService contractDbService) {
    this.web3Functions = web3Functions;
    this.contractDbService = contractDbService;
  }

  // todo complex functions should be decomposed and use simple calls ************************
  public Tuple2<Double, Double> callReserves(
      String lpAddress,
      Long block,
      String network) {

    String lpName = callStrByName(NAME, lpAddress, block, network).orElse("");

    if (lpName.startsWith("1inch")) {
      return callOneInchReserves(lpAddress, block, network);
    } else {
      return callUniReserves(lpAddress, block, network);
    }
  }

  private Tuple2<Double, Double> callOneInchReserves(String lpAddress, Long block, String network) {
    String coin0 = callAddressByName(TOKEN0, lpAddress, block, network)
        .orElseThrow(() -> new IllegalStateException("Error get token0 for " + lpAddress));
    String coin1 = callAddressByName(TOKEN1, lpAddress, block, network)
        .orElseThrow(() -> new IllegalStateException("Error get token1 for " + lpAddress));

    double coin0Balance = parseAmount(
        callIntByNameWithAddressArg(BALANCE_OF, lpAddress, coin0, block, network)
            .orElse(ZERO), coin0, network);
    double coin1Balance = parseAmount(
        callIntByNameWithAddressArg(BALANCE_OF, lpAddress, coin1, block, network)
            .orElse(ZERO), coin1, network);
    String baseAdr = ContractUtils.getBaseNetworkWrappedTokenAddress(network);
    double baseBalance = parseAmount(
        web3Functions.fetchBalance(lpAddress, block, network), baseAdr, network);
    if (coin0Balance == 0 || coin1Balance == 0) {
      if (ZERO_ADDRESS.equals(coin0)) {
        coin0Balance = baseBalance;
      } else if (ZERO_ADDRESS.equals(coin1)) {
        coin1Balance = baseBalance;
      }
    }
    return new Tuple2<>(coin0Balance, coin1Balance);
  }

  private Tuple2<Double, Double> callUniReserves(String lpAddress, Long block, String network) {
    List<Type> types = web3Functions.callFunction(new Function(
        GET_RESERVES,
        Collections.emptyList(),
        Arrays.asList(new TypeReference<Uint112>() {
                      },
            new TypeReference<Uint112>() {
            },
            new TypeReference<Uint32>() {
            }
        )), lpAddress, resolveBlock(block), network);
    if (types == null || types.size() < 3) {
      log.error("Wrong values for " + lpAddress);
      return null;
    }

    Tuple2<String, String> tokens = contractDbService.tokenAddressesByUniPairAddress(
        lpAddress, network);
    BigInteger v1 = (BigInteger) types.get(0).getValue();
    BigInteger v2 = (BigInteger) types.get(1).getValue();
    return new Tuple2<>(
        parseAmount(v1, tokens.component1(), network),
        parseAmount(v2, tokens.component2(), network)
    );
  }

  public double fetchUint256Field(
      String functionName,
      String address,
      String underlyingAddress,
      long block,
      String network,
      String... args
  ) {
    if (args == null || args.length == 0) {
      return parseAmount(
          callIntByName(functionName, address, block, network)
              .orElseThrow(
                  () -> new IllegalStateException(
                      "Can't fetch " + functionName + " for " + address)
              ), underlyingAddress, network);
    } else {
      return parseAmount(
          callIntByNameWithAddressArg(functionName, args[0], address, block, network)
              .orElseThrow(
                  () -> new IllegalStateException(
                      "Can't fetch " + functionName + " for " + address)
              ), underlyingAddress, network);
    }
  }

  // ****************************************************************************

  public Optional<String> callAddressByName(String functionName, String hash, Long block,
      String network) {
    return callStringFunction(findSimpleFunction(functionName, TYPE_ADR), hash, block, network);
  }

  public Optional<String> callAddressByNameWithArg(
      String functionName,
      String arg,
      String hash,
      Long block,
      String network) {
    List<Type> argTypes;
    if (arg.startsWith("0x")) {
      argTypes = Collections.singletonList(new Address(arg));
    } else {
      argTypes = Collections.singletonList(new Uint256(Integer.parseInt(arg)));
    }
    return callStringFunction(
        new Function(
            functionName,
            argTypes,
            Collections.singletonList(new TypeReference<Address>() {
            })),
        hash,
        block,
        network);
  }

  public Optional<String> callAddressByNameBytes4(
      String functionName,
      byte[] arg,
      String hash,
      Long block,
      String network) {
    // you should create function for every new argument
    return callStringFunction(new Function(
        functionName,
        Collections.singletonList(new Bytes4(arg)),
        Collections.singletonList(new TypeReference<Address>() {
        })), hash, block, network);
  }

  public Optional<String> callStrByName(String functionName, String hash, Long block,
      String network) {
    return callStringFunction(findSimpleFunction(functionName, TYPE_STR), hash, block, network);
  }

  public Optional<BigInteger> callIntByName(String functionName, String hash, Long block,
      String network) {
    return callUint256Function(findSimpleFunction(functionName, TYPE_INT), hash, block, network);
  }

  public Optional<Boolean> callBoolByName(String functionName, String hash, Long block,
      String network) {
    return callBoolFunction(findSimpleFunction(functionName, TYPE_BOOL), hash, block, network);
  }

  public Optional<BigInteger> callIntByNameWithAddressArg(
      String functionName,
      String arg,
      String hash,
      Long block,
      String network) {
    // you should create function for every new argument
    return callUint256Function(new Function(
        functionName,
        Collections.singletonList(new Address(arg)),
        Collections.singletonList(new TypeReference<Uint256>() {
        })), hash, block, network);
  }

  public Optional<BigInteger> callIntByNameWithAddressesArrayAndBigIntegerArg(
      String functionName,
      List<String> addresses,
      BigInteger arg2,
      String hash,
      Long block,
      String network) {

    List<Address> addressList = addresses.stream()
        .map(Address::new)
        .collect(Collectors.toList());

    DynamicArray addressesArray = new DynamicArray<Address>(Address.class, addressList);

    return callUint256Function(new Function(
        functionName,
        List.of(addressesArray, new Uint256(arg2)),
        Collections.singletonList(new TypeReference<Uint256>() {
        })), hash, block, network);
  }

  public Optional<Boolean> callBoolByNameWithAddressArg(
      String functionName,
      String arg,
      String hash,
      Long block,
      String network) {
    // you should create function for every new argument
    return callBoolFunction(new Function(
        functionName,
        Collections.singletonList(new Address(arg)),
        Collections.singletonList(new TypeReference<Bool>() {
        })), hash, block, network);
  }

  public Optional<String> callViewFunction(Function function, String address, long block,
      String network) {
    List<Type> response = web3Functions.callFunction(function, address,
        DefaultBlockParameter.valueOf(BigInteger.valueOf(block)), network);
    if (response == null || response.isEmpty()) {
      return Optional.empty();
    }
    try {
      return Optional.ofNullable(MethodDecoder.typesToString(response));
    } catch (JsonProcessingException e) {
      return Optional.empty();
    }
  }

  public double parseAmount(BigInteger amount, String address, String network) {
    if (amount == null || ZERO_ADDRESS.equalsIgnoreCase(address)) {
      return 0.0;
    }
    return new BigDecimal(amount)
        .divide(getDividerByAddress(address, network), 99, RoundingMode.HALF_UP)
        .doubleValue();
  }

  public BigDecimal getDividerByAddress(String _address, String network) {
    String address = _address.toLowerCase();
    long decimals;
    // unique addresses
    if (ContractUtils.isPsAddress(address)) {
      decimals = 18L;
    } else {
      ContractEntity contract = contractDbService.getContractByAddress(address, network)
          .orElse(new ContractEntity());
      if (contract.getType() == ContractType.VAULT.getId()) {
        decimals = contractDbService.getVaultByAddress(address, network)
            .orElseThrow(
                () -> new IllegalStateException("Vault Not found by " + address)
            ).getDecimals();
      } else if (contract.getType() == ContractType.POOL.getId()) {
        decimals = 18L;
      } else if (contract.getType() == ContractType.UNI_PAIR.getId()) {
        decimals = contractDbService.getLpByAddress(address, network)
            .orElseThrow(
                () -> new IllegalStateException("LP Not found by " + address)
            ).getDecimals();
      } else if (contract.getType() == ContractType.TOKEN.getId()) {
        decimals = contractDbService.getTokenByAddress(address, network)
            .orElseThrow(
                () -> new IllegalStateException("Token Not found by " + address)
            ).getDecimals();
      } else {
        decimals = callIntByName(DECIMALS, address, null, network)
            .orElseThrow(
                () -> new IllegalStateException("Can't fetch decimals for " + address)
            ).longValue();
      }
    }
    return new BigDecimal(10L).pow((int) decimals);
  }

  public static TypeReference typeReferenceByName(String name) {
    return silentCall(() -> makeTypeReference(name))
        .orElseThrow(
            () -> new IllegalStateException("Error make type reference for " + name)
        );
  }

  public static List<TypeReference<?>> typeReferencesList(String... names) {
    List<TypeReference<?>> types = new ArrayList<>();
    for (String name : names) {
      types.add(typeReferenceByName(name));
    }
    return types;
  }

  // ************ PRIVATE METHODS **************************

  private Function findSimpleFunction(String name, String returnType) {
    // if we will have functions with the same names - create postfix
    Function function = functionsCache.get(name);
    if (function == null) {
      if (TYPE_ADR.equals(returnType)) {
        function = new Function(
            name,
            Collections.emptyList(),
            Collections.singletonList(new TypeReference<Address>() {
            }));
      } else if (TYPE_STR.equals(returnType)) {
        function = new Function(
            name,
            Collections.emptyList(),
            Collections.singletonList(new TypeReference<Utf8String>() {
            }));
      } else if (TYPE_INT.equals(returnType)) {
        function = new Function(
            name,
            Collections.emptyList(),
            Collections.singletonList(new TypeReference<Uint256>() {
            }));
      } else if (TYPE_BOOL.equals(returnType)) {
        function = new Function(
            name,
            Collections.emptyList(),
            Collections.singletonList(new TypeReference<Bool>() {
            }));
      } else {
        throw new IllegalStateException("Unknown function type " + returnType);
      }
      functionsCache.put(name, function);
    }
    return function;
  }

  private Optional<String> callStringFunction(Function function, String hash, Long block,
      String network) {
    List<Type> types = web3Functions.callFunction(function, hash, resolveBlock(block), network);
    if (types == null || types.isEmpty()) {
      // we use an absent function for determination a contract type
//      log.info(function.getName() + " Wrong callback for hash: " + hash);
      return Optional.empty();
    }
    return Optional.ofNullable((String) types.get(0).getValue());
  }

  private Optional<BigInteger> callUint256Function(Function function, String hash, Long block,
      String network) {
    List<Type> types = web3Functions.callFunction(function, hash, resolveBlock(block), network);
    if (types == null || types.isEmpty()) {
      // we use an absent function for determination a contract type
//      log.warn(function.getName() + " Wrong callback for hash: " + hash);
      return Optional.empty();
    }
    return Optional.ofNullable((BigInteger) types.get(0).getValue());
  }

  private Optional<Boolean> callBoolFunction(Function function, String hash, Long block,
      String network) {
    List<Type> types = web3Functions.callFunction(function, hash, resolveBlock(block), network);
    if (types == null || types.isEmpty()) {
      // we use an absent function for determination a contract type
//      log.warn(function.getName() + " Wrong callback for hash: " + hash);
      return Optional.empty();
    }
    return Optional.ofNullable((Boolean) types.get(0).getValue());
  }

  private static DefaultBlockParameter resolveBlock(Long block) {
    if (block != null) {
      return new DefaultBlockParameterNumber(block);
    }
    return LATEST;
  }
}
