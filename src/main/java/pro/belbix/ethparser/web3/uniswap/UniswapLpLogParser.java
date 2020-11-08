package pro.belbix.ethparser.web3.uniswap;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.model.UniswapTx;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Service;

@Service
public class UniswapLpLogParser implements Web3Parser {

    private static final Logger log = LoggerFactory.getLogger(UniswapLpLogParser.class);
    private static final AtomicBoolean run = new AtomicBoolean(true);
    public static final String UNI_ROUTER = "0x7a250d5630b4cf539739df2c5dacb4c659f2488d".toLowerCase();
    public static final String FARM_TOKEN_CONTRACT = "0xa0246c9032bc3a600820415ae600c6388619a14d".toLowerCase();
    public static final String FARM_WETH_UNI_CONTRACT = "0x56feAccb7f750B997B36A68625C7C596F0B41A58".toLowerCase();
    private final UniswapLpLogDecoder uniswapLpLogDecoder = new UniswapLpLogDecoder();
    private final Web3Service web3Service;
    private long parsedTxCount = 0;
    private final BlockingQueue<Log> logs = new ArrayBlockingQueue<>(10_000);
    private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(10_000);
    private final UniswapDbService uniswapDbService;
    private final EthBlockService ethBlockService;

    public UniswapLpLogParser(Web3Service web3Service, UniswapDbService uniswapDbService,
                              EthBlockService ethBlockService) {
        this.web3Service = web3Service;
        this.uniswapDbService = uniswapDbService;
        this.ethBlockService = ethBlockService;
    }

    @Override
    public void startParse() {
        log.info("Start parse Uniswap logs");
        web3Service.subscribeOnLogs(logs);
        new Thread(() -> {
            while (run.get()) {
                Log log = null;
                try {
                    log = logs.poll(1, TimeUnit.SECONDS);
                    UniswapDTO dto = parseUniswapLog(log);
                    if (dto != null) {
                        enrichDto(dto);
                        boolean success = uniswapDbService.saveUniswapDto(dto);
                        if (success) {
                            output.put(dto);
                        }
                    }
                } catch (Exception e) {
                    UniswapLpLogParser.log.error("Error uniswap parser loop " + log, e);
                }
            }
        }).start();
    }

    public UniswapDTO parseUniswapLog(Log ethLog) {
        UniswapTx tx = new UniswapTx();
        uniswapLpLogDecoder.decode(tx, ethLog);
        if (tx.getHash() == null) {
            return null;
        }

        UniswapDTO dto = tx.toDto(FARM_TOKEN_CONTRACT);

        //enrich owner
        TransactionReceipt receipt = web3Service.fetchTransactionReceipt(dto.getHash());
        dto.setOwner(receipt.getFrom());

        //enrich date
        dto.setBlockDate(ethBlockService.getTimestampSecForBlock(ethLog.getBlockHash(), ethLog.getBlockNumber().longValue()));

        log.info(dto.print());

        return dto;
    }

    private void enrichDto(UniswapDTO dto) {
        dto.setLastGas(web3Service.fetchAverageGasPrice());
    }

    @Override
    public BlockingQueue<DtoI> getOutput() {
        return output;
    }

    @PreDestroy
    public void stop() {
        run.set(false);
    }
}
