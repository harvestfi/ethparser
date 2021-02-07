package pro.belbix.ethparser.properties;

import static pro.belbix.ethparser.web3.contracts.ContractConstants.CONTROLLER;
import static pro.belbix.ethparser.web3.contracts.LpContracts.UNI_LP_GRAIN_FARM;
import static pro.belbix.ethparser.web3.contracts.LpContracts.UNI_LP_USDC_FARM;
import static pro.belbix.ethparser.web3.contracts.LpContracts.UNI_LP_WETH_FARM;
import static pro.belbix.ethparser.web3.contracts.Tokens.FARM_TOKEN;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.contracts.LpContracts;

@Validated
@ConfigurationProperties(prefix = "subscription")
@Data
public class SubscriptionsProperties {

    private List<String> logSubscriptions;

    public void init() {
        // if filled up from app config skip default values
        if (logSubscriptions != null && !logSubscriptions.isEmpty()) {
            return;
        }

        Set<String> contracts = new HashSet<>();

        //FARM prices
        contracts.add(UNI_LP_USDC_FARM);
        contracts.add(UNI_LP_WETH_FARM);
        contracts.add(UNI_LP_GRAIN_FARM);

        //hard work parsing
        contracts.add(CONTROLLER);

        //transfers parsing
        contracts.add(FARM_TOKEN);

        //harvest events
        contracts.addAll(ContractUtils.getAllVaultAddresses());
        contracts.addAll(ContractUtils.getAllPoolAddresses());

        // FARM token Mint event parsing
        contracts.add(FARM_TOKEN);

        // contracts for price parsing
        contracts.addAll(LpContracts.keyCoinForLp.keySet());

        logSubscriptions = new ArrayList<>(contracts);
    }

}
