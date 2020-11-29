package pro.belbix.ethparser.web3.harvest.utils;

import static java.util.Collections.singletonList;
import static pro.belbix.ethparser.web3.harvest.utils.LoopUtils.handleLoop;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.RewardDTO;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.harvest.contracts.RewardVaults;
import pro.belbix.ethparser.web3.harvest.db.RewardsDBService;
import pro.belbix.ethparser.web3.harvest.parser.RewardParser;

@Service
@SuppressWarnings("rawtypes")
public class RewardDownloader {

    private static final int BATCH = 10000;
    private static final Logger logger = LoggerFactory.getLogger(HardWorkDownloader.class);
    private final Web3Service web3Service;
    private final RewardParser rewardParser;
    private final PriceProvider priceProvider;
    private final RewardsDBService rewardsDBService;

    public RewardDownloader(Web3Service web3Service,
                            RewardParser rewardParser,
                            PriceProvider priceProvider,
                            RewardsDBService rewardsDBService) {
        this.web3Service = web3Service;
        this.rewardParser = rewardParser;
        this.priceProvider = priceProvider;
        this.rewardsDBService = rewardsDBService;
    }

    public void start() {
        priceProvider.setUpdateTimeout(0);
        for (String contract : RewardVaults.hashToName.keySet()) {
            if (contract == null || contract.isEmpty()) {
                continue;
            }
            handleLoop(10770000, null, (from, end) -> parse(from, end, contract));
        }
    }

    private void parse(int start, Integer end, String contract) {
        List<LogResult> logResults = web3Service.fetchContractLogs(singletonList(contract), start, end);
        if (logResults.isEmpty()) {
            logger.info("Empty log {} {}", start, end);
            return;
        }
        for (LogResult logResult : logResults) {
            try {
                RewardDTO dto = rewardParser.parseLog((Log) logResult.get());
                if (dto != null) {
                    rewardsDBService.saveRewardDTO(dto);
                }
            } catch (Exception e) {
                logger.error("error with " + logResult.get(), e);
                break;
            }
        }
    }

}
