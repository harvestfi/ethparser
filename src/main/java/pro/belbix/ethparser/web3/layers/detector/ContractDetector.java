package pro.belbix.ethparser.web3.layers.detector;

import java.beans.MethodDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.tx.exceptions.ContractCallException;
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
import pro.belbix.ethparser.web3.abi.generated.WrapperMapper;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.layers.SubscriptionRouter;
import pro.belbix.ethparser.web3.layers.detector.db.ContractEventsDbService;

@Service
@Log4j2
public class ContractDetector {

    private static final String EXEC_REVERTED = "Contract Call has been reverted by the EVM with the reason: 'execution reverted'.";
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

        Map<EthAddressEntity, Map<String, EthTxEntity>> contractsWithTxs = collectEligibleContracts(
            block);
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

            Set<ContractTxEntity> contractTxEntities = new HashSet<>();
            for (EthTxEntity tx : entry.getValue().values()) {
                ContractTxEntity contractTxEntity = new ContractTxEntity();
                contractTxEntity.setTx(tx);

                collectLogs(tx, contractTxEntity);
                contractTxEntity.setContractEvent(eventEntity);
                contractTxEntities.add(contractTxEntity);
            }
            eventEntity.setTxs(contractTxEntities);

            collectStates(eventEntity);
            eventEntities.add(eventEntity);
        }
        return eventEntities;
    }

    private void collectLogs(
        EthTxEntity tx, ContractTxEntity contractTxEntity) {
        // remove duplicates logs
        Map<String, EthLogEntity> logsMap = new LinkedHashMap<>();
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
                List<String> topics = new ArrayList<>(List.of(ethLog.getFirstTopic().getHash()));
                topics.addAll(List.of(ethLog.getTopics().split(",")));
                @SuppressWarnings("rawtypes")
                List<Type> types = MethodDecoder.extractLogIndexedValues(
                    topics,
                    ethLog.getData(),
                    event.getParameters()
                );
                logValues = MethodDecoder.typesToString(types);
            }

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

    private void collectStates(ContractEventEntity eventEntity) {
        String contractAddress = eventEntity.getContract().getAddress().toLowerCase();
        Class<?> clazz = WrapperMapper.contractToWrapper.get(contractAddress);
        if (clazz == null) {
            log.error("Wrapper class for {} not found", contractAddress);
            return;
        }

        List<MethodDescriptor> methods = WrapperReader.collectMethods(clazz);
        Object wrapper = WrapperReader.createWrapperInstance(
            clazz, contractAddress, web3Service.getWeb3());

        Set<ContractStateEntity> states = new HashSet<>();
        for (MethodDescriptor method : methods) {
            try {
                Object value = callFunction(method.getMethod(), wrapper);
                if (value == null) {
                    log.warn("Empty state for {}", method.getName());
                    continue;
                }
                ContractStateEntity state = new ContractStateEntity();
                state.setContractEvent(eventEntity);
                state.setName(method.getName().replace("call_", ""));
                state.setValue(value.toString());
                states.add(state);
            } catch (Exception e) {
                log.error("Error call method {}", method.getName(), e);
            }
        }
        eventEntity.setStates(states);
    }

    private Object callFunction(Method method, Object wrapper) {
        int count = 0;
        while (true) {
            try {
                return ((RemoteFunctionCall<?>) method.invoke(wrapper)).send();
            } catch (Exception e) {
                log.error("Error function call", e);
                if (e instanceof ContractCallException) {
                    if (EXEC_REVERTED.equals(e.getMessage())) {
                        break;
                    }
                }
                count++;
                if (count == 3) {
                    break;
                }
            }
            log.info("Retry func call {}", count);
        }
        return null;
    }

    private Map<EthAddressEntity, Map<String, EthTxEntity>> collectEligibleContracts(
        EthBlockEntity block) {
        Map<EthAddressEntity, Map<String, EthTxEntity>> addresses = new LinkedHashMap<>();
        for (EthTxEntity tx : block.getTransactions()) {
            if (ContractUtils.getAllContractAddresses().contains(
                tx.getToAddress().getAddress().toLowerCase())) {
                addToAddresses(addresses, tx.getToAddress(), tx);
            }
            if (ContractUtils.getAllContractAddresses().contains(
                tx.getFromAddress().getAddress().toLowerCase())) {
                addToAddresses(addresses, tx.getFromAddress(), tx);
            }
            for (EthLogEntity ethLog : tx.getLogs()) {
                if (ContractUtils.getAllContractAddresses().contains(
                    ethLog.getAddress().getAddress().toLowerCase())) {
                    addToAddresses(addresses, ethLog.getAddress(), tx);
                    break;
                }
            }
        }
        return addresses;
    }

    private void addToAddresses(
        Map<EthAddressEntity, Map<String, EthTxEntity>> addresses,
        EthAddressEntity address,
        EthTxEntity tx) {
        Map<String, EthTxEntity> txs =
            addresses.computeIfAbsent(address, k -> new LinkedHashMap<>());
        txs.put(tx.getHash().getHash(), tx);
    }
}
