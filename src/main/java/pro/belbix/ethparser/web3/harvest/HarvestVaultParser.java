package pro.belbix.ethparser.web3.harvest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.model.DtoI;
import pro.belbix.ethparser.model.HarvestDTO;
import pro.belbix.ethparser.model.HarvestTx;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Service;

@Service
public class HarvestVaultParser implements Web3Parser {

    private static final Logger log = LoggerFactory.getLogger(HarvestVaultParser.class);
    private static final AtomicBoolean run = new AtomicBoolean(true);
    private static final Set<String> allowedMethods = new HashSet<>(Arrays.asList("withdraw", "deposit"));
    private static final String ZERO_ADDRESS = "0x0000000000000000000000000000000000000000";
    private final HarvestVaultLogDecoder harvestVaultLogDecoder = new HarvestVaultLogDecoder();
    private final Web3Service web3Service;
    private long parsedTxCount = 0;
    private final BlockingQueue<Log> logs = new ArrayBlockingQueue<>(10_000);
    private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(10_000);
    private final HarvestDBService harvestDBService;
    private final EthBlockService ethBlockService;

    public HarvestVaultParser(Web3Service web3Service,
                              HarvestDBService harvestDBService,
                              EthBlockService ethBlockService) {
        this.web3Service = web3Service;
        this.harvestDBService = harvestDBService;
        this.ethBlockService = ethBlockService;
    }

    @Override
    public void startParse() {
        log.info("Start parse Harvest vaults logs");
        web3Service.subscribeOnLogs(logs);
        new Thread(() -> {
            while (run.get()) {
                Log ethLog = null;
                try {
                    ethLog = logs.poll(1, TimeUnit.SECONDS);
                } catch (InterruptedException ignored) {
                }
                HarvestDTO dto = parseVaultLog(ethLog);
                if (dto != null) {
                    try {
                        enrichDto(dto);
                        boolean success = harvestDBService.saveHarvestDTO(dto);
                        if (success) {
                            output.put(dto);
                        }
                    } catch (Exception e) {
                        log.error("Can't save " + dto.toString(), e);
                    }
                }
            }
        }).start();
    }

    public HarvestDTO parseVaultLog(Log ethLog) {
        HarvestTx harvestTx;
        try {
            harvestTx = harvestVaultLogDecoder.decode(ethLog);
        } catch (Exception e) {
            log.error("Error decode " + ethLog, e);
            return null;
        }
        if (harvestTx == null) {
            return null;
        }

        if (!allowedMethods.contains(harvestTx.getMethodName().toLowerCase())) {
            return null;
        }
        TransactionReceipt receipt = web3Service.fetchTransactionReceipt(harvestTx.getHash());
        replaceInputCoinValueOnFarmWrap(harvestTx, receipt);

        HarvestDTO dto = harvestTx.toDto();

        //enrich date
        dto.setBlockDate(ethBlockService.getTimestampSecForBlock(harvestTx.getBlockHash()));

        //enrich owner
        dto.setOwner(receipt.getFrom());

        log.info(dto.print());
        return dto;
    }

    private void replaceInputCoinValueOnFarmWrap(HarvestTx harvestTx, TransactionReceipt receipt) {
        harvestTx.setAmountIn(harvestTx.getAmount());

        List<Log> logs = receipt.getLogs();

        for (Log ethLog : logs) {
            HarvestTx logTx = harvestVaultLogDecoder.decode(ethLog);
            if (logTx == null) {
                continue;
            }
            if (!"Transfer".equals(logTx.getMethodName())) {
                continue;
            }

            if (ZERO_ADDRESS.equals(logTx.getAddressFromArgs1().getValue())
                && harvestTx.getMethodName().toLowerCase().equals("deposit")) {
                harvestTx.setAmount(logTx.getAmount());
                harvestTx.setfToken(new Address(ethLog.getAddress()));
                return;
            } else if (ZERO_ADDRESS.equals(logTx.getAddressFromArgs2().getValue())
                && harvestTx.getMethodName().toLowerCase().equals("withdraw")) {
                harvestTx.setAmount(logTx.getAmount());
                harvestTx.setfToken(new Address(ethLog.getAddress()));
                return;
            }
        }
        throw new IllegalStateException("Not found transfer value");
    }

    public void enrichDto(HarvestDTO dto) {
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
