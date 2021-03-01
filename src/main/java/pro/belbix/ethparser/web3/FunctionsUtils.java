package pro.belbix.ethparser.web3;

import static java.math.BigInteger.ZERO;
import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;
import static pro.belbix.ethparser.web3.FunctionsNames.BALANCE_OF;
import static pro.belbix.ethparser.web3.FunctionsNames.GET_RESERVES;
import static pro.belbix.ethparser.web3.FunctionsNames.TOKEN0;
import static pro.belbix.ethparser.web3.FunctionsNames.TOKEN1;
import static pro.belbix.ethparser.web3.MethodDecoder.parseAmount;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.PAIR_TYPE_ONEINCHE;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ZERO_ADDRESS;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint112;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.entity.contracts.TokenEntity;
import pro.belbix.ethparser.web3.contracts.ContractUtils;

@SuppressWarnings("rawtypes")
@Service
@Log4j2
public class FunctionsUtils {

    public final static double SECONDS_OF_YEAR = 31557600.0;
    public final static double SECONDS_IN_WEEK = 604800.0;
    public final static String TYPE_ADR = "adr";
    public final static String TYPE_STR = "str";
    public final static String TYPE_INT = "int";

    private final Map<String, Function> functionsCache = new HashMap<>();

    private final Web3Service web3Service;

    public FunctionsUtils(Web3Service web3Service) {
        this.web3Service = web3Service;
    }

    // todo complex functions should be decomposed and use simple calls ************************
    public Tuple2<Double, Double> callReserves(String lpAddress, Long block) {
        if (ContractUtils.getUniPairType(lpAddress) == PAIR_TYPE_ONEINCHE) {
            return callOneInchReserves(lpAddress, block);
        } else {
            return callUniReserves(lpAddress, block);
        }
    }

    private Tuple2<Double, Double> callOneInchReserves(String lpAddress, Long block) {
        String coin0 = callAddressByName(TOKEN0, lpAddress, block)
            .orElseThrow(() -> new IllegalStateException("Error get token0 for " + lpAddress));
        String coin1 = callAddressByName(TOKEN1, lpAddress, block)
            .orElseThrow(() -> new IllegalStateException("Error get token1 for " + lpAddress));

        double coin0Balance = 0;
        double coin1Balance = 0;
        if (!ZERO_ADDRESS.equals(coin0)) {
            coin0Balance = parseAmount(callIntByName(BALANCE_OF, lpAddress, coin0, block)
                .orElse(ZERO), coin0);
        }
        if (!ZERO_ADDRESS.equals(coin1)) {
            coin1Balance = parseAmount(callIntByName(BALANCE_OF, lpAddress, coin1, block)
                .orElse(ZERO), coin1);
        }
        return new Tuple2<>(coin0Balance, coin1Balance);
    }

    private Tuple2<Double, Double> callUniReserves(String lpAddress, Long block) {
        List<Type> types = web3Service.callFunction(new Function(
            GET_RESERVES,
            Collections.emptyList(),
            Arrays.asList(new TypeReference<Uint112>() {
                          },
                new TypeReference<Uint112>() {
                },
                new TypeReference<Uint32>() {
                }
            )), lpAddress, resolveBlock(block));
        if (types == null || types.size() < 3) {
            log.error("Wrong values for " + lpAddress);
            return null;
        }

        Tuple2<TokenEntity, TokenEntity> tokens = ContractUtils.getUniPairTokens(lpAddress);
        BigDecimal v1 = new BigDecimal((BigInteger) types.get(0).getValue());
        BigDecimal v2 = new BigDecimal((BigInteger) types.get(1).getValue());
        return new Tuple2<>(
            v1.divide(new BigDecimal(10L).pow(tokens.component1().getDecimals().intValue())
                , 99, RoundingMode.HALF_UP).doubleValue(),
            v2.divide(new BigDecimal(10L).pow(tokens.component2().getDecimals().intValue())
                , 99, RoundingMode.HALF_UP).doubleValue()
        );
    }

    // ****************************************************************************

    public Optional<String> callAddressByName(String functionName, String hash, Long block) {
        return callStringFunction(findSimpleFunction(functionName, TYPE_ADR), hash, block);
    }

    public Optional<String> callStrByName(String functionName, String hash, Long block) {
        return callStringFunction(findSimpleFunction(functionName, TYPE_STR), hash, block);
    }

    public Optional<BigInteger> callIntByName(String functionName, String hash, Long block) {
        return callUint256Function(findSimpleFunction(functionName, TYPE_INT), hash, block);
    }

    public Optional<BigInteger> callIntByName(String functionName, String arg, String hash, Long block) {
        // you should create function for every new argument
        return callUint256Function(new Function(
            functionName,
            Collections.singletonList(new Address(arg)),
            Collections.singletonList(new TypeReference<Uint256>() {
            })), hash, block);
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

            } else {
                throw new IllegalStateException("Unknown function type " + returnType);
            }
            functionsCache.put(name, function);
        }
        return function;
    }

    private Optional<String> callStringFunction(Function function, String hash, Long block) {
        List<Type> types = web3Service.callFunction(function, hash, resolveBlock(block));
        if (types == null || types.isEmpty()) {
            log.error(function.getName() + " Wrong callback for hash: " + hash);
            return Optional.empty();
        }
        return Optional.ofNullable((String) types.get(0).getValue());
    }

    private Optional<BigInteger> callUint256Function(Function function, String hash, Long block) {
        List<Type> types = web3Service.callFunction(function, hash, resolveBlock(block));
        if (types == null || types.isEmpty()) {
            log.error(function.getName() + " Wrong callback for hash: " + hash);
            return Optional.empty();
        }
        return Optional.ofNullable((BigInteger) types.get(0).getValue());
    }

    private static DefaultBlockParameter resolveBlock(Long block) {
        if (block != null) {
            return new DefaultBlockParameterNumber(block);
        }
        return LATEST;
    }
}
