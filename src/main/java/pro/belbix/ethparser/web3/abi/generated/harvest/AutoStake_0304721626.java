package pro.belbix.ethparser.web3.abi.generated.harvest;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.8.4.
 */
@SuppressWarnings("rawtypes")
public class AutoStake_0304721626 extends Contract {
    public static final String BINARY = "Bin file was not provided";

    public static final String FUNC_BALANCEOF = "balanceOf";

    public static final String FUNC_CONTROLLER = "controller";

    public static final String FUNC_EXIT = "exit";

    public static final String FUNC_FORCEGREYLISTEDEXIT = "forceGreyListedExit";

    public static final String FUNC_GOVERNANCE = "governance";

    public static final String FUNC_GREYLISTESCROW = "greylistEscrow";

    public static final String FUNC_LPTOKEN = "lpToken";

    public static final String FUNC_REFRESHAUTOSTAKE = "refreshAutoStake";

    public static final String FUNC_REWARDPOOL = "rewardPool";

    public static final String FUNC_SETGREYLISTESCROW = "setGreylistEscrow";

    public static final String FUNC_SETSTORAGE = "setStorage";

    public static final String FUNC_SHARE = "share";

    public static final String FUNC_STAKE = "stake";

    public static final String FUNC_STORE = "store";

    public static final String FUNC_TOTALSHARES = "totalShares";

    public static final String FUNC_TOTALVALUE = "totalValue";

    public static final String FUNC_UNIT = "unit";

    public static final String FUNC_VALUEPERSHARE = "valuePerShare";

