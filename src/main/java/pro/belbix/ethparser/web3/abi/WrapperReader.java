package pro.belbix.ethparser.web3.abi;

import static pro.belbix.ethparser.codegen.ContractGenerator.STUB_CREDENTIALS;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.web3j.abi.datatypes.Event;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.tx.gas.ContractGasProvider;
import pro.belbix.ethparser.web3.MethodDecoder;

@Log4j2
public class WrapperReader {

    private static Map<String, Event> eventsMap;

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

    public static List<Event> collectEvents(Class<?> clazz) {
        List<Event> events = new ArrayList<>();
        try {
            for(Field field : clazz.getDeclaredFields()) {
                if(!field.getName().endsWith("_EVENT")
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

}
