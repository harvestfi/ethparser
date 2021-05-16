package pro.belbix.ethparser.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.repositories.v0.UniswapRepository;
import pro.belbix.ethparser.web3.uniswap.db.UniswapDbService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
@AutoConfigureMockMvc
public class UniControllerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UniswapDbService uniswapDbService;

    @Autowired
    private UniswapRepository uniswapRepository;


    @Test
    public void transactionsHistoryUni() throws Exception {

        String expectedResult = objectMapper.writeValueAsString(
                uniswapDbService.fetchUni(null, null));

        this.mockMvc.perform(get("/api/transactions/history/uni"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResult));
    }


    @ParameterizedTest(name = "{index} => name={0}, address={1}")
    @CsvSource({
            "WETH, 0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2",
            "WBTC, 0x2260fac5e5542a773aa44fbcfedf7c193bc2c599",
            "DAI, 0x6b175474e89094c44da98b954eedeac495271d0f",
    })
    public void transactionsHistoryUni(String name, String address) throws Exception {

        // Warning: no data in the test DB
        String expectedResult = objectMapper.writeValueAsString(
                uniswapRepository.fetchOHLCTransactions(
                        address, 0, Long.MAX_VALUE, 3600));

        this.mockMvc.perform(get("/api/transactions/history/uni/ohcl/" + name))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResult));
    }

    @Test
    public void historyUniAddress() throws Exception {
        String address = "0xaa4ba008dc503733c2999089f9cf09ed10755f5e";

        String expectedResult = objectMapper.writeValueAsString(
                uniswapRepository.fetchAllByOwner(address.toLowerCase(), 0, Long.MAX_VALUE));

        this.mockMvc.perform(get("/history/uni/" + address))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResult));
    }


}
