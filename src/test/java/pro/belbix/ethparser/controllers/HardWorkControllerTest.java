package pro.belbix.ethparser.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.web3j.protocol.ObjectMapperFactory;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.model.PaginatedResponse;
import pro.belbix.ethparser.model.RestResponse;
import pro.belbix.ethparser.repositories.v0.HardWorkRepository;
import pro.belbix.ethparser.web3.harvest.HardWorkCalculator;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
@AutoConfigureMockMvc
public class HardWorkControllerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private HardWorkRepository hardWorkRepository;

  @MockBean
  private HardWorkCalculator hardWorkCalculator;

  @Test
  public void transactionsLastHardwork() throws Exception {
    String expectedResult = objectMapper.writeValueAsString(
        hardWorkRepository.fetchLatest(ETH_NETWORK));

    this.mockMvc.perform(get("/api/transactions/last/hardwork"))
        .andExpect(status().isOk())
        .andExpect(content().string(expectedResult));
  }

  @ParameterizedTest
  @ValueSource(strings = {"WBTC", "USDC", "CRV_HUSD"})
  public void historyHardwork(String vault) throws Exception {
    String expectedResult = objectMapper.writeValueAsString(
        hardWorkRepository.findAllByVaultOrderByBlockDate(
            vault, ETH_NETWORK, 0, Long.MAX_VALUE));

    this.mockMvc.perform(get("/api/transactions/history/hardwork/" + vault))
        .andExpect(status().isOk())
        .andExpect(content().string(expectedResult));
  }

  @Test
  public void lastHistoryHardworkAllDefaults() throws Exception {
    String expectedResult = objectMapper.writeValueAsString(
        hardWorkRepository.fetchAllInRange(0, Long.MAX_VALUE, ETH_NETWORK));

    this.mockMvc.perform(get("/api/transactions/history/hardwork"))
        .andExpect(status().isOk())
        .andExpect(content().string(expectedResult));
  }

  @Test
  public void lastHardworkAllFrom1to42() throws Exception {
    String expectedResult = objectMapper.writeValueAsString(
        hardWorkRepository.fetchAllInRange(1, 42, ETH_NETWORK));

    this.mockMvc.perform(get("/api/transactions/history/hardwork?from=1&to=42"))
        .andExpect(status().isOk())
        .andExpect(content().string(expectedResult));
  }

  @Test
  public void lastSavedGasSum() throws Exception {
    String expectedResult = String.format("%.8f", (
        hardWorkRepository.fetchLastGasSaved(ETH_NETWORK)));

    this.mockMvc.perform(get("/last_saved_gas_sum"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(expectedResult)));
  }

  @Test
  public void totalSavedGasFeeByAddress() throws Exception {

    // mock used here because of internal ethBlockService dependency in hardWorkCalculator
    when(hardWorkCalculator.calculateTotalHardWorksFeeByOwner(
        "0x0000000000000", ETH_NETWORK)).thenReturn(42.00);

    this.mockMvc.perform(get("/total_saved_gas_fee_by_address?address=0x0000000000000"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("42")));
  }

  @Test
  public void lastHardwork() throws Exception {
    String expectedResult = objectMapper.writeValueAsString(
        hardWorkRepository.findFirstByNetworkOrderByBlockDateDesc(ETH_NETWORK));

    this.mockMvc.perform(get("/last/hardwork"))
        .andExpect(status().isOk())
        .andExpect(content().string(expectedResult));
  }

  @Test
  public void hardworkPagesPageSizePageDefaults() throws Exception {
    var pages = hardWorkRepository.fetchPages(Integer.MIN_VALUE, ETH_NETWORK,
        PageRequest.of(0, 5, Sort.by("blockDate")));

    var expectedResult = ObjectMapperFactory
        .getObjectMapper()
        .writeValueAsString(pages.getContent());

    this.mockMvc.perform(get("/hardwork/pages?pageSize=5&page=0"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(expectedResult)));
  }

  @Test
  public void hardworkPagesPageSizePageDefaultsUSDCVault() throws Exception {
    var pages = hardWorkRepository
        .fetchPagesByVault("USDC", ETH_NETWORK, Integer.MIN_VALUE,
            PageRequest.of(0, 5, Sort.by("blockDate")));

    var expectedResult = ObjectMapperFactory
        .getObjectMapper()
        .writeValueAsString(pages.getContent());

    this.mockMvc.perform(get("/hardwork/pages?vault=USDC&pageSize=5&page=0"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(expectedResult)));
  }
}
