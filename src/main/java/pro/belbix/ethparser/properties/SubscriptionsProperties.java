package pro.belbix.ethparser.properties;

import static pro.belbix.ethparser.web3.harvest.parser.HardWorkParser.CONTROLLER;
import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.UNI_LP_GRAIN_FARM;
import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.UNI_LP_USDC_FARM;
import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.UNI_LP_WETH_FARM;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import pro.belbix.ethparser.web3.harvest.contracts.StakeContracts;
import pro.belbix.ethparser.web3.harvest.contracts.Vaults;

@Validated
@ConfigurationProperties(prefix = "subscription")
public class SubscriptionsProperties {

    private List<String> logSubscriptions = addAllContracts();

    private static List<String> addAllContracts() {
        List<String> contracts = new ArrayList<>();

        //FARM prices
        contracts.add(UNI_LP_USDC_FARM);
        contracts.add(UNI_LP_WETH_FARM);
        contracts.add(UNI_LP_GRAIN_FARM);

        //hard work parsing
        contracts.add(CONTROLLER);

        //harvest events
        contracts.addAll(Vaults.vaultNames.keySet());
        contracts.addAll(StakeContracts.hashToName.keySet());
        return contracts;
    }

    public List<String> getLogSubscriptions() {
        return logSubscriptions;
    }

    public void setLogSubscriptions(List<String> logSubscriptions) {
        this.logSubscriptions = logSubscriptions;
    }

}
