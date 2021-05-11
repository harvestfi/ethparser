package pro.belbix.ethparser.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.repositories.v0.RewardsRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
@AutoConfigureMockMvc
public class RewardControllerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RewardsRepository rewardsRepository;

    @ParameterizedTest
    @ValueSource(strings = {
            "0x145f39B3c6e6a885AA6A8fadE4ca69d64bab69c8",
            "0xf0358e8c3CD5Fa238a29301d0bEa3D63A17bEdBE"})
    public void historyRewardsAddress(String address) throws Exception {
        var daysBack = 1000;
        String expectedResult = objectMapper.writeValueAsString(
                rewardsRepository.fetchRewardsByVaultAfterBlockDate(
                        address,
                        Instant.now().minus(daysBack, ChronoUnit.DAYS).getEpochSecond(),
                        Long.MAX_VALUE,
                        ETH_NETWORK
                )
        );

        this.mockMvc.perform(get("/history/rewards/" + address + "?days=" + daysBack))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResult));
    }

    @Disabled("Need to be fixed in the scope of https://github.com/harvestfi/ethparser/issues/92")
    @ParameterizedTest(name = "{index} => name={0}, address={1}")
    @CsvSource({
            "SUSHI_MIS_USDT, 0x145f39B3c6e6a885AA6A8fadE4ca69d64bab69c8",
            "USDC, 0xf0358e8c3CD5Fa238a29301d0bEa3D63A17bEdBE",
    })
    public void historyRewardsName(String name, String address) throws Exception {
        var daysBack = 1000;
        String expectedResult = objectMapper.writeValueAsString(
                rewardsRepository.fetchRewardsByVaultAfterBlockDate(
                        address,
                        Instant.now().minus(daysBack, ChronoUnit.DAYS).getEpochSecond(),
                        Long.MAX_VALUE,
                        ETH_NETWORK
                )
        );

        this.mockMvc.perform(get("/history/rewards/" + name + "?days=" + daysBack))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResult));
    }

    @Test
    public void transactionLastReword() throws Exception {
        String expectedResult = objectMapper.writeValueAsString(
                rewardsRepository.fetchLastRewards(ETH_NETWORK)
        );
        this.mockMvc.perform(get("/api/transactions/last/reward"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(expectedResult)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0x145f39B3c6e6a885AA6A8fadE4ca69d64bab69c8",
            "0xf0358e8c3CD5Fa238a29301d0bEa3D63A17bEdBE"})
    public void transactionsHistoryRewardAddress(String address) throws Exception {
        String expectedResult = objectMapper.writeValueAsString(
                rewardsRepository.getAllByVaultOrderByBlockDate(
                        address,
                        0,
                        Long.MAX_VALUE, ETH_NETWORK));

        this.mockMvc.perform(get("/api/transactions/history/reward/" + address))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(expectedResult)));
    }

    @Disabled("Need to be fixed in the scope of https://github.com/harvestfi/ethparser/issues/92")
    @ParameterizedTest(name = "{index} => name={0}, address={1}")
    @CsvSource({
            "SUSHI_MIS_USDT, 0x145f39B3c6e6a885AA6A8fadE4ca69d64bab69c8",
            "USDC, 0xf0358e8c3CD5Fa238a29301d0bEa3D63A17bEdBE",
    })
    public void transactionsHistoryRewardName(String name, String address) throws Exception {
        String expectedResult = objectMapper.writeValueAsString(
                rewardsRepository.getAllByVaultOrderByBlockDate(
                        address,
                        0,
                        Long.MAX_VALUE, ETH_NETWORK));

        this.mockMvc.perform(get("/api/transactions/history/reward/" + name))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(expectedResult)));
    }

    @Test
    public void transactionsHistoryReward() throws Exception {
        String expectedResult = objectMapper.writeValueAsString(
                rewardsRepository.getAllOrderByBlockDate(0, Long.MAX_VALUE, ETH_NETWORK)
        );

        this.mockMvc.perform(get("/api/transactions/history/reward?start=0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(expectedResult)));
    }
}
