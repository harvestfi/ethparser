package pro.belbix.ethparser;

import static org.junit.Assert.assertEquals;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.harvest.db.VaultActionsDBService.aprToApy;

import org.junit.jupiter.api.Test;
import pro.belbix.ethparser.web3.contracts.ContractUtils;

public class CommonTests {

    @Test
    public void testAprToApy() {
        double apr = 0.743;
        double period = 365.0;
        assertEquals(110.06457410361162, aprToApy(apr, period) * 100, 0.0);
    }
}
