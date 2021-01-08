package pro.belbix.ethparser.web3.harvest.downloader;

import static java.util.Collections.singletonList;

import java.util.List;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.utils.LoopUtils;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.harvest.contracts.Vaults;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;
import pro.belbix.ethparser.web3.harvest.parser.HarvestVaultParserV2;

@SuppressWarnings("rawtypes")
@Service
public class HarvestVaultDownloader {

    private static final Logger logger = LoggerFactory.getLogger(HarvestVaultDownloader.class);
    private final Web3Service web3Service;
    private final HarvestDBService harvestDBService;
    private final HarvestVaultParserV2 harvestVaultParser;
    private final PriceProvider priceProvider;

    @Value("${harvest-download.contract:}")
    private String contractName;
    @Value("${harvest-download.from:}")
    private Integer from;
    @Value("${harvest-download.to:}")
    private Integer to;

    public HarvestVaultDownloader(Web3Service web3Service, HarvestDBService harvestDBService,
                                  HarvestVaultParserV2 harvestVaultParser,
                                  PriceProvider priceProvider) {
        this.web3Service = web3Service;
        this.harvestDBService = harvestDBService;
        this.harvestVaultParser = harvestVaultParser;
        this.priceProvider = priceProvider;
    }

    public void start() {
        for (Entry<String, String> entry : Vaults.vaultHashToName.entrySet()) {
            if (contractName != null && !contractName.isEmpty() && !contractName.equals(entry.getValue())) {
                continue;
            }

            LoopUtils.handleLoop(from, to, (start, end) -> parse(entry.getKey(), start, end));
        }
    }

    private void parse(String vaultHash, Integer start, Integer end) {
        List<LogResult> logResults = web3Service.fetchContractLogs(singletonList(vaultHash), start, end);
        if (logResults.isEmpty()) {
            logger.info("Empty log {} {} {}", start, end, vaultHash);
            return;
        }
        for (LogResult logResult : logResults) {
            try {
                HarvestDTO dto = harvestVaultParser.parseVaultLog((Log) logResult.get());
                if (dto != null) {
                    dto.setPrices(priceProvider.getAllPrices(dto.getBlock().longValue()));
                    harvestDBService.saveHarvestDTO(dto);
                }
            } catch (Exception e) {
                logger.error("error with " + logResult.get(), e);
            }
        }
    }


}
