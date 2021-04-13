package pro.belbix.ethparser.web3.layers.detector;

import static pro.belbix.ethparser.web3.contracts.ContractConstants.ZERO_ADDRESS;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.codegen.FunctionWrapper;
import pro.belbix.ethparser.codegen.GeneratedContract;
import pro.belbix.ethparser.codegen.SimpleContractGenerator;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.entity.a_layer.EthAddressEntity;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.entity.a_layer.EthLogEntity;
import pro.belbix.ethparser.entity.a_layer.EthTxEntity;
import pro.belbix.ethparser.entity.b_layer.ContractEventEntity;
import pro.belbix.ethparser.entity.b_layer.ContractLogEntity;
import pro.belbix.ethparser.entity.b_layer.ContractStateEntity;
import pro.belbix.ethparser.entity.b_layer.ContractTxEntity;
import pro.belbix.ethparser.entity.b_layer.FunctionHashEntity;
import pro.belbix.ethparser.entity.b_layer.LogHashEntity;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.MethodDecoder;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.layers.SubscriptionRouter;
import pro.belbix.ethparser.web3.layers.ViewRouter;
import pro.belbix.ethparser.web3.layers.detector.db.ContractEventsDbService;

@Service
@Log4j2
public class ContractDetector {

    private static final AtomicBoolean run = new AtomicBoolean(true);
    private final BlockingQueue<EthBlockEntity> input = new ArrayBlockingQueue<>(100);
    private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);

    private final AppProperties appProperties;
    private final SubscriptionRouter subscriptionRouter;
    private final ContractEventsDbService contractEventsDbService;
    private final SimpleContractGenerator simpleContractGenerator;
    private final FunctionsUtils functionsUtils;
    private final ViewRouter viewRouter;

    public ContractDetector(AppProperties appProperties,
        SubscriptionRouter subscriptionRouter,
        ContractEventsDbService contractEventsDbService,
        SimpleContractGenerator simpleContractGenerator,
        FunctionsUtils functionsUtils, ViewRouter viewRouter) {
        this.appProperties = appProperties;
        this.subscriptionRouter = subscriptionRouter;
        this.contractEventsDbService = contractEventsDbService;
        this.simpleContractGenerator = simpleContractGenerator;
        this.functionsUtils = functionsUtils;
        this.viewRouter = viewRouter;
    }

    public void start() {
        log.info("Start ContractDetector");
        subscriptionRouter.subscribeOnBlocks(input);
        new Thread(() -> {
            while (run.get()) {
                EthBlockEntity block = null;
                try {
                    block = input.poll(1, TimeUnit.SECONDS);
                    List<ContractEventEntity> events = handleBlock(block,
                        appProperties.getNetwork());
                    for (ContractEventEntity event : events) {
                        ContractEventEntity eventPersisted =
                            contractEventsDbService.save(event);
                        if (eventPersisted != null) {
                            viewRouter.route(eventPersisted, appProperties.getNetwork());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error contract detector loop {}",
                        block == null ? null : block.getNumber(), e);
                    if (appProperties.isStopOnParseError()) {
                        System.exit(-1);
                    }
                }
            }
        }).start();
    }

    public List<ContractEventEntity> handleBlock(EthBlockEntity block, String network) {
        if (block == null) {
            return List.of();
        }

        Tuple2<Set<EthAddressEntity>, Set<EthTxEntity>> eligible = collectEligible(block, network);
        if (eligible.component1().isEmpty()) {
            return List.of();
        }
        List<ContractEventEntity> eventEntities =
            handleEligibleAddresses(eligible.component1(), block, network);

        handleEligibleTxs(eligible.component2(), eventEntities, network);
        log.info("Block {} handled and generated {} events",
            block.getNumber(), eventEntities.size());
        return eventEntities;
    }

    private List<ContractEventEntity> handleEligibleAddresses(
        Set<EthAddressEntity> addresses,
        EthBlockEntity block,
        String network
    ) {
        List<ContractEventEntity> eventEntities = new ArrayList<>();
        for (EthAddressEntity address : addresses) {
            ContractEventEntity eventEntity = new ContractEventEntity();
            ContractEntity contract = ContractUtils.getInstance(network)
                .getContractByAddress(address.getAddress())
                .orElse(null);
            if (contract == null) {
                log.error("Not found contract for {}", address.getAddress());
                continue;
            }
            eventEntity.setContract(address);
            eventEntity.setBlock(block);
            collectStates(eventEntity, network);
            eventEntities.add(eventEntity);
        }
        return eventEntities;
    }

    private void handleEligibleTxs(
        Set<EthTxEntity> txs,
        List<ContractEventEntity> eventEntities,
        String network
    ) {
        for (EthTxEntity tx : txs) {

            Set<String> eligibleAddresses = collectEligibleAddresses(tx, network).stream()
                .map(EthAddressEntity::getAddress)
                .collect(Collectors.toSet());

            Set<ContractEventEntity> eligibleEvents = eventEntities.stream()
                .filter(e -> eligibleAddresses.contains(e.getContract().getAddress()))
                .collect(Collectors.toSet());

            ContractTxEntity contractTxEntity = new ContractTxEntity();
            contractTxEntity.setTx(tx);
            fillFuncData(tx, contractTxEntity, network);
            collectLogs(tx, contractTxEntity, network);

            eligibleEvents.forEach(e -> {
                if (e.getTxs() == null) {
                    e.setTxs(new LinkedHashSet<>());
                }
                e.getTxs().add(contractTxEntity);
            });
            contractTxEntity.setContractEvents(eligibleEvents);
        }
    }

    private void fillFuncData(EthTxEntity tx, ContractTxEntity contractTx, String network) {
        String input = tx.getInput();
        if (input == null || input.isBlank() || input.length() < 10) {
            return;
        }

        String methodId = input.substring(0, 10);
        String inputData = input.substring(10);

        String address = tx.getToAddress().getAddress();
        GeneratedContract contract =
            simpleContractGenerator.getContract(
                address,
                tx.getBlockNumber().getNumber(),
                methodId,
                network
            );
        if (contract == null) {
            log.warn("Can't generate contract for {}", address);
            return;
        }

        FunctionHashEntity funcHash = new FunctionHashEntity();
        funcHash.setMethodId(methodId);
        contractTx.setFuncHash(funcHash);

        FunctionWrapper function = contract.getFunction(methodId);
        if (function == null) {
            log.warn("Not found function for {} in {}", methodId, tx.getHash().getHash());
        } else {
            String funcData = parseFunctionInput(inputData, function.getInput());
            contractTx.setFuncData(funcData);
            funcHash.setName(function.getFunction().getName());
        }
    }

    @SuppressWarnings("rawtypes")
    private String parseFunctionInput(
        String inputData,
        List<TypeReference<Type>> parameters
    ) {
        List<Type> types = FunctionReturnDecoder.decode(inputData, parameters);
        try {
            return MethodDecoder.typesToString(types);
        } catch (JsonProcessingException e) {
            log.error("Error parse function", e);
            return "";
        }
    }

    private void collectLogs(EthTxEntity tx, ContractTxEntity contractTxEntity, String network) {
        int block = (int) tx.getBlockNumber().getNumber();

        Set<ContractLogEntity> logEntities = new LinkedHashSet<>();
        for (EthLogEntity ethLog : tx.getLogs()) {
//            if (!isEligibleContract(ethLog.getAddress().getAddress())) {
//                continue;
//            }
            String logAddress = ethLog.getAddress().getAddress();
            Event event = findEvent(
                logAddress,
                ethLog.getFirstTopic().getHash(),
                block, network);
            if (event == null) {
                log.warn("Not found event for hash: {} from tx: {} contract: {}",
                    ethLog.getFirstTopic().getHash(), tx.getHash().getHash(),
                    logAddress);
                continue;
            }
            String logValues = extractLogValues(ethLog, event);

            ContractLogEntity logEntity = new ContractLogEntity();
            logEntity.setAddress(new EthAddressEntity(logAddress));
            logEntity.setLogIdx(ethLog.getLogId());
            logEntity.setLogs(logValues);
            logEntity.setContractTx(contractTxEntity);

            LogHashEntity logHashEntity = new LogHashEntity();
            String methodId = MethodDecoder.createMethodId(event.getName(), event.getParameters());
            logHashEntity.setMethodId(methodId);
            logHashEntity.setMethodName(event.getName());
            logHashEntity.setTopicHash(ethLog.getFirstTopic());
            logEntity.setTopic(logHashEntity);

            logEntities.add(logEntity);
        }
        contractTxEntity.setLogs(logEntities);
    }

    private Event findEvent(String address, String hash, int block, String network) {
        GeneratedContract contract = simpleContractGenerator.getContract(address, (long) block, null,network);
        if (contract == null) {
            return null;
        }
        return contract.getEvent(hash);
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
        try {
            return MethodDecoder.typesToString(types);
        } catch (JsonProcessingException e) {
            log.error("Error parse logs", e);
            return "";
        }
    }

    private void collectStates(ContractEventEntity eventEntity, String network) {
        int block = (int) eventEntity.getBlock().getNumber();
        String contractAddress = eventEntity.getContract().getAddress().toLowerCase();

        GeneratedContract contract =
            simpleContractGenerator.getContract(contractAddress, (long) block,null, network);
        if (contract == null) {
            log.error("Can't generate contract for {} at {}", contractAddress, block);
            return;
        }

        Set<ContractStateEntity> states = new LinkedHashSet<>();
        for (FunctionWrapper functionW : contract.getFunctions()) {
            if (!functionW.isView() || !functionW.getInput().isEmpty()) {
                continue;
            }
            Function function = functionW.getFunction();
            try {
                String value = functionsUtils
                    .callViewFunction(function, contractAddress, block, network)
                    .orElse(null);
                if (value == null) {
                    log.info("Null value for {} {}", function.getName(), contractAddress);
                    continue;
                }
                ContractStateEntity state = new ContractStateEntity();
                state.setContractEvent(eventEntity);
                state.setName(function.getName());
                state.setValue(value);
                states.add(state);
            } catch (Exception e) {
                log.error("Error call func {}", function.getName(), e);
            }
        }
        eventEntity.setStates(states);
    }

    Tuple2<Set<EthAddressEntity>, Set<EthTxEntity>> collectEligible(EthBlockEntity block,
        String network) {
        if (block == null) {
            return new Tuple2<>(Set.of(), Set.of());
        }
        Set<EthAddressEntity> addresses = new LinkedHashSet<>();
        Set<EthTxEntity> txs = new LinkedHashSet<>();
        for (EthTxEntity tx : block.getTransactions()) {
            collectEligibleAddresses(tx, network)
                .forEach(a -> addToSets(addresses, txs, tx, a, network));
        }
        return new Tuple2<>(addresses, txs);
    }

    private List<EthAddressEntity> collectEligibleAddresses(EthTxEntity tx, String network) {
        List<EthAddressEntity> addresses = new ArrayList<>();
        if (isEligibleContract(tx.getToAddress(), network)) {
            addresses.add(tx.getToAddress());
        }
        if (isEligibleContract(tx.getContractAddress(), network)) {
            addresses.add(tx.getToAddress());
        }
        if (isEligibleContract(tx.getFromAddress(), network)) {
            addresses.add(tx.getToAddress());
        }
        for (EthLogEntity ethLog : tx.getLogs()) {
            if (isEligibleContract(ethLog.getAddress(), network)) {
                addresses.add(ethLog.getAddress());
            }
        }
        return addresses;
    }

    private void addToSets(
        Set<EthAddressEntity> addresses,
        Set<EthTxEntity> txs,
        EthTxEntity tx,
        EthAddressEntity address,
        String network
    ) {
        if (isEligibleContract(address, network)) {
            addresses.add(address);
            txs.add(tx);
        }
    }

    private boolean isEligibleContract(EthAddressEntity address, String network) {
        if (address == null || ZERO_ADDRESS.equalsIgnoreCase(address.getAddress())) {
            return false;
        }
        return ContractUtils.getInstance(network).getAllContractAddresses()
            .contains(address.getAddress().toLowerCase());
    }
}
