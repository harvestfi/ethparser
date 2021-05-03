package pro.belbix.ethparser.web3.prices;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.GET_PRICE;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.D18;

import java.util.Optional;
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
        String oracleAddress;
        if (BSC_NETWORK.equals(network)) {
            Optional<String> factory =
                functionsUtils.callStrByName("factory", tokenAdr, block, network);
            //noinspection OptionalIsPresent
            if (factory.isPresent()) {
                oracleAddress = ContractUtils.getPriceOracleByFactory(factory.get(), network);
            } else {
                oracleAddress = ContractUtils.getPriceOracle(block, network);
            }
        } else {
            oracleAddress = ContractUtils.getPriceOracle(block, network);
        }

        if (oracleAddress == null) {
            throw new IllegalStateException("Not found oracle for " + tokenAdr + " at " + block);
        }
        double price = functionsUtils
            .callIntByNameWithAddressArg(GET_PRICE, tokenAdr, oracleAddress, block, network)
            .orElseThrow(() -> new IllegalStateException(
                "Can't fetch price for " + tokenAdr))
            .doubleValue();

        return price / D18;
    }

    public static boolean isAvailable(long block, String network) {
        return ContractUtils.getPriceOracle(block, network) != null;
    }
}
