package pro.belbix.ethparser.web3.erc20.downloader;

import static java.util.Collections.singletonList;
import static pro.belbix.ethparser.utils.LoopUtils.handleLoop;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.TransferDTO;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.erc20.Tokens;
import pro.belbix.ethparser.web3.erc20.db.TransferDBService;
import pro.belbix.ethparser.web3.erc20.parser.TransferParser;

@Service
@Log4j2
public class TransferDownloader {

    private final Web3Service web3Service;
    private final PriceProvider priceProvider;
    private final TransferDBService transferDBService;
    private final TransferParser transferParser;

    @Value("${transfer-download.contract:}")
    private String contractName;
    @Value("${transfer-download.from:}")
    private Integer from;
    @Value("${transfer-download.to:}")
    private Integer to;

    public TransferDownloader(Web3Service web3Service,
                              PriceProvider priceProvider,
                              TransferDBService transferDBService,
                              TransferParser transferParser) {
        this.web3Service = web3Service;
        this.priceProvider = priceProvider;
        this.transferDBService = transferDBService;
        this.transferParser = transferParser;
    }

    public void start() {
        if (contractName == null) {
            throw new IllegalStateException("Empty contract");
        }
        priceProvider.setUpdateTimeout(0);
        handleLoop(from, to, (from, end) -> parse(from, end, Tokens.findContractForName(contractName)));
    }

    private void parse(Integer start, Integer end, String contract) {
        List<LogResult> logResults = web3Service.fetchContractLogs(singletonList(contract), start, end);
        if (logResults.isEmpty()) {
            log.info("Empty log {} {}", start, end);
            return;
        }
        for (LogResult logResult : logResults) {
            try {
                TransferDTO dto = transferParser.parseLog((Log) logResult.get());
                if (dto != null) {
                    transferDBService.saveDto(dto);
                }
            } catch (Exception e) {
                log.error("error with " + logResult.get(), e);
                break;
            }
        }
    }

}
