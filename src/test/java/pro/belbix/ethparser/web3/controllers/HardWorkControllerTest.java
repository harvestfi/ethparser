package pro.belbix.ethparser.web3.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.web3j.protocol.ObjectMapperFactory;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.controllers.HardWorkController;
import pro.belbix.ethparser.dto.v0.HardWorkDTO;
import pro.belbix.ethparser.model.PaginatedResponse;
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
    final String owner = "0x858128d2f83dbb226b6cf29bffe5e7e129c3a128";

    @BeforeEach
    public void setUp() {
        doReturn(fakeBlock).when(ethBlockService).getLastBlock(ETH_NETWORK);
    }

    @Test
    public void shouldTotalCalculateHardWorksFeeByPeriodsAndVault() {
        doReturn(fakeBlockDate)
            .when(ethBlockService)
            .getTimestampSecForBlock(fakeBlock, ETH_NETWORK);
        RestResponse response = hardWorksController.totalSavedGasFeeByEthAddress(owner);
        String data = response.getData();
        Assertions.assertEquals("161.97026179", data);
    }

    @Test
    public void shouldHandleException() {
        doThrow(NullPointerException.class)
            .when(hardWorkCalculator)
            .calculateTotalHardWorksFeeByOwner(owner, ETH_NETWORK);
        RestResponse response = hardWorksController.totalSavedGasFeeByEthAddress(owner);
        String code = response.getCode();
        String message = response.getStatus();
        Assertions.assertEquals("500", code);
        String expectedMsg = "Error get total saved gas fee for address: " + owner;
        Assertions.assertEquals(expectedMsg, message);
    }

    @Test
    void testPagination() throws IOException {
        RestResponse response =
            hardWorksController.hardworkPages("2", "0", "desc");
        assertNotNull(response, "null response");
        assertEquals("200", response.getCode(), "code 200");
        assertNotNull(response.getData(), "null data");
        PaginatedResponse<List<HardWorkDTO>> pages = ObjectMapperFactory.getObjectMapper()
            .readValue(response.getData(),
                PaginatedResponse.PaginatedResponseHardWork.class);
        assertEquals(1614137409, pages.getData().get(0).getBlockDate(), "first msg data");
    }
}
