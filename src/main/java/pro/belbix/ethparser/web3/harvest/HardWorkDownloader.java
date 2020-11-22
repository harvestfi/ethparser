package pro.belbix.ethparser.web3.harvest;

import static java.util.Collections.singletonList;
import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;
import static pro.belbix.ethparser.web3.harvest.HardWorkParser.CONTROLLER;

import java.math.BigInteger;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.HardWorkDTO;
import pro.belbix.ethparser.repositories.HardWorkRepository;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.Web3Service;

@Service
@SuppressWarnings("rawtypes")
public class HardWorkDownloader {

    private static final int BATCH = 10000;
    private static final Logger logger = LoggerFactory.getLogger(HardWorkDownloader.class);
    private final Web3Service web3Service;
    private final HardWorkDbService hardWorkDbService;
    private final HardWorkParser hardWorkParser;
    private final PriceProvider priceProvider;

    public HardWorkDownloader(Web3Service web3Service,
                              HardWorkDbService hardWorkDbService,
                              HardWorkParser hardWorkParser, PriceProvider priceProvider) {
        this.web3Service = web3Service;
        this.hardWorkDbService = hardWorkDbService;
        this.hardWorkParser = hardWorkParser;
        this.priceProvider = priceProvider;
    }

    public void start() {
        priceProvider.setUpdateTimeout(0);
        Integer from = 10770000;
        Integer to = null;
        while (true) {
            Integer end = null;
            if (to != null) {
                end = from + BATCH;
            }
            parse(from, end);
            from = end;
            if (to != null) {
                if (end > to) {
                    break;
                }
            } else {
                break;
            }
        }
    }

    private void parse(int start, Integer end) {
        DefaultBlockParameter fromBlock = new DefaultBlockParameterNumber(new BigInteger(start + ""));
        DefaultBlockParameter toBlock;
        if (end == null) {
            toBlock = LATEST;
        } else {
            toBlock = new DefaultBlockParameterNumber(new BigInteger(end + ""));
        }
        List<LogResult> logResults = web3Service.fetchContractLogs(singletonList(CONTROLLER), fromBlock, toBlock);
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