    public static final Event FORCEGREYLISTEXITED_EVENT = new Event("ForceGreylistExited", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event SMARTCONTRACTDENIED_EVENT = new Event("SmartContractDenied", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}));
    ;

    public static final Event SMARTCONTRACTRECORDED_EVENT = new Event("SmartContractRecorded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event STAKED_EVENT = new Event("Staked", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event STAKINGDENIED_EVENT = new Event("StakingDenied", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event WITHDRAWN_EVENT = new Event("Withdrawn", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected AutoStake_0304721626(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected AutoStake_0304721626(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected AutoStake_0304721626(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected AutoStake_0304721626(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public List<ForceGreylistExitedEventResponse> getForceGreylistExitedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(FORCEGREYLISTEXITED_EVENT, transactionReceipt);
        ArrayList<ForceGreylistExitedEventResponse> responses = new ArrayList<ForceGreylistExitedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ForceGreylistExitedEventResponse typedResponse = new ForceGreylistExitedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.grelisted = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<ForceGreylistExitedEventResponse> forceGreylistExitedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, ForceGreylistExitedEventResponse>() {
            @Override
            public ForceGreylistExitedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(FORCEGREYLISTEXITED_EVENT, log);
                ForceGreylistExitedEventResponse typedResponse = new ForceGreylistExitedEventResponse();
                typedResponse.log = log;
                typedResponse.grelisted = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<ForceGreylistExitedEventResponse> forceGreylistExitedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(FORCEGREYLISTEXITED_EVENT));
        return forceGreylistExitedEventFlowable(filter);
    }

    public List<SmartContractDeniedEventResponse> getSmartContractDeniedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(SMARTCONTRACTDENIED_EVENT, transactionReceipt);
        ArrayList<SmartContractDeniedEventResponse> responses = new ArrayList<SmartContractDeniedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            SmartContractDeniedEventResponse typedResponse = new SmartContractDeniedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.greylistedAddress = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<SmartContractDeniedEventResponse> smartContractDeniedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, SmartContractDeniedEventResponse>() {
            @Override
            public SmartContractDeniedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(SMARTCONTRACTDENIED_EVENT, log);
                SmartContractDeniedEventResponse typedResponse = new SmartContractDeniedEventResponse();
                typedResponse.log = log;
                typedResponse.greylistedAddress = (String) eventValues.getIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<SmartContractDeniedEventResponse> smartContractDeniedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(SMARTCONTRACTDENIED_EVENT));
        return smartContractDeniedEventFlowable(filter);
    }

    public List<SmartContractRecordedEventResponse> getSmartContractRecordedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(SMARTCONTRACTRECORDED_EVENT, transactionReceipt);
        ArrayList<SmartContractRecordedEventResponse> responses = new ArrayList<SmartContractRecordedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            SmartContractRecordedEventResponse typedResponse = new SmartContractRecordedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.smartContractAddress = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.smartContractInitiator = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<SmartContractRecordedEventResponse> smartContractRecordedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, SmartContractRecordedEventResponse>() {
            @Override
            public SmartContractRecordedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(SMARTCONTRACTRECORDED_EVENT, log);
                SmartContractRecordedEventResponse typedResponse = new SmartContractRecordedEventResponse();
                typedResponse.log = log;
                typedResponse.smartContractAddress = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.smartContractInitiator = (String) eventValues.getIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<SmartContractRecordedEventResponse> smartContractRecordedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(SMARTCONTRACTRECORDED_EVENT));
        return smartContractRecordedEventFlowable(filter);
    }

    public List<StakedEventResponse> getStakedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(STAKED_EVENT, transactionReceipt);
        ArrayList<StakedEventResponse> responses = new ArrayList<StakedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            StakedEventResponse typedResponse = new StakedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.user = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.sharesIssued = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.oldShareVaule = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.newShareValue = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
            typedResponse.balanceOf = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<StakedEventResponse> stakedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, StakedEventResponse>() {
            @Override
            public StakedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(STAKED_EVENT, log);
                StakedEventResponse typedResponse = new StakedEventResponse();
                typedResponse.log = log;
                typedResponse.user = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.sharesIssued = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.oldShareVaule = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
                typedResponse.newShareValue = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
                typedResponse.balanceOf = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<StakedEventResponse> stakedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(STAKED_EVENT));
        return stakedEventFlowable(filter);
    }

    public List<StakingDeniedEventResponse> getStakingDeniedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(STAKINGDENIED_EVENT, transactionReceipt);
        ArrayList<StakingDeniedEventResponse> responses = new ArrayList<StakingDeniedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            StakingDeniedEventResponse typedResponse = new StakingDeniedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.user = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<StakingDeniedEventResponse> stakingDeniedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, StakingDeniedEventResponse>() {
            @Override
            public StakingDeniedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(STAKINGDENIED_EVENT, log);
                StakingDeniedEventResponse typedResponse = new StakingDeniedEventResponse();
                typedResponse.log = log;
                typedResponse.user = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<StakingDeniedEventResponse> stakingDeniedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(STAKINGDENIED_EVENT));
        return stakingDeniedEventFlowable(filter);
    }

    public List<WithdrawnEventResponse> getWithdrawnEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(WITHDRAWN_EVENT, transactionReceipt);
        ArrayList<WithdrawnEventResponse> responses = new ArrayList<WithdrawnEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            WithdrawnEventResponse typedResponse = new WithdrawnEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.user = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.total = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<WithdrawnEventResponse> withdrawnEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, WithdrawnEventResponse>() {
            @Override
            public WithdrawnEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(WITHDRAWN_EVENT, log);
                WithdrawnEventResponse typedResponse = new WithdrawnEventResponse();
                typedResponse.log = log;
                typedResponse.user = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.total = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<WithdrawnEventResponse> withdrawnEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(WITHDRAWN_EVENT));
        return withdrawnEventFlowable(filter);
    }

    public RemoteFunctionCall<BigInteger> call_balanceOf(String who) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_BALANCEOF, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, who)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_balanceOf(String who) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_BALANCEOF, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, who)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> call_controller() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_CONTROLLER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_controller() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_CONTROLLER, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_exit() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_EXIT, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_forceGreyListedExit(String greyListed) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_FORCEGREYLISTEDEXIT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, greyListed)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> call_governance() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GOVERNANCE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_governance() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_GOVERNANCE, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> call_greylistEscrow() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GREYLISTESCROW, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_greylistEscrow() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_GREYLISTESCROW, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> call_lpToken() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_LPTOKEN, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_lpToken() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_LPTOKEN, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_refreshAutoStake() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_REFRESHAUTOSTAKE, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> call_rewardPool() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_REWARDPOOL, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_rewardPool() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_REWARDPOOL, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_setGreylistEscrow(String _greylistEscrow) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_SETGREYLISTESCROW, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _greylistEscrow)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_setStorage(String _store) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_SETSTORAGE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _store)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> call_share(String param0) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_SHARE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_share(String param0) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_SHARE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, param0)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_stake(BigInteger amount) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_STAKE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(amount)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> call_store() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_STORE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_store() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_STORE, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> call_totalShares() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_TOTALSHARES, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_totalShares() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_TOTALSHARES, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> call_totalValue() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_TOTALVALUE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_totalValue() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_TOTALVALUE, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> call_unit() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_UNIT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_unit() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_UNIT, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> call_valuePerShare() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_VALUEPERSHARE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_valuePerShare() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_VALUEPERSHARE, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static AutoStake_0304721626 load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new AutoStake_0304721626(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static AutoStake_0304721626 load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new AutoStake_0304721626(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static AutoStake_0304721626 load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new AutoStake_0304721626(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static AutoStake_0304721626 load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new AutoStake_0304721626(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static class ForceGreylistExitedEventResponse extends BaseEventResponse {
        public String grelisted;

        public BigInteger amount;
    }

    public static class SmartContractDeniedEventResponse extends BaseEventResponse {
        public String greylistedAddress;
    }

    public static class SmartContractRecordedEventResponse extends BaseEventResponse {
        public String smartContractAddress;

        public String smartContractInitiator;
    }

    public static class StakedEventResponse extends BaseEventResponse {
        public String user;

        public BigInteger amount;

        public BigInteger sharesIssued;

        public BigInteger oldShareVaule;

        public BigInteger newShareValue;

        public BigInteger balanceOf;
    }

    public static class StakingDeniedEventResponse extends BaseEventResponse {
        public String user;

        public BigInteger amount;
    }

    public static class WithdrawnEventResponse extends BaseEventResponse {
        public String user;

        public BigInteger total;
    }
}
