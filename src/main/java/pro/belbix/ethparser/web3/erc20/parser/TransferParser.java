package pro.belbix.ethparser.web3.erc20.parser;

import static pro.belbix.ethparser.web3.MethodDecoder.parseAmount;
import static pro.belbix.ethparser.web3.erc20.Tokens.FARM_TOKEN;
import static pro.belbix.ethparser.web3.erc20.TransferType.COMMON;
import static pro.belbix.ethparser.web3.erc20.TransferType.HARD_WORK;
import static pro.belbix.ethparser.web3.erc20.TransferType.LP_RECEIVE;
import static pro.belbix.ethparser.web3.erc20.TransferType.LP_SEND;
import static pro.belbix.ethparser.web3.erc20.TransferType.NOTIFY;
import static pro.belbix.ethparser.web3.erc20.TransferType.PS_EXIT;
import static pro.belbix.ethparser.web3.erc20.TransferType.PS_INTERNAL;
import static pro.belbix.ethparser.web3.erc20.TransferType.PS_STAKE;
import static pro.belbix.ethparser.web3.erc20.TransferType.REWARD;

import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.dto.TransferDTO;
import pro.belbix.ethparser.model.TokenTx;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.erc20.Tokens;
import pro.belbix.ethparser.web3.erc20.db.TransferDBService;
import pro.belbix.ethparser.web3.erc20.decoder.ERC20Decoder;
import pro.belbix.ethparser.web3.harvest.contracts.StakeContracts;
import pro.belbix.ethparser.web3.harvest.contracts.Vaults;
import pro.belbix.ethparser.web3.uniswap.contracts.LpContracts;

@Service
@Log4j2
public class TransferParser implements Web3Parser {

    private static final String FEE_REWARD_FORWARDER = "0x9397bd6fB1EC46B7860C8073D2cb83BE34270D94".toLowerCase();
    private static final AtomicBoolean run = new AtomicBoolean(true);
    private final BlockingQueue<Log> logs = new ArrayBlockingQueue<>(1000);
    private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
    private Instant lastTx = Instant.now();
    private final ERC20Decoder erc20Decoder = new ERC20Decoder();

    private final Web3Service web3Service;
    private final EthBlockService ethBlockService;
    private final ParserInfo parserInfo;
    private final TransferDBService transferDBService;

    public TransferParser(Web3Service web3Service,
                          EthBlockService ethBlockService,
                          ParserInfo parserInfo,
                          TransferDBService transferDBService) {
        this.web3Service = web3Service;
        this.ethBlockService = ethBlockService;
        this.parserInfo = parserInfo;
        this.transferDBService = transferDBService;
    }

    @Override
    public void startParse() {
        log.info("Start parse Token info logs");
        parserInfo.addParser(this);
        web3Service.subscribeOnLogs(logs);
        new Thread(() -> {
            while (run.get()) {
                Log ethLog = null;
                try {
                    ethLog = logs.poll(1, TimeUnit.SECONDS);
                    TransferDTO dto = parseLog(ethLog);
                    if (dto != null) {
                        lastTx = Instant.now();
                        boolean saved = transferDBService.saveDto(dto);
                        if (saved) {
                            output.put(dto);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error parse token info from " + ethLog, e);
                }
            }
        }).start();
    }

    public TransferDTO parseLog(Log ethLog) {
        if (ethLog == null || !FARM_TOKEN.equals(ethLog.getAddress())) {
            return null;
        }

        TokenTx tx;
        try {
            tx = erc20Decoder.decode(ethLog);
        } catch (Exception e) {
            log.error("Error decode " + ethLog, e);
            return null;
        }

        if (tx == null
            || !"Transfer".equals(tx.getMethodName())) {
            return null;
        }

        long blockTime = ethBlockService.getTimestampSecForBlock(tx.getBlockHash(), tx.getBlock());

        TransferDTO dto = new TransferDTO();
        dto.setId(tx.getHash() + "_" + tx.getLogId());
        dto.setBlock(tx.getBlock());
        dto.setBlockDate(blockTime);
        dto.setName(Tokens.findNameForContract(tx.getTokenAddress()));
        dto.setOwner(tx.getOwner());
        dto.setRecipient(tx.getRecipient());
        dto.setValue(parseAmount(tx.getValue(), tx.getTokenAddress()));

        fillMethodName(dto);
        fillTransferType(dto);

        log.info(dto.print());
        return dto;
    }

    public void fillMethodName(TransferDTO dto) {
        String hash = dto.getId().split("_")[0];
        Transaction ethTx = web3Service.findTransaction(hash);
        String methodName = erc20Decoder.decodeMethodName(ethTx);
        if (methodName == null) {
            log.warn("Can't decode method for " + hash);
            dto.setMethodName(ethTx.getInput().substring(0, 10));
            return;
        }
        dto.setMethodName(methodName);
    }

    public static void fillTransferType(TransferDTO dto) {
        String recipient = dto.getRecipient().toLowerCase();
        String owner = dto.getOwner().toLowerCase();
        if (Vaults.isPs(recipient)) {
            if (StakeContracts.isST_PS(owner)) {
                dto.setType(PS_INTERNAL.name());
            } else {
                dto.setType(PS_STAKE.name());
            }
        } else if (Vaults.isPs(owner)) {
            if (StakeContracts.isST_PS(recipient)) {
                dto.setType(PS_INTERNAL.name());
            } else {
                dto.setType(PS_EXIT.name());
            }
        } else if (StakeContracts.hashToName.containsKey(recipient)) {
            dto.setType(NOTIFY.name());
        } else if (StakeContracts.hashToName.containsKey(owner)) {
            dto.setType(REWARD.name());
        } else if (LpContracts.lpHashToName.containsKey(owner)) {
            dto.setType(LP_RECEIVE.name());
        } else if (LpContracts.lpHashToName.containsKey(recipient)) {
            dto.setType(LP_SEND.name());
        } else if (FEE_REWARD_FORWARDER.equals(recipient) || FEE_REWARD_FORWARDER.equals(owner)) {
            dto.setType(HARD_WORK.name());
        } else {
            dto.setType(COMMON.name());
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
