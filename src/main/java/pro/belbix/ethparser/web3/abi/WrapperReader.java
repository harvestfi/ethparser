package pro.belbix.ethparser.web3.abi;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.web3j.protocol.core.RemoteFunctionCall;
import pro.belbix.ethparser.web3.abi.contracts.harvest.WrappedVault;

@Log4j2
public class WrapperReader {
    private final static Set<String> excludeMethods = Set.of(
        ""
    );

    public static void collectMethods() {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(WrappedVault.class);
            for (MethodDescriptor methodDescriptor : beanInfo.getMethodDescriptors()) {
                // only static, view and pure
                if(!methodDescriptor.getName().startsWith("call_")) {
                    continue;
                }
                // only remote calls
                if(!RemoteFunctionCall.class.equals(methodDescriptor.getMethod().getReturnType())) {
                    continue;
                }
                // only getters
                if(methodDescriptor.getMethod().getParameterCount() != 0) {
                    continue;
                }
                log.info("method {}", methodDescriptor.getName());
            }
        } catch (Exception e) {
            log.error("Error collect methods", e);
        }
    }

}
