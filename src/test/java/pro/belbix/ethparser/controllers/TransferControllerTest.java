package pro.belbix.ethparser.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.repositories.v0.TransferRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.utils.CommonUtils.parseLong;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
@AutoConfigureMockMvc
public class TransferControllerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransferRepository transferRepository;

    @Test
    public void historyTransferAddress() throws Exception{
        var address = "0xce2a3294f800b1bf9a907db3c7e377cf9486a456";
        String expectedResult = objectMapper.writeValueAsString(
                transferRepository.fetchAllByOwnerAndRecipient(
                        address.toLowerCase(), address, 0, Long.MAX_VALUE, ETH_NETWORK));
        this.mockMvc.perform(get("/history/transfer/" + address))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResult));
    }
}
