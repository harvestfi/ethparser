package pro.belbix.ethparser.web3.uniswap;

import static pro.belbix.ethparser.model.UniswapTx.SWAP;
import static pro.belbix.ethparser.web3.Web3Service.LOG_LAST_PARSED_COUNT;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.model.DtoI;
import pro.belbix.ethparser.model.UniswapDTO;
import pro.belbix.ethparser.model.UniswapTx;
import pro.belbix.ethparser.repositories.UniswapRepository;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Service;

@Service
public class UniswapTransactionsParser implements Web3Parser {

    private static final Logger log = LoggerFactory.getLogger(UniswapTransactionsParser.class);
    public static final String UNI_ROUTER = "0x7a250d5630b4cf539739df2c5dacb4c659f2488d".toLowerCase();
    public static final String FARM_TOKEN_CONTRACT = "0xa0246c9032bc3a600820415ae600c6388619a14d".toLowerCase();
    public static final String FARM_WETH_UNI_CONTRACT = "0x56feAccb7f750B997B36A68625C7C596F0B41A58".toLowerCase();
    private final UniswapRouterDecoder uniswapRouterDecoder = new UniswapRouterDecoder();
    private final Web3Service web3Service;
    private double lastPrice = 0.0;
    private final static double ETH_PRICE = 390.0; //shortcut for pending transactions
    private long parsedTxCount = 0;
    private final BlockingQueue<Transaction> transactions = new ArrayBlockingQueue<>(10_000);
    private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(10_000);
    private final UniswapPoolDecoder uniswapPoolDecoder = new UniswapPoolDecoder();
    private final UniswapRepository uniswapRepository;
    private final EthBlockService ethBlockService;

    public UniswapTransactionsParser(Web3Service web3Service, UniswapRepository uniswapRepository,
                                     EthBlockService ethBlockService) {
        this.web3Service = web3Service;
        this.uniswapRepository = uniswapRepository;
        this.ethBlockService = ethBlockService;
    }

    public void startParse() {
        log.info("Start parse transactions");
        web3Service.subscribeOn(transactions);
        new Thread(() -> {
            while (true) {
                Transaction transaction = null;
                try {
                    transaction = transactions.take();
                } catch (InterruptedException ignored) {
                }
                UniswapDTO dto = parseUniswapTransaction(transaction);
                if (dto != null) {
                    try {
                        output.put(dto);
                    } catch (InterruptedException e) {
                    }
                    try {
                        uniswapRepository.save(dto);
                    } catch (Exception e) {
                        log.error("Can't save " + dto.toString(), e);
                    }
                }
            }
        }).start();
    }

    UniswapDTO parseUniswapTransaction(Transaction tx) {
        incrementAndPrintCount(tx);
        if (!isValidTransaction(tx)) {
            return null;
        }

        UniswapTx uniswapTx = decodeTransaction(tx);
        if (uniswapTx == null) {
            return null;
        }

        UniswapDTO uniswapDTO = uniswapTx.toDto(FARM_TOKEN_CONTRACT);
        uniswapDTO.setLastGas(web3Service.fetchAverageGasPrice());
        uniswapDTO.setBlockDate(ethBlockService.getTimestampSecForBlock(tx.getBlockHash()));
        calculateNotClearData(uniswapDTO);
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

            if (!uniswapTx.isContainsAddress(FARM_TOKEN_CONTRACT)) {
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

    private void print(UniswapDTO uniswapDTO) {
        if (uniswapDTO.isConfirmed()) {
            log.info(uniswapDTO.print() + " " + lastPrice);
        } else {
            log.debug(uniswapDTO.print() + " " + lastPrice);
        }
    }

    private void calculateNotClearData(UniswapDTO uniswapDTO) {
        if (!uniswapDTO.isConfirmed() && lastPrice != 0.0) {
            if (uniswapDTO.getAmount() == 0.0) {
                if (uniswapDTO.getEthAmount() != 0.0) {
                    uniswapDTO.setAmount((uniswapDTO.getEthAmount() * ETH_PRICE) / lastPrice);
                } else {
                    uniswapDTO.setOtherAmount(uniswapDTO.getAmount() * lastPrice);
                }
            } else if (uniswapDTO.getOtherAmount() == 0.0) {
                if (uniswapDTO.getEthAmount() != 0.0) {
                    uniswapDTO.setOtherAmount(uniswapDTO.getEthAmount() * ETH_PRICE);
                } else {
                    uniswapDTO.setAmount(uniswapDTO.getOtherAmount() / lastPrice);
                }
            }
        }
    }

    private void saveLastPrice(UniswapDTO uniswapDTO) {
        if (uniswapDTO.isConfirmed() && "USDC".equals(uniswapDTO.getOtherCoin())) {
            lastPrice = uniswapDTO.getOtherAmount() / uniswapDTO.getAmount();
            uniswapDTO.setLastPrice(lastPrice);
        }
    }

    public BlockingQueue<DtoI> getOutput() {
        return output;
    }
}
