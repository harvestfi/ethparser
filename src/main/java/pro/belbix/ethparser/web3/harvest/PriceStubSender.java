package pro.belbix.ethparser.web3.harvest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.properties.Web3Properties;

@Service
public class PriceStubSender {

    private static final Logger log = LoggerFactory.getLogger(PriceStubSender.class);
    public static final String PRICE_STUB_TYPE = "price_stub";
    private final HarvestVaultParser harvestVaultParser;
    private final Web3Properties web3Properties;

    public PriceStubSender(HarvestVaultParser harvestVaultParser, Web3Properties web3Properties) {
        this.harvestVaultParser = harvestVaultParser;
        this.web3Properties = web3Properties;
    }

    @Scheduled(fixedRate = 60000)
    private void sendStubPrice() throws InterruptedException {
        if (!web3Properties.isStubPrice()) {
            return;
        }
        Log ethLog = new Log();
        ethLog.setType(PRICE_STUB_TYPE);
        harvestVaultParser.getLogs().put(ethLog);
    }

}
