package pro.belbix.ethparser.web3.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.controllers.HardWorkController;
import pro.belbix.ethparser.model.RestResponse;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.harvest.HardWorkCalculator;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class HardWorkControllerTest {

    @SpyBean
    private EthBlockService ethBlockService;
    @SpyBean
    private HardWorkCalculator hardWorkCalculator;

    @Autowired
    private HardWorkController hardWorksController;

    final long fakeBlock = 11925259L;
    final String fakeEthAddr = "0xc3882fb25d3cc2e0933841e7f89544caf2d2ca73";

    @Before
    public void setUp() {
        doReturn(fakeBlock).when(ethBlockService).getLastBlock();
    }

    @Test
    public void shouldTotalCalculateHardWorksFeeByPeriodsAndVault() {
        RestResponse response = hardWorksController.totalSavedGasFeeByEthAddress(fakeEthAddr);
        String data = response.getData();
        assertEquals("592,92464145", data);
    }

    @Test
    public void shouldHandleException() {
        when(hardWorkCalculator.calculateTotalHardWorksFeeByOwner(fakeEthAddr))
            .thenThrow(NullPointerException.class);
        RestResponse response = hardWorksController.totalSavedGasFeeByEthAddress(fakeEthAddr);
        String code = response.getCode();
        String message = response.getStatus();
        assertEquals("500", code);
        String expectedMsg = "Error get total saved gas fee for address: " + fakeEthAddr;
        assertEquals(expectedMsg, message);
    }
}
