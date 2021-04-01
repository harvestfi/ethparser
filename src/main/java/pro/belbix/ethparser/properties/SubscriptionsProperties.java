package pro.belbix.ethparser.properties;

import static pro.belbix.ethparser.web3.contracts.EthContractConstants.ETH_CONTROLLER;
import static pro.belbix.ethparser.web3.contracts.EthContractConstants.PARSABLE_UNI_PAIRS;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import pro.belbix.ethparser.web3.contracts.EthContractConstants;
import pro.belbix.ethparser.web3.contracts.ContractUtils;

@Validated
@ConfigurationProperties(prefix = "subscription")
@Data
@Log4j2
public class SubscriptionsProperties {

    private List<String> logSubscriptions;

    public void init() {
        // if filled up from app config skip default values
        if (logSubscriptions != null && !logSubscriptions.isEmpty()) {
            log.info("Subscriptions already filled up");
            return;
        }

        Set<String> contracts = new HashSet<>();

        // hard work parsing
        contracts.add(ETH_CONTROLLER);

        // FARM token Mint event parsing + transfers parsing
        contracts.add(EthContractConstants.FARM_TOKEN);

        // harvest events
        contracts.addAll(ContractUtils.getAllVaultAddresses());
        contracts.addAll(ContractUtils.getAllPoolAddresses());
        // price parsing
        contracts.addAll(ContractUtils.getAllUniPairAddressesWithKeys());
        // uni events
        contracts.addAll(PARSABLE_UNI_PAIRS);

        logSubscriptions = new ArrayList<>(contracts);
    }

}
