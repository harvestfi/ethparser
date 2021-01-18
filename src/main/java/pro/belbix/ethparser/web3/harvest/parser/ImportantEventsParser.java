package pro.belbix.ethparser.web3.harvest.parser;

import static pro.belbix.ethparser.web3.ContractConstants.D18;
import static pro.belbix.ethparser.web3.erc20.Tokens.FARM_TOKEN;

import java.math.BigInteger;
import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.dto.ImportantEventsDTO;
import pro.belbix.ethparser.model.ImportantEventsTx;
import pro.belbix.ethparser.model.ImportantEventsInfo;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.Functions;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.erc20.Tokens;
import pro.belbix.ethparser.web3.harvest.contracts.Vaults;
import pro.belbix.ethparser.web3.harvest.db.ImportantEventsDbService;
import pro.belbix.ethparser.web3.harvest.decoder.ImportantEventsLogDecoder;


@Service
public class ImportantEventsParser implements Web3Parser {

    private static final Logger log = LoggerFactory.getLogger(ImportantEventsParser.class);
    private static final AtomicBoolean run = new AtomicBoolean(true);
    private final BlockingQueue<Log> logs = new ArrayBlockingQueue<>(100);
    private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
    private final ImportantEventsLogDecoder importantEventsLogDecoder = new ImportantEventsLogDecoder();
    private Instant lastTx = Instant.now();

    private final Web3Service web3Service;
    private final ImportantEventsDbService importantEventsDbService;
    private final ParserInfo parserInfo;
    private final EthBlockService ethBlockService;
    private final Functions functions;



    public static final String TOKEN_MINTED = "TokenMinted";

    public ImportantEventsParser(
        Web3Service web3Service,
        ImportantEventsDbService importantEventsDbService,
        ParserInfo parserInfo,
        EthBlockService ethBlockService,
        Functions functions) {
        this.web3Service = web3Service;
        this.importantEventsDbService = importantEventsDbService;
        this.parserInfo = parserInfo;
        this.ethBlockService = ethBlockService;
        this.functions = functions;
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

        ImportantEventsDTO dto = new ImportantEventsDTO();
        dto.setId(tx.getHash() + "_" + tx.getLogId());
        dto.setBlock(tx.getBlock());
        dto.setOldStrategy(tx.getOldStrategy());
        dto.setNewStrategy(tx.getNewStrategy());
        dto.setHash(tx.getHash());

        //enrich date
        dto.setBlockDate(
            ethBlockService.getTimestampSecForBlock(ethLog.getBlockHash(), ethLog.getBlockNumber().longValue()));

        parseEvent(dto, tx.getMethodName());
        parseVault(dto, tx.getVault());
        parseMintAmount(dto, tx.getMintAmount());
        updateInfo(dto, tx);
        log.info(dto.print());
        return dto;
    }

    private void updateInfo(ImportantEventsDTO dto, ImportantEventsTx tx) {
        ImportantEventsInfo info = new ImportantEventsInfo();
        info.setVaultAddress(tx.getVault());

        if ("StrategyAnnounced".equals(dto.getEvent())) {
            info.setStrategyTimeLock(functions.callStrategyTimeLock(tx.getVault(), tx.getBlock()).longValue());
            dto.setOldStrategy(functions.callStrategy(tx.getVault(), tx.getBlock()));
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            dto.setInfo(mapper.writeValueAsString(info));
        } catch (JsonProcessingException e) {
            log.error("Error converting to json " + info, e);
        }
    
    }

    private void parseMintAmount(ImportantEventsDTO dto, BigInteger mintAmount) {
        if (!mintAmount.equals(BigInteger.ZERO)) {
            dto.setMintAmount(mintAmount.doubleValue() / D18);
        }
    }

    private void parseEvent(ImportantEventsDTO dto, String methodName) {
        // ERC20 token Mint function emits Transfer event
        if ("Transfer".equals(methodName)) {
            dto.setEvent(TOKEN_MINTED);
        } else {
            dto.setEvent(methodName);
        }
    }

    private void parseVault(ImportantEventsDTO dto, String vault) {
        dto.setVault(
            Vaults.vaultHashToName.containsKey(vault)
                ? Vaults.vaultHashToName.get(vault)
                : Tokens.findNameForContract(vault)
        );
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
