package pro.belbix.ethparser.web3.harvest.downloader;

import static java.util.Collections.singletonList;
import static pro.belbix.ethparser.web3.harvest.parser.HardWorkParser.CONTROLLER;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.HardWorkDTO;
import pro.belbix.ethparser.utils.LoopUtils;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.harvest.db.HardWorkDbService;
import pro.belbix.ethparser.web3.harvest.parser.HardWorkParser;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@SuppressWarnings("rawtypes")
@Log4j2
public class HardWorkDownloader {

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
        log.info("HardWorkDownloader start");
        priceProvider.setUpdateBlockDifference(1);
        LoopUtils.handleLoop(from, to, this::parse);

    }

    private void parse(Integer start, Integer end) {
        List<LogResult> logResults = web3Service.fetchContractLogs(singletonList(CONTROLLER), start, end);
        if (logResults.isEmpty()) {
            log.info("Empty log {} {}", start, end);
            return;
        }
        for (LogResult logResult : logResults) {
            try {
                HardWorkDTO dto = hardWorkParser.parseLog((Log) logResult.get());
                if (dto != null) {
                    hardWorkDbService.save(dto);
                }
            } catch (Exception e) {
                log.error("error with " + logResult.get(), e);
                break;
            }
        }
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public void setTo(Integer to) {
        this.to = to;
    }
}
