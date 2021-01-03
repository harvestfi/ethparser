package pro.belbix.ethparser.web3.harvest.parser;

import static pro.belbix.ethparser.web3.uniswap.contracts.Tokens.FARM_TOKEN;
import static pro.belbix.ethparser.web3.ContractConstants.D18;

import java.math.BigInteger;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.dto.ImportantEventsDTO;
import pro.belbix.ethparser.model.ImportantEventsTx;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.harvest.contracts.Vaults;
import pro.belbix.ethparser.web3.harvest.db.ImportantEventsDbService;
import pro.belbix.ethparser.web3.harvest.decoder.ImportantEventsLogDecoder;
import pro.belbix.ethparser.web3.EthBlockService;

@Service
public class ImportantEventsParser implements Web3Parser {

    private static final Logger log = LoggerFactory.getLogger(ImportantEventsParser.class);
    private static final AtomicBoolean run = new AtomicBoolean(true);
    private final BlockingQueue<Log> logs = new ArrayBlockingQueue<>(100);
    private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
    private final ImportantEventsLogDecoder importantEventsLogDecoder = new ImportantEventsLogDecoder();
    private Instant lastTx = Instant.now();

    private final PriceProvider priceProvider;
    private final Web3Service web3Service;
    private final ImportantEventsDbService importantEventsDbService;
    private final ParserInfo parserInfo;
    private final EthBlockService ethBlockService;

    private static final Set<String> allowedMethods = new HashSet<>(
        Arrays.asList("StrategyChanged", "StrategyAnnounced", "Mint")
        );

    public ImportantEventsParser(PriceProvider priceProvider,
                          Web3Service web3Service,
                          ImportantEventsDbService importantEventsDbService, ParserInfo parserInfo,
                          EthBlockService ethBlockService) {
        this.priceProvider = priceProvider;
        this.web3Service = web3Service;
        this.importantEventsDbService = importantEventsDbService;
        this.parserInfo = parserInfo;
        this.ethBlockService = ethBlockService;
    }

    @Override
    public void startParse() {
        log.info("Start parse Important Events logs");
        web3Service.subscribeOnLogs(logs);
        parserInfo.addParser(this);
        new Thread(() -> {
            while (run.get()) {
                Log ethLog = null;
                try {
                    ethLog = logs.poll(1, TimeUnit.SECONDS);                    
                    ImportantEventsDTO dto = parseLog(ethLog);
                    if (dto != null) {
                        lastTx = Instant.now();
                        boolean saved = importantEventsDbService.save(dto);
                        if (saved) {
                            output.put(dto);
                        }
                    }
                } catch (Exception e) {
                    log.error("Can't save " + ethLog, e);
                }
            }
        }).start();
    }

    public ImportantEventsDTO parseLog(Log ethLog) {
        if (ethLog == null || 
        (!FARM_TOKEN.equals(ethLog.getAddress()) && !Vaults.vaultHashToName.containsKey(ethLog.getAddress()))
        ) {
            return null;
        }

        ImportantEventsTx tx;
        try {
            tx = importantEventsLogDecoder.decode(ethLog);
        } catch (Exception e) {
            log.error("Error decode " + ethLog, e);
            return null;
        }
        if (tx == null) {
            return null;
        }
        if (!allowedMethods.contains(tx.getMethodName())) {
            //throw new IllegalStateException("Unknown method " + tx.getMethodName());
            return null;
        }

        ImportantEventsDTO dto = new ImportantEventsDTO();
        dto.setId(tx.getHash() + "_" + tx.getLogId());
        dto.setBlock(tx.getBlock());
        dto.setVault(Vaults.vaultHashToName.get(tx.getVault()));
        dto.setOldStrategy(tx.getOldStrategy());
        dto.setNewStrategy(tx.getNewStrategy());
        dto.setMethodName(tx.getMethodName());
      
        //enrich date
        dto.setBlockDate(
            ethBlockService.getTimestampSecForBlock(ethLog.getBlockHash(), ethLog.getBlockNumber().longValue()));

        parseFee(dto, tx.getHash());
        parseMintAmount(dto, tx.getMintAmount());
        log.info(dto.print());
        return dto;
    }

    private void parseFee(ImportantEventsDTO dto, String hash) {
        Transaction transaction = web3Service.findTransaction(hash);
        TransactionReceipt receipt = web3Service.fetchTransactionReceipt(hash);
        double gas = (receipt.getGasUsed().doubleValue());
        double gasPrice = transaction.getGasPrice().doubleValue() / 1000_000_000 / 1000_000_000;
        double ethPrice = priceProvider.getPriceForCoin("ETH", dto.getBlock());
        double feeUsd = gas * gasPrice * ethPrice;
        dto.setFee(feeUsd);
    }

    private void parseMintAmount(ImportantEventsDTO dto, BigInteger mintAmount) {
        Double _mintAmount;
        if (!mintAmount.equals(BigInteger.ZERO)) {
            _mintAmount = mintAmount.doubleValue() / D18;
            dto.setMintAmount(_mintAmount);    
        }   
        
    }

    @Override
    public BlockingQueue<DtoI> getOutput() {
        return output;
    }

    @Override
    public Instant getLastTx() {
        return lastTx;
    }
}
