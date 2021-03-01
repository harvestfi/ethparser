package pro.belbix.ethparser.web3.harvest.downloader;

import static java.util.Collections.singletonList;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.utils.LoopUtils;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.harvest.HarvestOwnerBalanceCalculator;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;
import pro.belbix.ethparser.web3.harvest.parser.HarvestVaultParserV2;

@SuppressWarnings("rawtypes")
@Service
public class HarvestVaultDownloader {

    private static final Logger logger = LoggerFactory.getLogger(HarvestVaultDownloader.class);
    private final Web3Service web3Service;
    private final HarvestDBService harvestDBService;
    private final HarvestVaultParserV2 harvestVaultParserV2;
    private final HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator;

    @Value("${harvest-download.contract:}")
    private String contractName;
    @Value("${harvest-download.from:}")
    private Integer from;
    @Value("${harvest-download.to:}")
    private Integer to;

    public HarvestVaultDownloader(Web3Service web3Service,
                                  HarvestDBService harvestDBService,
                                  HarvestVaultParserV2 harvestVaultParserV2,
                                  HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator) {
        this.web3Service = web3Service;
        this.harvestDBService = harvestDBService;
        this.harvestVaultParserV2 = harvestVaultParserV2;
        this.harvestOwnerBalanceCalculator = harvestOwnerBalanceCalculator;
    }

    public void start() {
        for (String vaultAddress : ContractUtils.getAllVaultAddresses()) {
            if (contractName != null && !contractName.isEmpty()
                && !contractName.equalsIgnoreCase(ContractUtils.getNameByAddress(vaultAddress).orElse(""))) {
                continue;
            }

            LoopUtils.handleLoop(from, to, (start, end) -> parse(vaultAddress, start, end));
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
                HarvestDTO dto = harvestVaultParserV2.parseVaultLog((Log) logResult.get());
                if (dto != null) {
                    harvestOwnerBalanceCalculator.fillBalance(dto);
                    harvestDBService.saveHarvestDTO(dto);
                }
            } catch (Exception e) {
                logger.error("error with " + logResult.get(), e);
            }
        }
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public void setTo(Integer to) {
        this.to = to;
    }
}
