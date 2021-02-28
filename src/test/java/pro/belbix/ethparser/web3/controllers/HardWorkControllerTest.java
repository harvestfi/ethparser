package pro.belbix.ethparser.web3.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;


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

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class HardWorkControllerTest {

    @SpyBean
    private EthBlockService ethBlockService;

    @Autowired
    private HardWorkController hardWorksController;
    @Autowired
    private ContractLoader contractLoader;

    final long fakeBlock = 11925259L;


    @Before
    public void setUp() {
        doReturn(fakeBlock).when(ethBlockService).getLastBlock();
        contractLoader.load();
    }

    @Test
    public void shouldTotalCalculateHardWorksFeeByPeriodsAndVault() {
        String ethAddr = "0xc3882fb25d3cc2e0933841e7f89544caf2d2ca73";
        RestResponse response = hardWorksController.totalSavedGasFeeByEthAddress(ethAddr);
        String data = response.getData();
        assertEquals("592,92464145", data);
    }

}
