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
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint112;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.web3.harvest.Vaults;
import pro.belbix.ethparser.web3.uniswap.LpContracts;

@SuppressWarnings("rawtypes")
@Service
public class Functions {

    private static final Logger log = LoggerFactory.getLogger(Functions.class);
    private final Web3Service web3Service;

    public Functions(Web3Service web3Service) {
        this.web3Service = web3Service;
    }

    public BigInteger callPricePerFullShare(String contractAddress) {
        List<Type> types = web3Service
            .callMethod(GET_PRICE_PER_FULL_SHARE, contractAddress, LATEST); //TODO archive data required for not LATEST
        if (types == null || types.isEmpty()) {
            return BigInteger.ONE;
        }
        return (BigInteger) types.get(0).getValue();
    }

    public Tuple2<Double, Double> callReserves(String lpAddress) {
        List<Type> types = web3Service
            .callMethod(GET_RESERVES, lpAddress, LATEST); //TODO archive data required for not LATEST
        if (types == null || types.size() < 3) {
            log.error("Wrong values for " + lpAddress);
            return new Tuple2<>(0.0, 0.0);
        }

        Tuple2<Long, Long> dividers = LpContracts.lpPairsDividers.get(lpAddress);
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

    public BigInteger callErc20TotalSupply(String hash) {
        List<Type> types = web3Service.callMethod(ERC_20_TOTAL_SUPPLY, hash, LATEST);
        if (types == null || types.isEmpty()) {
            log.error("Wrong total supply for " + hash);
            return BigInteger.ZERO;
        }
        return (BigInteger) types.get(0).getValue();
    }

    public BigInteger callUnderlyingUnit(String hash) {
        List<Type> types = web3Service.callMethod(UNDERLYING_UNIT, hash, LATEST);
        if (types == null || types.isEmpty()) {
            log.error("Wrong underlying unit for " + hash);
            return BigInteger.ZERO;
        }
        return (BigInteger) types.get(0).getValue();
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

}
