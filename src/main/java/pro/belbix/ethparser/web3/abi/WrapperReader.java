package pro.belbix.ethparser.web3.abi;

import static pro.belbix.ethparser.codegen.ContractGenerator.STUB_CREDENTIALS;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.web3j.abi.datatypes.Event;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.tx.Contract;
import org.web3j.tx.exceptions.ContractCallException;
import org.web3j.tx.gas.ContractGasProvider;

@Log4j2
public class WrapperReader {

    private static final int RETRY_COUNT = 10;
    private static final String EXEC_REVERTED = "Contract Call has been reverted by the EVM with the reason: 'execution reverted'.";

    public static List<MethodDescriptor> collectMethods(Class<?> clazz) {
        List<MethodDescriptor> result = new ArrayList<>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            for (MethodDescriptor methodDescriptor : beanInfo.getMethodDescriptors()) {
                // only static, view and pure
                if (!methodDescriptor.getName().startsWith("call_")) {
                    continue;
                }
                // only remote calls
                if (!RemoteFunctionCall.class
                    .equals(methodDescriptor.getMethod().getReturnType())) {
                    continue;
                }
                // only getters
                if (methodDescriptor.getMethod().getParameterCount() != 0) {
                    continue;
                }
                result.add(methodDescriptor);
            }
        } catch (Exception e) {
            log.error("Error collect methods", e);
        }
        return result;
    }

    public static Object createWrapperInstance(Class<?> clazz, String address, Web3j web3j) {
        try {
            Method load = clazz.getDeclaredMethod("load",
                String.class, Web3j.class, Credentials.class, ContractGasProvider.class);
            return load
                .invoke(null, address, web3j, STUB_CREDENTIALS, null);
        } catch (Exception e) {
            log.error("Can instantiate class {}", clazz, e);
            throw new RuntimeException(e);
        }
    }

    public static List<Event> collectEvents(Class<?> clazz) {
        List<Event> events = new ArrayList<>();
        try {
            for (Field field : clazz.getDeclaredFields()) {
                if (!field.getName().endsWith("_EVENT")
                    || field.getType() != Event.class) {
                    continue;
                }
                events.add((Event) field.get(null));
            }
        } catch (Exception e) {
            log.error("Error collect events", e);
        }
        return events;
    }

    public static Event extractEvent(String name, Class<?> clazz) {
        try {
            Field field = clazz.getDeclaredField(name);
            return (Event) field.get(null);
        } catch (Exception e) {
            log.error("Error extract event {}", name, e);
            return null;
        }
    }

    public static <T> Optional<T> call(
        String methodName,
        Class<?> clazz,
        String address,
        Web3j web3j,
        Integer block) {
        T callResult = null;
        try {
            Method method = clazz.getDeclaredMethod(methodName);
            Object instance = createWrapperInstance(clazz, address, web3j);
            callResult = callFunction(method, (Contract) instance, block);
        } catch (Exception e) {
            log.error("Error call {} for {}", methodName, address);
        }
        return Optional.ofNullable(callResult);
    }

    public static <T> T callFunction(Method method, Contract instance, Integer block) {
        if (block != null) {
            instance.setDefaultBlockParameter(
                DefaultBlockParameter.valueOf(BigInteger.valueOf(block)));
        }
        int count = 0;
        while (true) {
            try {
                //noinspection unchecked
                return ((RemoteFunctionCall<T>) method.invoke(instance)).send();
            } catch (Exception e) {
                log.error("Error function call", e);
                if (e instanceof ContractCallException) {
                    if (EXEC_REVERTED.equals(e.getMessage())) {
                        break;
                    }
                }
                count++;
                if (count == RETRY_COUNT) {
                    break;
                }
            }
            log.info("Retry func call {}", count);
        }
        return null;
    }

}
