package pro.belbix.ethparser.web3;

import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint112;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.web3.uniswap.contracts.LpContracts;

@SuppressWarnings("rawtypes")
@Service
public class Functions {
    public final static double SECONDS_OF_YEAR = 31557600.0;
    public final static double SECONDS_IN_WEEK = 604800.0;
    private static final Logger log = LoggerFactory.getLogger(Functions.class);
    private final Web3Service web3Service;

    public Functions(Web3Service web3Service) {
        this.web3Service = web3Service;
    }

    public BigInteger callPricePerFullShare(String contractAddress, Long block) {
        List<Type> types = web3Service
            .callFunction(GET_PRICE_PER_FULL_SHARE, contractAddress, resolveBlock(block));
        if (types == null || types.isEmpty()) {
            return null;
        }
        return (BigInteger) types.get(0).getValue();
    }

    public Tuple2<Double, Double> callReserves(String lpAddress, Long block) {
        List<Type> types = web3Service
            .callFunction(GET_RESERVES, lpAddress, resolveBlock(block));
        if (types == null || types.size() < 3) {
            log.error("Wrong values for " + lpAddress);
            return null;
        }

        Tuple2<Double, Double> dividers = LpContracts.lpPairsDividers.get(lpAddress);
        if (dividers == null) {
            throw new IllegalStateException("Not found divider for " + lpAddress);
        }
        double v1 = ((BigInteger) types.get(0).getValue()).doubleValue();
        double v2 = ((BigInteger) types.get(1).getValue()).doubleValue();
        return new Tuple2<>(
            v1 / dividers.component1(),
            v2 / dividers.component2()
        );
    }

    public BigInteger callErc20TotalSupply(String hash, Long block) {
        return callUint256Function(ERC_20_TOTAL_SUPPLY, hash, block);
    }

    public BigInteger callUnderlyingUnit(String hash, Long block) {
        return callUint256Function(UNDERLYING_UNIT, hash, block);
    }

    public BigInteger callRewardPerTokens(String hash, Long block) {
        return callUint256Function(REWARD_PER_TOKEN, hash, block);
    }

    public BigInteger callLastTimeRewardApplicable(String hash, Long block) {
        return callUint256Function(LAST_TIME_REWARD_APPLICABLE, hash, block);
    }

    public BigInteger callRewardRate(String hash, Long block) {
        return callUint256Function(REWARD_RATE, hash, block);
    }

    public BigInteger callPeriodFinish(String hash, Long block) {
        return callUint256Function(PERIOD_FINISH, hash, block);
    }

    public BigInteger callSellFloor(String hash, Long block) {
        return callUint256Function(SELL_FLOOR, hash, block);
    }

    public String callRewardToken(String hash, Long block) {
        return callAddressFunction(REWARD_TOKEN, hash, block);
    }

    public String callRewardPool(String hash, Long block) {
        return callAddressFunction(REWARD_POOL, hash, block);
    }

    public BigInteger callUnderlyingBalance(String holder, String hash, Long block) {
        return callUint256Function(new Function(
            "underlyingBalanceWithInvestmentForHolder",
            Collections.singletonList(new Address(holder)),
            Collections.singletonList(new TypeReference<Uint256>() {
            })), hash, block);
    }

    public BigInteger callBalanceOf(String holder, String hash, Long block) {
        return callUint256Function(new Function(
            "balanceOf",
            Collections.singletonList(new Address(holder)),
            Collections.singletonList(new TypeReference<Uint256>() {
            })), hash, block);
    }

    private BigInteger callUint256Function(Function function, String hash, Long block) {
        List<Type> types = web3Service.callFunction(function, hash, resolveBlock(block));
        if (types == null || types.isEmpty()) {
            log.error(function.getName() + " Wrong callback " + hash);
            return null;
        }
        return (BigInteger) types.get(0).getValue();
    }

    private String callAddressFunction(Function function, String hash, Long block) {
        List<Type> types = web3Service.callFunction(function, hash, resolveBlock(block));
        if (types == null || types.isEmpty()) {
            log.error(function.getName() + " Wrong callback " + hash);
            return null;
        }
        return (String) types.get(0).getValue();
    }

    public static DefaultBlockParameter resolveBlock(Long block) {
        if (block != null) {
            return new DefaultBlockParameterNumber(block);
        }
        return LATEST;
    }

    static final Function GET_PRICE_PER_FULL_SHARE = new Function(
        "getPricePerFullShare",
        Collections.emptyList(),
        Collections.singletonList(new TypeReference<Uint256>() {
        }));

    static final Function GET_RESERVES = new Function(
        "getReserves",
        Collections.emptyList(),
        Arrays.asList(new TypeReference<Uint112>() {
                      },
            new TypeReference<Uint112>() {
            },
            new TypeReference<Uint32>() {
            }
        ));

    static final Function ERC_20_TOTAL_SUPPLY = new Function(
        "totalSupply",
        Collections.emptyList(),
        Collections.singletonList(new TypeReference<Uint256>() {
        }));

    static final Function UNDERLYING_UNIT = new Function(
        "underlyingUnit",
        Collections.emptyList(),
        Collections.singletonList(new TypeReference<Uint256>() {
        }));

    static final Function REWARD_PER_TOKEN = new Function(
        "rewardPerToken",
        Collections.emptyList(),
        Collections.singletonList(new TypeReference<Uint256>() {
        }));

    static final Function LAST_TIME_REWARD_APPLICABLE = new Function(
        "lastTimeRewardApplicable",
        Collections.emptyList(),
        Collections.singletonList(new TypeReference<Uint256>() {
        }));

    static final Function REWARD_RATE = new Function(
        "rewardRate",
        Collections.emptyList(),
        Collections.singletonList(new TypeReference<Uint256>() {
        }));

    static final Function PERIOD_FINISH = new Function(
        "periodFinish",
        Collections.emptyList(),
        Collections.singletonList(new TypeReference<Uint256>() {
        }));

    static final Function SELL_FLOOR = new Function(
        "sellFloor",
        Collections.emptyList(),
        Collections.singletonList(new TypeReference<Uint256>() {
        }));

    static final Function REWARD_TOKEN = new Function(
        "rewardToken",
        Collections.emptyList(),
        Collections.singletonList(new TypeReference<Address>() {
        }));

    static final Function REWARD_POOL = new Function(
        "rewardPool",
        Collections.emptyList(),
        Collections.singletonList(new TypeReference<Address>() {
        }));

}
