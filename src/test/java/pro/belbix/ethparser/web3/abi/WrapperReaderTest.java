package pro.belbix.ethparser.web3.abi;

import org.junit.Test;
import pro.belbix.ethparser.web3.abi.generated.harvest.VaultProxy_1089971474;

public class WrapperReaderTest {

    @Test
    public void collectMethods() {
        WrapperReader.collectMethods(VaultProxy_1089971474.class);
    }

    @Test
    public void collectEvents() {
        WrapperReader.collectEvents(VaultProxy_1089971474.class);
    }
}
