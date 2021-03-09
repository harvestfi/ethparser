package pro.belbix.ethparser.web3.abi;

import org.junit.Test;
import pro.belbix.ethparser.web3.abi.contracts.harvest.WrappedVault;

public class WrapperReaderTest {

    @Test
    public void collectMethods() {
        WrapperReader.collectMethods();
    }

    @Test
    public void collectEvents() {
        WrapperReader.collectEvents(WrappedVault.class);
    }
}
