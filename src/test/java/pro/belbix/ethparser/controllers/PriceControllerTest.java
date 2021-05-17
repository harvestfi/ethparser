package pro.belbix.ethparser.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.PriceDTO;
import pro.belbix.ethparser.repositories.v0.PriceRepository;
import pro.belbix.ethparser.service.LastDbPricesService;
import pro.belbix.ethparser.web3.prices.PriceProvider;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
@AutoConfigureMockMvc
public class PriceControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PriceProvider priceProvider;

    @Autowired
    private LastDbPricesService lastDbPricesService;

    @Autowired
    private PriceRepository priceRepository;

    @Test
    public void priceLPBadAddress() throws Exception {
        this.mockMvc.perform(get("/price/lp/0011?amount=1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("500")))
                .andExpect(content().string(containsString("Not LP name"))
                );
    }

    @ParameterizedTest(name = "{index} => name={0}, address={1}")
    @CsvSource({
            "UNI_LP_DAI_BSG, 0x4a9596e5d2f9bef50e4de092ad7181ae3c40353e",
            "UNI_LP_BASv2_DAI, 0x3e78f2e7dade07ea685f8612f00477fd97162f1e",
            "UNI_LP_DAI_BSGS, 0x980a07e4f64d21a0cb2ef8d4af362a79b9f5c0da",
            "UNI_LP_UST_mAAPL, 0xb022e08adc8ba2de6ba4fecb59c6d502f66e953b",
    })
    public void priceLPUnsupportedLP(String name, String address) throws Exception {
        String expectedResult = String.format("%.8f",
                priceProvider.getLpTokenUsdPrice(address, 1.0, 12393771, ETH_NETWORK));

        this.mockMvc.perform(get("/price/lp/" + name + "?amount=1&block=12393771"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(expectedResult)));
    }

    @Test
    public void priceTokenAddress() throws Exception {
        String expectedResult = String.format("%.8f",
                priceProvider.getLpTokenUsdPrice(
                        "0xc50ef7861153c51d383d9a7d48e6c9467fb90c38",
                        1, 12393771, ETH_NETWORK)
        );

        this.mockMvc.perform(get("/price/token/0xc50ef7861153c51d383d9a7d48e6c9467fb90c38?block=12393771"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(expectedResult)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "WETH",
        "USDT",
        "DAI",
        "USDC",
        "1INCH",
        "0xEB4C2781e4ebA804CE9a9803C67d0893436bB27D",
        "WBTC",
    })
    public void priceTokenName(String token) throws Exception {
        String expectedResult = String.format("%.8f",
                priceProvider.getPriceForCoin(token, 12393771, ETH_NETWORK)
        );

        this.mockMvc.perform(get("/price/token/"+ token+"?block=12393771"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(expectedResult)));
    }

    @Test
    public void priceTokenLatest() throws Exception {
        String expectedResult = objectMapper.writeValueAsString(
                lastDbPricesService.getLastPrices(ETH_NETWORK));

        this.mockMvc.perform(get("/price/token/latest"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResult));
    }

    @Test
    public void priceTokenDTO() throws Exception {
        List<PriceDTO> prices = priceRepository.fetchLastPriceByTokenAddress(
                "0x795065dCc9f64b5614C407a6EFDC400DA6221FB0",
                Long.MAX_VALUE, ETH_NETWORK, PageRequest.of(0, 1));

        // test db is empty for this query, need to be updated when data will be ready
        String expectedResult = prices.size() == 0 ? "" : objectMapper.writeValueAsString(prices.get(0));

        this.mockMvc.perform(get("/price/token/dto/0x795065dCc9f64b5614C407a6EFDC400DA6221FB0"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResult));
    }

}
