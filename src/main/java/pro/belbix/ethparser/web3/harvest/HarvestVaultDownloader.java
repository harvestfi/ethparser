package pro.belbix.ethparser.web3.harvest;

import static java.util.Collections.singletonList;
import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.model.HarvestDTO;
import pro.belbix.ethparser.web3.Web3Service;

@SuppressWarnings("rawtypes")
@Service
public class HarvestVaultDownloader {

    private static final Logger logger = LoggerFactory.getLogger(HarvestVaultDownloader.class);
    private final Web3Service web3Service;
    private final HarvestDBService harvestDBService;
    private final HarvestVaultParser harvestVaultParser;

    public HarvestVaultDownloader(Web3Service web3Service, HarvestDBService harvestDBService,
                                  HarvestVaultParser harvestVaultParser) {
        this.web3Service = web3Service;
        this.harvestDBService = harvestDBService;
        this.harvestVaultParser = harvestVaultParser;
    }

    public void parseVault(String vaultHash, DefaultBlockParameter fromBlock) {
        if (fromBlock == null) {
            BigInteger lastBlock = harvestDBService.lastBlock();
            fromBlock = DefaultBlockParameter.valueOf(lastBlock);
            logger.info("Use last block " + lastBlock);
        }

        List<LogResult> logResults = web3Service.fetchContractLogs(singletonList(vaultHash), fromBlock, LATEST);
        if (logResults.isEmpty()) {
            logger.error("Empty log");
            return;
        }
        for (LogResult logResult : logResults) {
            HarvestDTO dto = harvestVaultParser.parseVaultLog((Log) logResult.get());
            if (dto != null) {
                harvestDBService.saveHarvestDTO(dto);
            }

        }
    }


}
