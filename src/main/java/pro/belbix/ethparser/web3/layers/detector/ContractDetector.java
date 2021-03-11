package pro.belbix.ethparser.web3.layers.detector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.entity.a_layer.EthLogEntity;
import pro.belbix.ethparser.entity.a_layer.EthTxEntity;
import pro.belbix.ethparser.entity.b_layer.ContractEventEntity;
import pro.belbix.ethparser.entity.b_layer.ContractLogEntity;
import pro.belbix.ethparser.entity.b_layer.LogHexEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.MethodDecoder;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.abi.WrapperReader;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.layers.SubscriptionRouter;
import pro.belbix.ethparser.web3.layers.detector.db.ContractEventsDbService;

@Service
@Log4j2
public class ContractDetector {

    private static final AtomicBoolean run = new AtomicBoolean(true);
    private final BlockingQueue<EthBlockEntity> input = new ArrayBlockingQueue<>(100);
    private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);

    private final Web3Service web3Service;
    private final AppProperties appProperties;
    private final SubscriptionRouter subscriptionRouter;
    private final ContractEventsDbService contractEventsDbService;

    public ContractDetector(Web3Service web3Service, AppProperties appProperties,
        SubscriptionRouter subscriptionRouter,
        ContractEventsDbService contractEventsDbService) {
        this.web3Service = web3Service;
        this.appProperties = appProperties;
        this.subscriptionRouter = subscriptionRouter;
        this.contractEventsDbService = contractEventsDbService;
    }

    public void start() {
        log.info("Start ContractDetector");
        subscriptionRouter.subscribeOnBlocks(input);
        new Thread(() -> {
            while (run.get()) {
                EthBlockEntity block = null;
                try {
                    block = input.poll(1, TimeUnit.SECONDS);
                    List<ContractEventEntity> events = handleBlock(block);
                    for (ContractEventEntity event : events) {
                        boolean success = contractEventsDbService.save(event);
                        if (success) {
                            //TODO send on C layer
                        }
                    }
                } catch (Exception e) {
                    log.error("Error contract detector loop " + block, e);
                    if (appProperties.isStopOnParseError()) {
                        System.exit(-1);
                    }
                }
            }
        }).start();
    }

    private List<ContractEventEntity> handleBlock(EthBlockEntity block) {
        if (block == null) {
            return List.of();
        }
        Map<String, List<EthTxEntity>> contractsWithTxs = collectEligibleContracts(block);
        if (contractsWithTxs.isEmpty()) {
            return List.of();
        }
        List<ContractEventEntity> eventEntities = new ArrayList<>();
        for (Entry<String, List<EthTxEntity>> entry : contractsWithTxs.entrySet()) {
            String contractAddress = entry.getKey();
            for (EthTxEntity tx : entry.getValue()) {
                ContractEventEntity eventEntity = new ContractEventEntity();
                eventEntity.setTx(tx);
                eventEntity.setContract(ContractUtils.getContractByAddress(contractAddress)
                    .orElse(null));

                collectLogs(tx, eventEntity);
                collectStates(eventEntity);
                eventEntities.add(eventEntity);
            }
        }
        return eventEntities;
    }

    private void collectLogs(
        EthTxEntity tx, ContractEventEntity eventEntity) {
        // remove duplicates logs
        Map<String, EthLogEntity> logsMap = new HashMap<>();
        for (EthLogEntity ethLog : tx.getLogs()) {
            logsMap.put(tx.getHash().getHash() + "_" + ethLog.getId(), ethLog);
        }

        Set<ContractLogEntity> logEntities = new HashSet<>();
        for (EthLogEntity ethLog : logsMap.values()) {
            Event event = WrapperReader.findEventByHex(ethLog.getFirstTopic().getHash());
            if (event == null) {
                continue;
            }
            String logValues = null;
            if (ethLog.getTopics() != null && !ethLog.getTopics().isBlank()) {
                @SuppressWarnings("rawtypes")
                List<Type> types = MethodDecoder.extractLogIndexedValues(
                    List.of(ethLog.getTopics().split(",")),
                    ethLog.getData(),
                    event.getParameters()
                );
                logValues = MethodDecoder.typesToString(types);
            }

            ContractLogEntity logEntity = new ContractLogEntity();
            logEntity.setLogs(logValues);
            logEntity.setContractEvent(eventEntity);

            LogHexEntity logHexEntity = new LogHexEntity();
            String methodId = MethodDecoder.createMethodId(event.getName(), event.getParameters());
            logHexEntity.setMethodId(methodId);
            logHexEntity.setMethodName(event.getName());
            logEntity.setTopic(logHexEntity);

            logEntities.add(logEntity);
        }
        eventEntity.setLogs(logEntities);
    }

    private void collectStates(ContractEventEntity eventEntity) {
        String contractAddress = eventEntity.getContract().getAddress();



    }

    private Map<String, List<EthTxEntity>> collectEligibleContracts(EthBlockEntity block) {
        Map<String, List<EthTxEntity>> addresses = new HashMap<>();
        for (EthTxEntity tx : block.getTransactions()) {
            if (ContractUtils.getAllContractAddresses().contains(tx.getToAddress().getAddress())) {
                addresses
                    .computeIfAbsent(tx.getToAddress().getAddress(), k -> new ArrayList<>())
                    .add(tx);
            }
            if (ContractUtils.getAllContractAddresses().contains(tx.getFromAddress().getAddress())) {
                addresses
                    .computeIfAbsent(tx.getFromAddress().getAddress(), k -> new ArrayList<>())
                    .add(tx);
            }
        }
        return addresses;
    }
}
