package pro.belbix.ethparser.web3.uniswap.parser;

import static pro.belbix.ethparser.model.UniswapTx.SWAP;
import static pro.belbix.ethparser.web3.Web3Service.LOG_LAST_PARSED_COUNT;
import static pro.belbix.ethparser.web3.contracts.Tokens.FARM_TOKEN;

import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.model.UniswapTx;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.uniswap.db.UniswapDbService;
import pro.belbix.ethparser.web3.uniswap.decoder.UniswapPoolDecoder;
import pro.belbix.ethparser.web3.uniswap.decoder.UniswapRouterDecoder;

@Service
@Log4j2
public class UniswapTransactionsParser implements Web3Parser {

    public static final String UNI_ROUTER = "0x7a250d5630b4cf539739df2c5dacb4c659f2488d".toLowerCase();
    private static final AtomicBoolean run = new AtomicBoolean(true);
    private final UniswapRouterDecoder uniswapRouterDecoder = new UniswapRouterDecoder();
    private final Web3Service web3Service;
    private final BlockingQueue<Transaction> transactions = new ArrayBlockingQueue<>(100);
    private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
    private final UniswapPoolDecoder uniswapPoolDecoder = new UniswapPoolDecoder();
    private final UniswapDbService uniswapDbService;
    private final EthBlockService ethBlockService;
    private final ParserInfo parserInfo;
    private final AppProperties appProperties;
    private double lastPrice = 0.0;
    private long parsedTxCount = 0;
    private Instant lastTx = Instant.now();

    public UniswapTransactionsParser(Web3Service web3Service, UniswapDbService uniswapDbService,
                                     EthBlockService ethBlockService, ParserInfo parserInfo,
                                     AppProperties appProperties) {
        this.web3Service = web3Service;
        this.uniswapDbService = uniswapDbService;
        this.ethBlockService = ethBlockService;
        this.parserInfo = parserInfo;
        this.appProperties = appProperties;
    }

    public void startParse() {
        log.info("Start parse Uniswap transactions");
        parserInfo.addParser(this);
        web3Service.subscribeOnTransactions(transactions);
        new Thread(() -> {
            while (run.get()) {
                Transaction transaction = null;
                try {
                    transaction = transactions.poll(1, TimeUnit.SECONDS);
                } catch (InterruptedException ignored) {
                }
                UniswapDTO dto = parseUniswapTransaction(transaction);
                if (dto != null) {
                    lastTx = Instant.now();
                    try {
                        boolean success = uniswapDbService.saveUniswapDto(dto);
                        if (success) {
                            output.put(dto);
                        }
                    } catch (Exception e) {
                        log.error("Can't save " + dto.toString(), e);
                        if(appProperties.isStopOnParseError()) {
                            System.exit(-1);
                        }
                    }
                }
            }
        }).start();
    }

    public UniswapDTO parseUniswapTransaction(Transaction tx) {
        incrementAndPrintCount(tx);
        if (!isValidTransaction(tx)) {
            return null;
        }

        UniswapTx uniswapTx = decodeTransaction(tx);
        if (uniswapTx == null) {
            return null;
        }

        UniswapDTO uniswapDTO = uniswapTx.toDto();
        uniswapDTO.setLastGas(web3Service.fetchAverageGasPrice());
        uniswapDTO
            .setBlockDate(ethBlockService.getTimestampSecForBlock(tx.getBlockHash(), tx.getBlockNumber().longValue()));
//        calculateNotClearData(uniswapDTO);
        saveLastPrice(uniswapDTO);
        print(uniswapDTO);
        return uniswapDTO;
    }

    private void incrementAndPrintCount(Transaction tx) {
        parsedTxCount++;
        if (parsedTxCount % LOG_LAST_PARSED_COUNT == 0) {
            log.info("Uniswap parsed " + parsedTxCount + ", last block: " + tx.getBlockNumber());
        }
    }

    private boolean isValidTransaction(Transaction tx) {
        if (tx == null) {
            log.error("Null clear tx!");
            return false;
        }
        if (tx.getTo() == null) {
            //it is contract deploy
            return false;
        }
        return UNI_ROUTER.equals(tx.getTo().toLowerCase());
    }

    private UniswapTx decodeTransaction(Transaction tx) {
        UniswapTx uniswapTx;
        try {
            uniswapTx = (UniswapTx) uniswapRouterDecoder.decodeInputData(tx);

            if (uniswapTx == null) {
                if (tx.getInput().length() > 70) {
                    log.error("tx not parsed " + tx.getHash());
                }
                return null;
            }

            if (!uniswapTx.isContainsAddress(FARM_TOKEN)) {
                return null;
            }
            TransactionReceipt transactionReceipt = web3Service.fetchTransactionReceipt(tx.getHash());
            if ("0x1".equals(transactionReceipt.getStatus())) {
                uniswapTx.setSuccess(true);
                if (SWAP.equals(uniswapTx.getType())) {
                    uniswapPoolDecoder.enrichUniTx(uniswapTx, transactionReceipt.getLogs());
                }
            }

        } catch (Exception e) {
            log.error("Error tx " + tx.getHash(), e);
            return null;
        }
        return uniswapTx;
    }

    private void saveLastPrice(UniswapDTO uniswapDTO) {
        if (uniswapDTO.isConfirmed() && "USDC".equals(uniswapDTO.getOtherCoin())) {
            lastPrice = uniswapDTO.getOtherAmount() / uniswapDTO.getAmount();
            uniswapDTO.setPrice(lastPrice);
        }
    }

//    private void calculateNotClearData(UniswapDTO uniswapDTO) {
//        if (!uniswapDTO.isConfirmed() && lastPrice != 0.0) {
//            if (uniswapDTO.getAmount() == 0.0) {
//                if (uniswapDTO.getEthAmount() != 0.0) {
//                    uniswapDTO.setAmount((uniswapDTO.getEthAmount() * ETH_PRICE) / lastPrice);
//                } else {
//                    uniswapDTO.setOtherAmount(uniswapDTO.getAmount() * lastPrice);
//                }
//            } else if (uniswapDTO.getOtherAmount() == 0.0) {
//                if (uniswapDTO.getEthAmount() != 0.0) {
//                    uniswapDTO.setOtherAmount(uniswapDTO.getEthAmount() * ETH_PRICE);
//                } else {
//                    uniswapDTO.setAmount(uniswapDTO.getOtherAmount() / lastPrice);
//                }
//            }
//        }
//    }

    private void print(UniswapDTO uniswapDTO) {
        if (uniswapDTO.isConfirmed()) {
            log.info(uniswapDTO.print() + " " + lastPrice);
        } else {
            log.debug(uniswapDTO.print() + " " + lastPrice);
        }
    }

    public BlockingQueue<DtoI> getOutput() {
        return output;
    }

    @PreDestroy
    public void stop() {
        run.set(false);
    }

    @Override
    public Instant getLastTx() {
        return lastTx;
    }
}
