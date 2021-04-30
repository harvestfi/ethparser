package pro.belbix.ethparser.web3.prices;

import static pro.belbix.ethparser.web3.abi.FunctionsNames.GET_PRICE;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.D18;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractUtils;

@Service
@Log4j2
public class PriceOracle {

    private final FunctionsUtils functionsUtils;
    private final AppProperties appProperties;

    public PriceOracle(FunctionsUtils functionsUtils,
                         AppProperties appProperties) {
        this.functionsUtils = functionsUtils;
        this.appProperties = appProperties;

    }

    public double getPriceForCoinOnChain(String tokenAdr, Long block, String network) {
        if (appProperties.isOnlyApi()) {
            return 0.0;
        }
        String oracleAddress = ContractUtils.getPriceOracle(block, network);
        if (oracleAddress == null) {
            throw new IllegalStateException("Oracle price smart contract  not deployed yet");
        }
        double price = functionsUtils
            .callIntByName(GET_PRICE, tokenAdr, oracleAddress, block, network)
            .orElseThrow(() -> new IllegalStateException(
                "Can't fetch price for " + tokenAdr))
            .doubleValue();

        return price / D18;
    }

    public static boolean isAvailable(long block, String network) {
        return ContractUtils.getPriceOracle(block, network) != null;
    }
}
