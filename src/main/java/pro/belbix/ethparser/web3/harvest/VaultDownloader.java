package pro.belbix.ethparser.web3.harvest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.model.HarvestDTO;
import pro.belbix.ethparser.model.HarvestTx;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.Web3Service;

@SuppressWarnings("rawtypes")
@Service
public class VaultDownloader {

    private static final Logger logger = LoggerFactory.getLogger(VaultDownloader.class);
    private final Web3Service web3Service;
    private final HarvestDBService harvestDBService;
    private final EthBlockService ethBlockService;
    private final HarvestVaultLogDecoder harvestVaultLogDecoder = new HarvestVaultLogDecoder();

    public VaultDownloader(
        Web3Service web3Service, HarvestDBService harvestDBService, EthBlockService ethBlockService) {
        this.web3Service = web3Service;
        this.harvestDBService = harvestDBService;
        this.ethBlockService = ethBlockService;
    }

    public void parseVault(String vaultHash, DefaultBlockParameter fromBlock) {
        if (fromBlock == null) {
            fromBlock = DefaultBlockParameter.valueOf(harvestDBService.lastBlock());
        }
        Map<String, Integer> methods = new HashMap<>();
        List<LogResult> logResults = web3Service.fetchContractLogs(vaultHash, fromBlock);
        if (logResults.isEmpty()) {
            logger.error("Empty log");
            return;
        }
        for (LogResult logResult : logResults) {
            Log log = (Log) logResult.get();
            if (log.getTopics().isEmpty()) {
                logger.error("Empty topic " + log);
                continue;
            }
            String topic0 = log.getTopics().get(0);
            HarvestTx harvestTx = new HarvestTx();
            harvestTx.setVault(new Address(vaultHash));
            try {
                harvestVaultLogDecoder.enrichFromLog(harvestTx, log);
            } catch (Exception e) {
                logger.error("error with " + log, e);
                continue;
            }

            String methodName = harvestTx.getMethodName();
            if (methods.containsKey(methodName)) {
                methods.put(methodName, methods.get(methodName) + 1);
            } else {
                methods.put(methodName, 1);
            }

            if (!allowedMethods.contains(methodName)) {
                continue;
            }

//            TransactionReceipt transactionReceipt = web3Service.fetchTransactionReceipt(harvestTx.getHash());
//            if ("0x1".equals(transactionReceipt.getStatus())) {
                harvestTx.setSuccess(true); //always success from logs
//            }

            HarvestDTO dto = harvestTx.toDto();
            dto.setBlockDate(ethBlockService.getTimestampSecForBlock(log.getBlockHash()));
            logger.info(dto.print());
            harvestDBService.saveHarvestDTO(dto);

        }

        methods.forEach((key, value) -> logger.info(key + " " + value));
    }

    private static final Set<String> allowedMethods = new HashSet<>();

    static {
        allowedMethods.add("Withdraw");
        allowedMethods.add("Deposit");
    }

}
