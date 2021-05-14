package pro.belbix.ethparser.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.repositories.v0.HarvestRepository;
import pro.belbix.ethparser.web3.harvest.db.VaultActionsDBService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
@AutoConfigureMockMvc
public class HarvestControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HarvestRepository harvestRepository;

    @Autowired
    private VaultActionsDBService vaultActionsDBService;

    @Test
    public void transactionsLastHarvest() throws Exception {
        String expectedResult = objectMapper.writeValueAsString(
                harvestRepository.fetchLastTvl(ETH_NETWORK));

        this.mockMvc.perform(get("/api/transactions/last/harvest"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResult));
    }

    @ParameterizedTest
    @ValueSource(strings = {"WBTC", "USDC"})
    public void historyHarvest(String vault) throws Exception {
        String expectedResult = objectMapper.writeValueAsString(
                harvestRepository.findAllByVaultOrderByBlockDate(
                        vault, 0, Long.MAX_VALUE, ETH_NETWORK));

        this.mockMvc.perform(get("/api/transactions/history/harvest/" + vault))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResult));
    }

    @Test
    public void transactionsHistoryHarvestDefault() throws Exception {
        String expectedResult = objectMapper.writeValueAsString(
                vaultActionsDBService.fetchHarvest(null, null, ETH_NETWORK));

        this.mockMvc.perform(get("/api/transactions/history/harvest"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResult));
    }

    @Test
    public void transactionsHistoryHarvestToMax() throws Exception {
        String expectedResult = objectMapper.writeValueAsString(
                vaultActionsDBService.fetchHarvest(null, "2147483647", ETH_NETWORK));

        this.mockMvc.perform(get("/api/transactions/history/harvest?to=2147483647"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResult));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0x3af015f6e3ac79d217198f00ef36af099d223e29",
            "0x858128d2f83dbb226b6cf29bffe5e7e129c3a128",
            "0x7fba36c647cc537a6e08bd981cd8dee6727b0f4f",
            "0xe5350e927b904fdb4d2af55c566e269bb3df1941",
    })
    public void historyHarvestAddress(String address) throws Exception {
        String expectedResult = objectMapper.writeValueAsString(
                harvestRepository.fetchAllByOwner(address, 0, Long.MAX_VALUE, ETH_NETWORK));

        this.mockMvc.perform(get("/history/harvest/" + address))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResult));
    }

    @Test
    public void userBalances() throws Exception {
        String expectedResult = objectMapper.writeValueAsString(
                harvestRepository.fetchOwnerBalances(ETH_NETWORK));

        this.mockMvc.perform(get("/user_balances"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResult));
    }
}
