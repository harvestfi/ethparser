package pro.belbix.ethparser.web3.uniswap;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.model.DtoI;
import pro.belbix.ethparser.model.UniswapDTO;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Service;

@Service
public class UniswapLpLogParser implements Web3Parser {

    private static final Logger log = LoggerFactory.getLogger(UniswapLpLogParser.class);
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
            while (true) {
                Log log = null;
                try {
                    log = logs.take();
                } catch (InterruptedException ignored) {
                }
                UniswapDTO dto = parseUniswapLog(log);
                if (dto != null) {
                    try {
                        boolean success = uniswapDbService.saveUniswapDto(dto);
                        if(success) {
                            output.put(dto);
                        }
                    } catch (Exception e) {
                        UniswapLpLogParser.log.error("Can't save " + dto.toString(), e);
                    }
                }
            }
        }).start();
    }

    private UniswapDTO parseUniswapLog(Log log) {

        return null;
    }

    @Override
    public BlockingQueue<DtoI> getOutput() {
        return output;
    }
}
