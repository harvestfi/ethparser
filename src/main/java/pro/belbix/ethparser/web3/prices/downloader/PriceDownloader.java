package pro.belbix.ethparser.web3.prices.downloader;

import static java.util.Collections.singletonList;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.PriceDTO;
import pro.belbix.ethparser.utils.LoopUtils;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.prices.db.PriceDBService;
import pro.belbix.ethparser.web3.prices.parser.PriceLogParser;
import pro.belbix.ethparser.web3.uniswap.contracts.LpContracts;

@Service
@Log4j2
@SuppressWarnings("rawtypes")
public class PriceDownloader {

    private final Web3Service web3Service;
    private final PriceDBService priceDBService;
    private final PriceLogParser priceLogParser;

    @Value("${price-download.contracts:}")
    private String[] contractNames;
    @Value("${price-download.from:}")
    private Integer from;
    @Value("${price-download.to:}")
    private Integer to;

    public PriceDownloader(Web3Service web3Service, PriceDBService priceDBService,
                           PriceLogParser priceLogParser) {
        this.web3Service = web3Service;
        this.priceDBService = priceDBService;
        this.priceLogParser = priceLogParser;
    }

    public void start() {
        if (contractNames.length == 0) {
            contractNames = LpContracts.keyCoinForLp.keySet().stream()
                .map(LpContracts.lpHashToName::get)
                .collect(Collectors.toSet())
                .toArray(contractNames);
        }
        for (String contractName : contractNames) {
            String contractHash = LpContracts.lpNameToHash.get(contractName);
            LoopUtils.handleLoop(from, to, (start, end) -> parse(start, end, contractHash));
        }
    }

    private void parse(Integer start, Integer end, String contractName) {
        List<LogResult> logResults = web3Service.fetchContractLogs(singletonList(contractName), start, end);
        if (logResults.isEmpty()) {
            log.info("Empty log {} {}", start, end);
            return;
        }
        for (LogResult logResult : logResults) {
            try {
                PriceDTO dto = priceLogParser.parse((Log) logResult.get());
                if (dto != null) {
                    boolean success = priceDBService.savePriceDto(dto);
                    if (success) {
                        log.info("Save " + dto);
                    }
                }
            } catch (Exception e) {
                log.error("error with " + logResult.get(), e);
                break;
            }
        }
    }
}
