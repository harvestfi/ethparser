package pro.belbix.ethparser.web3.uniswap.downloader;

import static java.util.Collections.singletonList;
import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.lpNameToHash;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.utils.LoopUtils;
import pro.belbix.ethparser.web3.uniswap.db.UniswapDbService;
import pro.belbix.ethparser.web3.uniswap.parser.UniswapLpLogParser;

@SuppressWarnings("rawtypes")
@Service
public class UniswapLpDownloader {

    private static final Logger logger = LoggerFactory.getLogger(UniswapLpDownloader.class);
    private final Web3Service web3Service;
    private final UniswapDbService saveHarvestDTO;
    private final UniswapLpLogParser uniswapLpLogParser;

    @Value("${uniswap-download.contract:}")
    private String contractName;
    @Value("${uniswap-download.from:}")
    private Integer from;
    @Value("${uniswap-download.to:}")
    private Integer to;

    public UniswapLpDownloader(Web3Service web3Service,
                               UniswapDbService saveHarvestDTO,
                               UniswapLpLogParser uniswapLpLogParser) {
        this.web3Service = web3Service;
        this.saveHarvestDTO = saveHarvestDTO;
        this.uniswapLpLogParser = uniswapLpLogParser;
    }

    public void start() {
        LoopUtils.handleLoop(from, to, this::load);
    }

    private void load(Integer from, Integer to) {
        List<LogResult> logResults = web3Service
            .fetchContractLogs(singletonList(lpNameToHash.get(contractName)), from, to);
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
