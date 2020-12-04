package pro.belbix.ethparser.web3.uniswap;

import static java.util.Collections.singletonList;
import static pro.belbix.ethparser.web3.uniswap.LpContracts.UNI_LP_WBTC_BADGER;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.harvest.utils.LoopUtils;

@SuppressWarnings("rawtypes")
@Service
public class UniswapLpDownloader {

    private static final Logger logger = LoggerFactory.getLogger(UniswapLpDownloader.class);
    private final Web3Service web3Service;
    private final UniswapDbService saveHarvestDTO;
    private final UniswapLpLogParser uniswapLpLogParser;

    public UniswapLpDownloader(Web3Service web3Service, UniswapDbService saveHarvestDTO,
                               UniswapLpLogParser uniswapLpLogParser) {
        this.web3Service = web3Service;
        this.saveHarvestDTO = saveHarvestDTO;
        this.uniswapLpLogParser = uniswapLpLogParser;
    }

    public void start() {
        LoopUtils.handleLoop(11381099, 11386849, this::load);
    }

    private void load(Integer from, Integer to) {
        List<LogResult> logResults = web3Service.fetchContractLogs(singletonList(UNI_LP_WBTC_BADGER), from, to);
        if (logResults == null) {
            logger.error("Log results is null");
            return;
        }
        for (LogResult logResult : logResults) {
            UniswapDTO dto = null;
            try {
                dto = uniswapLpLogParser.parseUniswapLog((Log) logResult.get());
                if (dto != null) {
                    saveHarvestDTO.saveUniswapDto(dto);
                }
            } catch (Exception e) {
                logger.info("Downloader error  " + dto, e);
            }
        }
    }

}
