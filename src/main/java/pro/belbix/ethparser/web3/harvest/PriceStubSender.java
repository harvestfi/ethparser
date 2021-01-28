package pro.belbix.ethparser.web3.harvest;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.harvest.parser.HarvestVaultParserV2;

@Service
public class PriceStubSender {

    public static final String PRICE_STUB_TYPE = "price_stub";
    private final HarvestVaultParserV2 harvestVaultParser;
    private final AppProperties appProperties;

    public PriceStubSender(HarvestVaultParserV2 harvestVaultParser, AppProperties appProperties) {
        this.harvestVaultParser = harvestVaultParser;
        this.appProperties = appProperties;
    }

    @Scheduled(fixedRate = 60000)
    private void sendStubPrice() throws InterruptedException {
        if (!appProperties.isStubPrice()) {
            return;
        }
        Log ethLog = new Log();
        ethLog.setType(PRICE_STUB_TYPE);
        harvestVaultParser.getLogs().put(ethLog);
    }

}
