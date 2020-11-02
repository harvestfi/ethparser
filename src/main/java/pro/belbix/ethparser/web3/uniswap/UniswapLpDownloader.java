package pro.belbix.ethparser.web3.uniswap;

import static pro.belbix.ethparser.web3.uniswap.UniswapLpLogDecoder.FARM_USDC_LP_CONTRACT;
import static pro.belbix.ethparser.web3.uniswap.UniswapTransactionsParser.FARM_TOKEN_CONTRACT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.model.UniswapDTO;
import pro.belbix.ethparser.model.UniswapTx;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.Web3Service;

@Service
public class UniswapLpDownloader {

    private static final Logger logger = LoggerFactory.getLogger(UniswapLpDownloader.class);
    private final Web3Service web3Service;
    private final EthBlockService ethBlockService;
    private final UniswapDbService saveHarvestDTO;
    private UniswapLpLogDecoder uniswapLpLogDecoder = new UniswapLpLogDecoder();

    public UniswapLpDownloader(Web3Service web3Service, EthBlockService ethBlockService,
                               UniswapDbService saveHarvestDTO) {
        this.web3Service = web3Service;
        this.ethBlockService = ethBlockService;
        this.saveHarvestDTO = saveHarvestDTO;
    }

    public void load(DefaultBlockParameter from, DefaultBlockParameter to) {
        Map<String, Integer> topics = new HashMap<>();
        List<LogResult> logResults = web3Service.fetchContractLogs(FARM_USDC_LP_CONTRACT, from, to);
        if (logResults == null) {
            logger.error("Log results is null");
            return;
        }

        for (LogResult logResult : logResults) {
            Log log = (Log) logResult.get();

            String topic0 = log.getTopics().get(0);
            if (topics.containsKey(topic0)) {
                topics.put(topic0, topics.get(topic0) + 1);
            } else {
                topics.put(topic0, 1);
            }
            UniswapTx tx = new UniswapTx();
            try {
                uniswapLpLogDecoder.enrichFromLog(tx, log);
                if (tx.getHash() == null) {
                    continue;
                }
            } catch (Exception e) {
                logger.info("error with " + e.getMessage() + " " + log, e);
                continue;
            }

            UniswapDTO dto = tx.toDto(FARM_TOKEN_CONTRACT);
            logger.info(dto.print());

            //enrich owner
            TransactionReceipt receipt = web3Service.fetchTransactionReceipt(dto.getHash());
            dto.setLastGas(receipt.getGasUsed().doubleValue());
            dto.setOwner(receipt.getFrom());

            //enrich date
            dto.setBlockDate(ethBlockService.getTimestampSecForBlock(log.getBlockHash()));

            saveHarvestDTO.saveUniswapDto(dto);
        }

        topics.forEach((key, value) -> logger.info(key + " " + value));
    }

}
