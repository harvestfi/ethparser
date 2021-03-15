package pro.belbix.ethparser.web3.abi.generated.erc20;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Int256;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
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
public class SetToken_945381920 extends Contract {
    public static final String BINARY = "Bin file was not provided";

    public static final String FUNC_ADDCOMPONENT = "addComponent";

    public static final String FUNC_ADDEXTERNALPOSITIONMODULE = "addExternalPositionModule";

    public static final String FUNC_ADDMODULE = "addModule";

    public static final String FUNC_ALLOWANCE = "allowance";

    public static final String FUNC_APPROVE = "approve";

    public static final String FUNC_BALANCEOF = "balanceOf";

    public static final String FUNC_BURN = "burn";

    public static final String FUNC_COMPONENTS = "components";

    public static final String FUNC_CONTROLLER = "controller";

    public static final String FUNC_DECIMALS = "decimals";

    public static final String FUNC_DECREASEALLOWANCE = "decreaseAllowance";

    public static final String FUNC_EDITDEFAULTPOSITIONUNIT = "editDefaultPositionUnit";

    public static final String FUNC_EDITEXTERNALPOSITIONDATA = "editExternalPositionData";

    public static final String FUNC_EDITEXTERNALPOSITIONUNIT = "editExternalPositionUnit";

    public static final String FUNC_EDITPOSITIONMULTIPLIER = "editPositionMultiplier";

    public static final String FUNC_GETCOMPONENTS = "getComponents";

    public static final String FUNC_GETDEFAULTPOSITIONREALUNIT = "getDefaultPositionRealUnit";

    public static final String FUNC_GETEXTERNALPOSITIONDATA = "getExternalPositionData";

    public static final String FUNC_GETEXTERNALPOSITIONMODULES = "getExternalPositionModules";

    public static final String FUNC_GETEXTERNALPOSITIONREALUNIT = "getExternalPositionRealUnit";

    public static final String FUNC_GETMODULES = "getModules";

    public static final String FUNC_GETPOSITIONS = "getPositions";

    public static final String FUNC_GETTOTALCOMPONENTREALUNITS = "getTotalComponentRealUnits";

    public static final String FUNC_INCREASEALLOWANCE = "increaseAllowance";

    public static final String FUNC_INITIALIZEMODULE = "initializeModule";

    public static final String FUNC_INVOKE = "invoke";

    public static final String FUNC_ISCOMPONENT = "isComponent";

    public static final String FUNC_ISEXTERNALPOSITIONMODULE = "isExternalPositionModule";

    public static final String FUNC_ISINITIALIZEDMODULE = "isInitializedModule";

    public static final String FUNC_ISLOCKED = "isLocked";

    public static final String FUNC_ISPENDINGMODULE = "isPendingModule";

    public static final String FUNC_LOCK = "lock";

    public static final String FUNC_LOCKER = "locker";

    public static final String FUNC_MANAGER = "manager";

    public static final String FUNC_MINT = "mint";

    public static final String FUNC_MODULESTATES = "moduleStates";

    public static final String FUNC_MODULES = "modules";

    public static final String FUNC_NAME = "name";

    public static final String FUNC_POSITIONMULTIPLIER = "positionMultiplier";

    public static final String FUNC_REMOVECOMPONENT = "removeComponent";

    public static final String FUNC_REMOVEEXTERNALPOSITIONMODULE = "removeExternalPositionModule";

    public static final String FUNC_REMOVEMODULE = "removeModule";

    public static final String FUNC_REMOVEPENDINGMODULE = "removePendingModule";

    public static final String FUNC_SETMANAGER = "setManager";

    public static final String FUNC_SYMBOL = "symbol";

    public static final String FUNC_TOTALSUPPLY = "totalSupply";

    public static final String FUNC_TRANSFER = "transfer";

    public static final String FUNC_TRANSFERFROM = "transferFrom";

    public static final String FUNC_UNLOCK = "unlock";

    public static final Event APPROVAL_EVENT = new Event("Approval", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event COMPONENTADDED_EVENT = new Event("ComponentAdded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}));
    ;

    public static final Event COMPONENTREMOVED_EVENT = new Event("ComponentRemoved", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}));
    ;

