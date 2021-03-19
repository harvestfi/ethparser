package pro.belbix.ethparser.web3.layers.detector;

import static pro.belbix.ethparser.web3.contracts.ContractConstants.ZERO_ADDRESS;

import java.beans.MethodDescriptor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import org.web3j.tx.Contract;
import pro.belbix.ethparser.codegen.ContractGenerator;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.entity.a_layer.EthAddressEntity;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.entity.a_layer.EthLogEntity;
import pro.belbix.ethparser.entity.a_layer.EthTxEntity;
import pro.belbix.ethparser.entity.b_layer.ContractEventEntity;
import pro.belbix.ethparser.entity.b_layer.ContractLogEntity;
import pro.belbix.ethparser.entity.b_layer.ContractStateEntity;
import pro.belbix.ethparser.entity.b_layer.ContractTxEntity;
import pro.belbix.ethparser.entity.b_layer.LogHexEntity;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
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
    private final ContractGenerator contractGenerator;

    public ContractDetector(Web3Service web3Service, AppProperties appProperties,
        SubscriptionRouter subscriptionRouter,
        ContractEventsDbService contractEventsDbService,
        ContractGenerator contractGenerator) {
        this.web3Service = web3Service;
        this.appProperties = appProperties;
        this.subscriptionRouter = subscriptionRouter;
        this.contractEventsDbService = contractEventsDbService;
        this.contractGenerator = contractGenerator;
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
                        ContractEventEntity eventPersisted =
                            contractEventsDbService.save(event);
                        if (eventPersisted != null) {
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

    public List<ContractEventEntity> handleBlock(EthBlockEntity block) {
        if (block == null) {
            return List.of();
        }

        Map<EthAddressEntity, Map<String, EthTxEntity>> contractsWithTxs =
            collectEligibleContracts(block);
        if (contractsWithTxs.isEmpty()) {
            return List.of();
        }
        List<ContractEventEntity> eventEntities = new ArrayList<>();
        for (Entry<EthAddressEntity, Map<String, EthTxEntity>> entry : contractsWithTxs
            .entrySet()) {
            ContractEventEntity eventEntity = new ContractEventEntity();

            EthAddressEntity contractAddress = entry.getKey();
            ContractEntity contract = ContractUtils
                .getContractByAddress(contractAddress.getAddress())
                .orElse(null);
            if (contract == null) {
                log.error("Not found contract for {}", contractAddress);
                continue;
            }
            eventEntity.setContract(contractAddress);
            eventEntity.setBlock(block);

            Set<ContractTxEntity> contractTxEntities = new LinkedHashSet<>();
            for (EthTxEntity tx : entry.getValue().values()) {
                ContractTxEntity contractTxEntity = new ContractTxEntity();
                contractTxEntity.setTx(tx);
                contractTxEntity.setContractEvent(eventEntity);

                collectLogs(tx, contractTxEntity);
                contractTxEntities.add(contractTxEntity);
            }
            eventEntity.setTxs(contractTxEntities);

            collectStates(eventEntity);
            eventEntities.add(eventEntity);
        }
        return eventEntities;
    }

    private void collectLogs(EthTxEntity tx, ContractTxEntity contractTxEntity) {
        int block = (int) tx.getBlockNumber().getNumber();
        // remove duplicates logs
        Map<String, EthLogEntity> logsMap = new LinkedHashMap<>();
        for (EthLogEntity ethLog : tx.getLogs()) {
            logsMap.put(tx.getHash().getHash() + "_" + ethLog.getId(), ethLog);
        }

        Set<ContractLogEntity> logEntities = new LinkedHashSet<>();
        for (EthLogEntity ethLog : logsMap.values()) {
            if (!isEligibleContract(ethLog.getAddress().getAddress())) {
                continue;
            }
            Event event = contractGenerator.findEventByHex(
                ethLog.getAddress().getAddress(),
                ethLog.getFirstTopic().getHash(),
                block);
            if (event == null) {
                log.warn("Not found event for {} from {}",
                    ethLog.getFirstTopic().getHash(), tx.getHash().getHash());
                continue;
            }
            String logValues = extractLogValues(ethLog, event);

            ContractLogEntity logEntity = new ContractLogEntity();
            logEntity.setLogIdx(ethLog.getLogId());
            logEntity.setLogs(logValues);
            logEntity.setContractTx(contractTxEntity);

            LogHexEntity logHexEntity = new LogHexEntity();
            String methodId = MethodDecoder.createMethodId(event.getName(), event.getParameters());
            logHexEntity.setMethodId(methodId);
            logHexEntity.setMethodName(event.getName());
            logHexEntity.setTopicHash(ethLog.getFirstTopic());
            logEntity.setTopic(logHexEntity);

            logEntities.add(logEntity);
        }
        contractTxEntity.setLogs(logEntities);
    }

    private String extractLogValues(EthLogEntity ethLog, Event event) {
        if (ethLog.getTopics() == null || ethLog.getTopics().isBlank()) {
            return null;
        }
        List<String> topics = new ArrayList<>(List.of(ethLog.getFirstTopic().getHash()));
        topics.addAll(List.of(ethLog.getTopics().split(",")));
        @SuppressWarnings("rawtypes")
        List<Type> types = MethodDecoder.extractLogIndexedValues(
            topics,
            ethLog.getData(),
            event.getParameters()
        );
        return MethodDecoder.typesToString(types);
    }

    private void collectStates(ContractEventEntity eventEntity) {
        Integer block = (int) eventEntity.getBlock().getNumber();
        String contractAddress = eventEntity.getContract().getAddress().toLowerCase();
        Class<?> clazz = contractGenerator.getWrapperClassByAddress(contractAddress, block);
        if (clazz == null) {
            log.error("Wrapper class for {} not found", contractAddress);
            return;
        }

        List<MethodDescriptor> methods = WrapperReader.collectMethods(clazz);
        Object wrapper = WrapperReader.createWrapperInstance(
            clazz, contractAddress, web3Service.getWeb3());

        Set<ContractStateEntity> states = new LinkedHashSet<>();
        for (MethodDescriptor method : methods) {
            try {
                Object value = WrapperReader
                    .callFunction(method.getMethod(), (Contract) wrapper, block);
                if (value == null) {
                    log.warn("Empty state for {} {}", method.getName(), contractAddress);
                    continue;
                }
                ContractStateEntity state = new ContractStateEntity();
                state.setContractEvent(eventEntity);
                state.setName(method.getName().replace("call_", ""));
                state.setValue(valueToString(value));
                states.add(state);
            } catch (Exception e) {
                log.error("Error call method {}", method.getName(), e);
            }
        }
        eventEntity.setStates(states);
    }

    private String valueToString(Object value) {
        if (value instanceof byte[]) {
            byte[] bytes = (byte[]) value;
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        return value.toString();
    }

    static Map<EthAddressEntity, Map<String, EthTxEntity>> collectEligibleContracts(
        EthBlockEntity block) {
        if(block == null) {
            return Map.of();
        }
        Map<EthAddressEntity, Map<String, EthTxEntity>> addresses = new LinkedHashMap<>();
        for (EthTxEntity tx : block.getTransactions()) {
            if (tx.getToAddress() != null &&
                isEligibleContract(tx.getToAddress().getAddress())) {
                addToAddresses(addresses, tx.getToAddress(), tx);
            }
            if (tx.getContractAddress() != null &&
                isEligibleContract(tx.getContractAddress().getAddress())) {
                addToAddresses(addresses, tx.getContractAddress(), tx);
            }
            if (isEligibleContract(tx.getFromAddress().getAddress())) {
                addToAddresses(addresses, tx.getFromAddress(), tx);
            }
            for (EthLogEntity ethLog : tx.getLogs()) {
                if (isEligibleContract(ethLog.getAddress().getAddress())) {
                    addToAddresses(addresses, ethLog.getAddress(), tx);
                }
            }
        }
        return addresses;
    }

    private static boolean isEligibleContract(String address) {
        if (ZERO_ADDRESS.equalsIgnoreCase(address)) {
            return false;
        }
        return ContractUtils.getAllContractAddresses().contains(address.toLowerCase());
    }

    private static void addToAddresses(
        Map<EthAddressEntity, Map<String, EthTxEntity>> addresses,
        EthAddressEntity address,
        EthTxEntity tx) {
        if (ZERO_ADDRESS.equalsIgnoreCase(address.getAddress())) {
            return;
        }
        Map<String, EthTxEntity> txs = addresses
            .computeIfAbsent(address, k -> new LinkedHashMap<>());
        txs.put(tx.getHash().getHash(), tx);
    }
}
