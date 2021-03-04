package pro.belbix.ethparser.web3.layers.detector;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.Contract;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.entity.a_layer.EthTxEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.abi.contracts.harvest.WrappedVault;
import pro.belbix.ethparser.web3.contracts.ContractConstants;
import pro.belbix.ethparser.web3.layers.SubscriptionRouter;

@Service
@Log4j2
public class ContractDetector {

    private static final AtomicBoolean run = new AtomicBoolean(true);
    private final BlockingQueue<EthBlockEntity> input = new ArrayBlockingQueue<>(100);
    private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);

    private final Web3Service web3Service;
    private final AppProperties appProperties;
    private final SubscriptionRouter subscriptionRouter;
    private final ContractFilter contractFilter;

    public ContractDetector(Web3Service web3Service, AppProperties appProperties,
                            SubscriptionRouter subscriptionRouter,
                            ContractFilter contractFilter) {
        this.web3Service = web3Service;
        this.appProperties = appProperties;
        this.subscriptionRouter = subscriptionRouter;
        this.contractFilter = contractFilter;
    }

    public void start() {
        log.info("Start ContractDetector");
        subscriptionRouter.subscribeOnBlocks(input);
        new Thread(() -> {
            while (run.get()) {
                EthBlockEntity block = null;
                try {
                    block = input.poll(1, TimeUnit.SECONDS);
                    handleBlock(block);
                } catch (Exception e) {
                    log.error("Error contract detector loop " + block, e);
                    if (appProperties.isStopOnParseError()) {
                        System.exit(-1);
                    }
                }
            }
        }).start();
    }

    private void handleBlock(EthBlockEntity block) {
        Map<String, List<EthTxEntity>> contractsWithLogs = collectEligibleContracts(block);

    }

    private void collectContractStates() {

    }

    private void wrapperMethods() {

    }

    private Map<String, List<EthTxEntity>> collectEligibleContracts(EthBlockEntity block) {
        Map<String, List<EthTxEntity>> addresses = new HashMap<>();
        for (EthTxEntity tx : block.getTransactions()) {
            if (contractFilter.isEligible(tx.getToAddress().getAddress())) {
                addresses
                    .computeIfAbsent(tx.getToAddress().getAddress(), k -> new ArrayList<>())
                    .add(tx);
            }
            if (contractFilter.isEligible(tx.getFromAddress().getAddress())) {
                addresses
                    .computeIfAbsent(tx.getFromAddress().getAddress(), k -> new ArrayList<>())
                    .add(tx);
            }
        }
        return addresses;
    }
}