    public static final Event DEFAULTPOSITIONUNITEDITED_EVENT = new Event("DefaultPositionUnitEdited", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Int256>() {}));
    ;

    public static final Event EXTERNALPOSITIONDATAEDITED_EVENT = new Event("ExternalPositionDataEdited", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<DynamicBytes>() {}));
    ;

    public static final Event EXTERNALPOSITIONUNITEDITED_EVENT = new Event("ExternalPositionUnitEdited", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Int256>() {}));
    ;

    public static final Event INVOKED_EVENT = new Event("Invoked", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>(true) {}, new TypeReference<DynamicBytes>() {}, new TypeReference<DynamicBytes>() {}));
    ;

    public static final Event MANAGEREDITED_EVENT = new Event("ManagerEdited", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}));
    ;

    public static final Event MODULEADDED_EVENT = new Event("ModuleAdded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}));
    ;

    public static final Event MODULEINITIALIZED_EVENT = new Event("ModuleInitialized", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}));
    ;

    public static final Event MODULEREMOVED_EVENT = new Event("ModuleRemoved", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}));
    ;

    public static final Event PENDINGMODULEREMOVED_EVENT = new Event("PendingModuleRemoved", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}));
    ;

    public static final Event POSITIONMODULEADDED_EVENT = new Event("PositionModuleAdded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event POSITIONMODULEREMOVED_EVENT = new Event("PositionModuleRemoved", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event POSITIONMULTIPLIEREDITED_EVENT = new Event("PositionMultiplierEdited", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
    ;

    public static final Event TRANSFER_EVENT = new Event("Transfer", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected SetToken_945381920(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected SetToken_945381920(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected SetToken_945381920(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected SetToken_945381920(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public List<ApprovalEventResponse> getApprovalEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(APPROVAL_EVENT, transactionReceipt);
        ArrayList<ApprovalEventResponse> responses = new ArrayList<ApprovalEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ApprovalEventResponse typedResponse = new ApprovalEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.spender = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<ApprovalEventResponse> approvalEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, ApprovalEventResponse>() {
            @Override
            public ApprovalEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(APPROVAL_EVENT, log);
                ApprovalEventResponse typedResponse = new ApprovalEventResponse();
                typedResponse.log = log;
                typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.spender = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<ApprovalEventResponse> approvalEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(APPROVAL_EVENT));
        return approvalEventFlowable(filter);
    }

    public List<ComponentAddedEventResponse> getComponentAddedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(COMPONENTADDED_EVENT, transactionReceipt);
        ArrayList<ComponentAddedEventResponse> responses = new ArrayList<ComponentAddedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ComponentAddedEventResponse typedResponse = new ComponentAddedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._component = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<ComponentAddedEventResponse> componentAddedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, ComponentAddedEventResponse>() {
            @Override
            public ComponentAddedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(COMPONENTADDED_EVENT, log);
                ComponentAddedEventResponse typedResponse = new ComponentAddedEventResponse();
                typedResponse.log = log;
                typedResponse._component = (String) eventValues.getIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<ComponentAddedEventResponse> componentAddedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(COMPONENTADDED_EVENT));
        return componentAddedEventFlowable(filter);
    }

    public List<ComponentRemovedEventResponse> getComponentRemovedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(COMPONENTREMOVED_EVENT, transactionReceipt);
        ArrayList<ComponentRemovedEventResponse> responses = new ArrayList<ComponentRemovedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ComponentRemovedEventResponse typedResponse = new ComponentRemovedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._component = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<ComponentRemovedEventResponse> componentRemovedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, ComponentRemovedEventResponse>() {
            @Override
            public ComponentRemovedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(COMPONENTREMOVED_EVENT, log);
                ComponentRemovedEventResponse typedResponse = new ComponentRemovedEventResponse();
                typedResponse.log = log;
                typedResponse._component = (String) eventValues.getIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<ComponentRemovedEventResponse> componentRemovedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(COMPONENTREMOVED_EVENT));
        return componentRemovedEventFlowable(filter);
    }

    public List<DefaultPositionUnitEditedEventResponse> getDefaultPositionUnitEditedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(DEFAULTPOSITIONUNITEDITED_EVENT, transactionReceipt);
        ArrayList<DefaultPositionUnitEditedEventResponse> responses = new ArrayList<DefaultPositionUnitEditedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DefaultPositionUnitEditedEventResponse typedResponse = new DefaultPositionUnitEditedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._component = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse._realUnit = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<DefaultPositionUnitEditedEventResponse> defaultPositionUnitEditedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, DefaultPositionUnitEditedEventResponse>() {
            @Override
            public DefaultPositionUnitEditedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(DEFAULTPOSITIONUNITEDITED_EVENT, log);
                DefaultPositionUnitEditedEventResponse typedResponse = new DefaultPositionUnitEditedEventResponse();
                typedResponse.log = log;
                typedResponse._component = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse._realUnit = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<DefaultPositionUnitEditedEventResponse> defaultPositionUnitEditedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DEFAULTPOSITIONUNITEDITED_EVENT));
        return defaultPositionUnitEditedEventFlowable(filter);
    }

    public List<ExternalPositionDataEditedEventResponse> getExternalPositionDataEditedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(EXTERNALPOSITIONDATAEDITED_EVENT, transactionReceipt);
        ArrayList<ExternalPositionDataEditedEventResponse> responses = new ArrayList<ExternalPositionDataEditedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ExternalPositionDataEditedEventResponse typedResponse = new ExternalPositionDataEditedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._component = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse._positionModule = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse._data = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<ExternalPositionDataEditedEventResponse> externalPositionDataEditedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, ExternalPositionDataEditedEventResponse>() {
            @Override
            public ExternalPositionDataEditedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(EXTERNALPOSITIONDATAEDITED_EVENT, log);
                ExternalPositionDataEditedEventResponse typedResponse = new ExternalPositionDataEditedEventResponse();
                typedResponse.log = log;
                typedResponse._component = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse._positionModule = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse._data = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<ExternalPositionDataEditedEventResponse> externalPositionDataEditedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(EXTERNALPOSITIONDATAEDITED_EVENT));
        return externalPositionDataEditedEventFlowable(filter);
    }

    public List<ExternalPositionUnitEditedEventResponse> getExternalPositionUnitEditedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(EXTERNALPOSITIONUNITEDITED_EVENT, transactionReceipt);
        ArrayList<ExternalPositionUnitEditedEventResponse> responses = new ArrayList<ExternalPositionUnitEditedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ExternalPositionUnitEditedEventResponse typedResponse = new ExternalPositionUnitEditedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._component = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse._positionModule = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse._realUnit = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<ExternalPositionUnitEditedEventResponse> externalPositionUnitEditedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, ExternalPositionUnitEditedEventResponse>() {
            @Override
            public ExternalPositionUnitEditedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(EXTERNALPOSITIONUNITEDITED_EVENT, log);
                ExternalPositionUnitEditedEventResponse typedResponse = new ExternalPositionUnitEditedEventResponse();
                typedResponse.log = log;
                typedResponse._component = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse._positionModule = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse._realUnit = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<ExternalPositionUnitEditedEventResponse> externalPositionUnitEditedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(EXTERNALPOSITIONUNITEDITED_EVENT));
        return externalPositionUnitEditedEventFlowable(filter);
    }

    public List<InvokedEventResponse> getInvokedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(INVOKED_EVENT, transactionReceipt);
        ArrayList<InvokedEventResponse> responses = new ArrayList<InvokedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            InvokedEventResponse typedResponse = new InvokedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._target = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse._value = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
            typedResponse._data = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse._returnValue = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<InvokedEventResponse> invokedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, InvokedEventResponse>() {
            @Override
            public InvokedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(INVOKED_EVENT, log);
                InvokedEventResponse typedResponse = new InvokedEventResponse();
                typedResponse.log = log;
                typedResponse._target = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse._value = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
                typedResponse._data = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse._returnValue = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<InvokedEventResponse> invokedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(INVOKED_EVENT));
        return invokedEventFlowable(filter);
    }

    public List<ManagerEditedEventResponse> getManagerEditedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(MANAGEREDITED_EVENT, transactionReceipt);
        ArrayList<ManagerEditedEventResponse> responses = new ArrayList<ManagerEditedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ManagerEditedEventResponse typedResponse = new ManagerEditedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._newManager = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse._oldManager = (String) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<ManagerEditedEventResponse> managerEditedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, ManagerEditedEventResponse>() {
            @Override
            public ManagerEditedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(MANAGEREDITED_EVENT, log);
                ManagerEditedEventResponse typedResponse = new ManagerEditedEventResponse();
                typedResponse.log = log;
                typedResponse._newManager = (String) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse._oldManager = (String) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<ManagerEditedEventResponse> managerEditedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(MANAGEREDITED_EVENT));
        return managerEditedEventFlowable(filter);
    }

    public List<ModuleAddedEventResponse> getModuleAddedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(MODULEADDED_EVENT, transactionReceipt);
        ArrayList<ModuleAddedEventResponse> responses = new ArrayList<ModuleAddedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ModuleAddedEventResponse typedResponse = new ModuleAddedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._module = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<ModuleAddedEventResponse> moduleAddedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, ModuleAddedEventResponse>() {
            @Override
            public ModuleAddedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(MODULEADDED_EVENT, log);
                ModuleAddedEventResponse typedResponse = new ModuleAddedEventResponse();
                typedResponse.log = log;
                typedResponse._module = (String) eventValues.getIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<ModuleAddedEventResponse> moduleAddedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(MODULEADDED_EVENT));
        return moduleAddedEventFlowable(filter);
    }

    public List<ModuleInitializedEventResponse> getModuleInitializedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(MODULEINITIALIZED_EVENT, transactionReceipt);
        ArrayList<ModuleInitializedEventResponse> responses = new ArrayList<ModuleInitializedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ModuleInitializedEventResponse typedResponse = new ModuleInitializedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._module = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<ModuleInitializedEventResponse> moduleInitializedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, ModuleInitializedEventResponse>() {
            @Override
            public ModuleInitializedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(MODULEINITIALIZED_EVENT, log);
                ModuleInitializedEventResponse typedResponse = new ModuleInitializedEventResponse();
                typedResponse.log = log;
                typedResponse._module = (String) eventValues.getIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<ModuleInitializedEventResponse> moduleInitializedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(MODULEINITIALIZED_EVENT));
        return moduleInitializedEventFlowable(filter);
    }

    public List<ModuleRemovedEventResponse> getModuleRemovedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(MODULEREMOVED_EVENT, transactionReceipt);
        ArrayList<ModuleRemovedEventResponse> responses = new ArrayList<ModuleRemovedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ModuleRemovedEventResponse typedResponse = new ModuleRemovedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._module = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<ModuleRemovedEventResponse> moduleRemovedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, ModuleRemovedEventResponse>() {
            @Override
            public ModuleRemovedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(MODULEREMOVED_EVENT, log);
                ModuleRemovedEventResponse typedResponse = new ModuleRemovedEventResponse();
                typedResponse.log = log;
                typedResponse._module = (String) eventValues.getIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<ModuleRemovedEventResponse> moduleRemovedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(MODULEREMOVED_EVENT));
        return moduleRemovedEventFlowable(filter);
    }

    public List<PendingModuleRemovedEventResponse> getPendingModuleRemovedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(PENDINGMODULEREMOVED_EVENT, transactionReceipt);
        ArrayList<PendingModuleRemovedEventResponse> responses = new ArrayList<PendingModuleRemovedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            PendingModuleRemovedEventResponse typedResponse = new PendingModuleRemovedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._module = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<PendingModuleRemovedEventResponse> pendingModuleRemovedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, PendingModuleRemovedEventResponse>() {
            @Override
            public PendingModuleRemovedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(PENDINGMODULEREMOVED_EVENT, log);
                PendingModuleRemovedEventResponse typedResponse = new PendingModuleRemovedEventResponse();
                typedResponse.log = log;
                typedResponse._module = (String) eventValues.getIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<PendingModuleRemovedEventResponse> pendingModuleRemovedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(PENDINGMODULEREMOVED_EVENT));
        return pendingModuleRemovedEventFlowable(filter);
    }

    public List<PositionModuleAddedEventResponse> getPositionModuleAddedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(POSITIONMODULEADDED_EVENT, transactionReceipt);
        ArrayList<PositionModuleAddedEventResponse> responses = new ArrayList<PositionModuleAddedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            PositionModuleAddedEventResponse typedResponse = new PositionModuleAddedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._component = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse._positionModule = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<PositionModuleAddedEventResponse> positionModuleAddedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, PositionModuleAddedEventResponse>() {
            @Override
            public PositionModuleAddedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(POSITIONMODULEADDED_EVENT, log);
                PositionModuleAddedEventResponse typedResponse = new PositionModuleAddedEventResponse();
                typedResponse.log = log;
                typedResponse._component = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse._positionModule = (String) eventValues.getIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<PositionModuleAddedEventResponse> positionModuleAddedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(POSITIONMODULEADDED_EVENT));
        return positionModuleAddedEventFlowable(filter);
    }

    public List<PositionModuleRemovedEventResponse> getPositionModuleRemovedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(POSITIONMODULEREMOVED_EVENT, transactionReceipt);
        ArrayList<PositionModuleRemovedEventResponse> responses = new ArrayList<PositionModuleRemovedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            PositionModuleRemovedEventResponse typedResponse = new PositionModuleRemovedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._component = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse._positionModule = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<PositionModuleRemovedEventResponse> positionModuleRemovedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, PositionModuleRemovedEventResponse>() {
            @Override
            public PositionModuleRemovedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(POSITIONMODULEREMOVED_EVENT, log);
                PositionModuleRemovedEventResponse typedResponse = new PositionModuleRemovedEventResponse();
                typedResponse.log = log;
                typedResponse._component = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse._positionModule = (String) eventValues.getIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<PositionModuleRemovedEventResponse> positionModuleRemovedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(POSITIONMODULEREMOVED_EVENT));
        return positionModuleRemovedEventFlowable(filter);
    }

    public List<PositionMultiplierEditedEventResponse> getPositionMultiplierEditedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(POSITIONMULTIPLIEREDITED_EVENT, transactionReceipt);
        ArrayList<PositionMultiplierEditedEventResponse> responses = new ArrayList<PositionMultiplierEditedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            PositionMultiplierEditedEventResponse typedResponse = new PositionMultiplierEditedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._newMultiplier = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<PositionMultiplierEditedEventResponse> positionMultiplierEditedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, PositionMultiplierEditedEventResponse>() {
            @Override
            public PositionMultiplierEditedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(POSITIONMULTIPLIEREDITED_EVENT, log);
                PositionMultiplierEditedEventResponse typedResponse = new PositionMultiplierEditedEventResponse();
                typedResponse.log = log;
                typedResponse._newMultiplier = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<PositionMultiplierEditedEventResponse> positionMultiplierEditedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(POSITIONMULTIPLIEREDITED_EVENT));
        return positionMultiplierEditedEventFlowable(filter);
    }

    public List<TransferEventResponse> getTransferEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(TRANSFER_EVENT, transactionReceipt);
        ArrayList<TransferEventResponse> responses = new ArrayList<TransferEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TransferEventResponse typedResponse = new TransferEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<TransferEventResponse> transferEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, TransferEventResponse>() {
            @Override
            public TransferEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(TRANSFER_EVENT, log);
                TransferEventResponse typedResponse = new TransferEventResponse();
                typedResponse.log = log;
                typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<TransferEventResponse> transferEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TRANSFER_EVENT));
        return transferEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> send_addComponent(String _component) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_ADDCOMPONENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _component)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_addExternalPositionModule(String _component, String _positionModule) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_ADDEXTERNALPOSITIONMODULE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _component), 
                new org.web3j.abi.datatypes.Address(160, _positionModule)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_addModule(String _module) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_ADDMODULE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _module)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> call_allowance(String owner, String spender) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ALLOWANCE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, owner), 
                new org.web3j.abi.datatypes.Address(160, spender)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_allowance(String owner, String spender) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_ALLOWANCE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, owner), 
                new org.web3j.abi.datatypes.Address(160, spender)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_approve(String spender, BigInteger amount) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_APPROVE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, spender), 
                new org.web3j.abi.datatypes.generated.Uint256(amount)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> call_balanceOf(String account) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_BALANCEOF, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, account)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_balanceOf(String account) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_BALANCEOF, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, account)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_burn(String _account, BigInteger _quantity) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_BURN, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _account), 
                new org.web3j.abi.datatypes.generated.Uint256(_quantity)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> call_components(BigInteger param0) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_COMPONENTS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_components(BigInteger param0) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_COMPONENTS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
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

    public RemoteFunctionCall<BigInteger> call_decimals() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_DECIMALS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_decimals() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_DECIMALS, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_decreaseAllowance(String spender, BigInteger subtractedValue) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_DECREASEALLOWANCE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, spender), 
                new org.web3j.abi.datatypes.generated.Uint256(subtractedValue)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_editDefaultPositionUnit(String _component, BigInteger _realUnit) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_EDITDEFAULTPOSITIONUNIT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _component), 
                new org.web3j.abi.datatypes.generated.Int256(_realUnit)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_editExternalPositionData(String _component, String _positionModule, byte[] _data) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_EDITEXTERNALPOSITIONDATA, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _component), 
                new org.web3j.abi.datatypes.Address(160, _positionModule), 
                new org.web3j.abi.datatypes.DynamicBytes(_data)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_editExternalPositionUnit(String _component, String _positionModule, BigInteger _realUnit) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_EDITEXTERNALPOSITIONUNIT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _component), 
                new org.web3j.abi.datatypes.Address(160, _positionModule), 
                new org.web3j.abi.datatypes.generated.Int256(_realUnit)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_editPositionMultiplier(BigInteger _newMultiplier) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_EDITPOSITIONMULTIPLIER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Int256(_newMultiplier)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<List> call_getComponents() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETCOMPONENTS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Address>>() {}));
        return new RemoteFunctionCall<List>(function,
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> send_getComponents() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_GETCOMPONENTS, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> call_getDefaultPositionRealUnit(String _component) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETDEFAULTPOSITIONREALUNIT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _component)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_getDefaultPositionRealUnit(String _component) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_GETDEFAULTPOSITIONREALUNIT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _component)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<byte[]> call_getExternalPositionData(String _component, String _positionModule) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETEXTERNALPOSITIONDATA, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _component), 
                new org.web3j.abi.datatypes.Address(160, _positionModule)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_getExternalPositionData(String _component, String _positionModule) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_GETEXTERNALPOSITIONDATA, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _component), 
                new org.web3j.abi.datatypes.Address(160, _positionModule)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<List> call_getExternalPositionModules(String _component) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETEXTERNALPOSITIONMODULES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _component)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Address>>() {}));
        return new RemoteFunctionCall<List>(function,
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> send_getExternalPositionModules(String _component) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_GETEXTERNALPOSITIONMODULES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _component)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> call_getExternalPositionRealUnit(String _component, String _positionModule) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETEXTERNALPOSITIONREALUNIT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _component), 
                new org.web3j.abi.datatypes.Address(160, _positionModule)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_getExternalPositionRealUnit(String _component, String _positionModule) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_GETEXTERNALPOSITIONREALUNIT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _component), 
                new org.web3j.abi.datatypes.Address(160, _positionModule)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<List> call_getModules() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETMODULES, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Address>>() {}));
        return new RemoteFunctionCall<List>(function,
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> send_getModules() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_GETMODULES, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public void call_getPositions() {
    }

    public RemoteFunctionCall<BigInteger> call_getTotalComponentRealUnits(String _component) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETTOTALCOMPONENTREALUNITS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _component)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_getTotalComponentRealUnits(String _component) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_GETTOTALCOMPONENTREALUNITS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _component)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_increaseAllowance(String spender, BigInteger addedValue) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_INCREASEALLOWANCE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, spender), 
                new org.web3j.abi.datatypes.generated.Uint256(addedValue)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_initializeModule() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_INITIALIZEMODULE, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_invoke(String _target, BigInteger _value, byte[] _data) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_INVOKE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _target), 
                new org.web3j.abi.datatypes.generated.Uint256(_value), 
                new org.web3j.abi.datatypes.DynamicBytes(_data)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Boolean> call_isComponent(String _component) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ISCOMPONENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _component)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_isComponent(String _component) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_ISCOMPONENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _component)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Boolean> call_isExternalPositionModule(String _component, String _module) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ISEXTERNALPOSITIONMODULE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _component), 
                new org.web3j.abi.datatypes.Address(160, _module)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_isExternalPositionModule(String _component, String _module) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_ISEXTERNALPOSITIONMODULE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _component), 
                new org.web3j.abi.datatypes.Address(160, _module)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Boolean> call_isInitializedModule(String _module) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ISINITIALIZEDMODULE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _module)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_isInitializedModule(String _module) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_ISINITIALIZEDMODULE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _module)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Boolean> call_isLocked() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ISLOCKED, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_isLocked() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_ISLOCKED, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Boolean> call_isPendingModule(String _module) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ISPENDINGMODULE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _module)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_isPendingModule(String _module) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_ISPENDINGMODULE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _module)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_lock() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_LOCK, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> call_locker() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_LOCKER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_locker() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_LOCKER, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> call_manager() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_MANAGER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_manager() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_MANAGER, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_mint(String _account, BigInteger _quantity) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_MINT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _account), 
                new org.web3j.abi.datatypes.generated.Uint256(_quantity)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> call_moduleStates(String param0) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_MODULESTATES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_moduleStates(String param0) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_MODULESTATES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, param0)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> call_modules(BigInteger param0) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_MODULES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_modules(BigInteger param0) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_MODULES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> call_name() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_NAME, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_name() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_NAME, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> call_positionMultiplier() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_POSITIONMULTIPLIER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_positionMultiplier() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_POSITIONMULTIPLIER, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_removeComponent(String _component) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_REMOVECOMPONENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _component)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_removeExternalPositionModule(String _component, String _positionModule) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_REMOVEEXTERNALPOSITIONMODULE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _component), 
                new org.web3j.abi.datatypes.Address(160, _positionModule)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_removeModule(String _module) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_REMOVEMODULE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _module)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_removePendingModule(String _module) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_REMOVEPENDINGMODULE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _module)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_setManager(String _manager) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_SETMANAGER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _manager)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> call_symbol() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_SYMBOL, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_symbol() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_SYMBOL, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> call_totalSupply() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_TOTALSUPPLY, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> send_totalSupply() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_TOTALSUPPLY, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_transfer(String recipient, BigInteger amount) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_TRANSFER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, recipient), 
                new org.web3j.abi.datatypes.generated.Uint256(amount)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_transferFrom(String sender, String recipient, BigInteger amount) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_TRANSFERFROM, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, sender), 
                new org.web3j.abi.datatypes.Address(160, recipient), 
                new org.web3j.abi.datatypes.generated.Uint256(amount)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> send_unlock() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_UNLOCK, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static SetToken_945381920 load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new SetToken_945381920(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static SetToken_945381920 load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new SetToken_945381920(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static SetToken_945381920 load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new SetToken_945381920(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static SetToken_945381920 load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new SetToken_945381920(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static class ApprovalEventResponse extends BaseEventResponse {
        public String owner;

        public String spender;

        public BigInteger value;
    }

    public static class ComponentAddedEventResponse extends BaseEventResponse {
        public String _component;
    }

    public static class ComponentRemovedEventResponse extends BaseEventResponse {
        public String _component;
    }

    public static class DefaultPositionUnitEditedEventResponse extends BaseEventResponse {
        public String _component;

        public BigInteger _realUnit;
    }

    public static class ExternalPositionDataEditedEventResponse extends BaseEventResponse {
        public String _component;

        public String _positionModule;

        public byte[] _data;
    }

    public static class ExternalPositionUnitEditedEventResponse extends BaseEventResponse {
        public String _component;

        public String _positionModule;

        public BigInteger _realUnit;
    }

    public static class InvokedEventResponse extends BaseEventResponse {
        public String _target;

        public BigInteger _value;

        public byte[] _data;

        public byte[] _returnValue;
    }

    public static class ManagerEditedEventResponse extends BaseEventResponse {
        public String _newManager;

        public String _oldManager;
    }

    public static class ModuleAddedEventResponse extends BaseEventResponse {
        public String _module;
    }

    public static class ModuleInitializedEventResponse extends BaseEventResponse {
        public String _module;
    }

    public static class ModuleRemovedEventResponse extends BaseEventResponse {
        public String _module;
    }

    public static class PendingModuleRemovedEventResponse extends BaseEventResponse {
        public String _module;
    }

    public static class PositionModuleAddedEventResponse extends BaseEventResponse {
        public String _component;

        public String _positionModule;
    }

    public static class PositionModuleRemovedEventResponse extends BaseEventResponse {
        public String _component;

        public String _positionModule;
    }

    public static class PositionMultiplierEditedEventResponse extends BaseEventResponse {
        public BigInteger _newMultiplier;
    }

    public static class TransferEventResponse extends BaseEventResponse {
        public String from;

        public String to;

        public BigInteger value;
    }
}
