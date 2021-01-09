package pro.belbix.ethparser.web3.harvest.downloader;

import static java.util.Collections.singletonList;
import static pro.belbix.ethparser.web3.harvest.parser.HardWorkParser.CONTROLLER;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.HardWorkDTO;
import pro.belbix.ethparser.utils.LoopUtils;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.harvest.db.HardWorkDbService;
import pro.belbix.ethparser.web3.harvest.parser.HardWorkParser;

@Service
@SuppressWarnings("rawtypes")
public class HardWorkDownloader {

    private static final int BATCH = 10000;
    private static final Logger logger = LoggerFactory.getLogger(HardWorkDownloader.class);
    private final Web3Service web3Service;
    private final HardWorkDbService hardWorkDbService;
    private final HardWorkParser hardWorkParser;
    private final PriceProvider priceProvider;

    @Value("${hardwork-download.from:}")
    private Integer from;
    @Value("${hardwork-download.to:}")
    private Integer to;

    public HardWorkDownloader(Web3Service web3Service,
                              HardWorkDbService hardWorkDbService,
                              HardWorkParser hardWorkParser, PriceProvider priceProvider) {
        this.web3Service = web3Service;
        this.hardWorkDbService = hardWorkDbService;
        this.hardWorkParser = hardWorkParser;
        this.priceProvider = priceProvider;
    }

    public void start() {
        priceProvider.setUpdateBlockDifference(1);
        LoopUtils.handleLoop(from, to, this::parse);

    }

    private void parse(Integer start, Integer end) {
        List<LogResult> logResults = web3Service.fetchContractLogs(singletonList(CONTROLLER), start, end);
        if (logResults.isEmpty()) {
            logger.info("Empty log {} {}", start, end);
            return;
        }
        for (LogResult logResult : logResults) {
            try {
                HardWorkDTO dto = hardWorkParser.parseLog((Log) logResult.get());
                if (dto != null) {
                    hardWorkDbService.save(dto);
                }
            } catch (Exception e) {
                logger.error("error with " + logResult.get(), e);
                break;
            }
        }
    }


}
