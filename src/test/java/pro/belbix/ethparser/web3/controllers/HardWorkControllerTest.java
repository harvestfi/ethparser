package pro.belbix.ethparser.web3.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.controllers.HardWorkController;
import pro.belbix.ethparser.model.RestResponse;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.harvest.HardWorkCalculator;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class HardWorkControllerTest {

    @SpyBean
    private EthBlockService ethBlockService;
    @SpyBean
    private HardWorkCalculator hardWorkCalculator;

    @Autowired
    private HardWorkController hardWorksController;

    final long fakeBlock = 11925259L;
    final long fakeBlockDate = 1614241875L;
    final String fakeEthAddr = "0xc3882fb25d3cc2e0933841e7f89544caf2d2ca73";

    @BeforeEach
    public void setUp() {
        doReturn(fakeBlock).when(ethBlockService).getLastBlock();
    }

    @Test
    public void shouldTotalCalculateHardWorksFeeByPeriodsAndVault() {
        doReturn(fakeBlockDate)
            .when(ethBlockService)
            .getTimestampSecForBlock(null, fakeBlock);
        RestResponse response = hardWorksController.totalSavedGasFeeByEthAddress(fakeEthAddr);
        String data = response.getData();
        assertEquals("0.00000000", data);
    }

    @Test
    public void shouldHandleException() {
        doThrow(NullPointerException.class)
            .when(hardWorkCalculator)
            .calculateTotalHardWorksFeeByOwner(fakeEthAddr);
        RestResponse response = hardWorksController.totalSavedGasFeeByEthAddress(fakeEthAddr);
        String code = response.getCode();
        String message = response.getStatus();
        assertEquals("500", code);
        String expectedMsg = "Error get total saved gas fee for address: " + fakeEthAddr;
        assertEquals(expectedMsg, message);
    }
}
