package pro.belbix.ethparser.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.repositories.eth.PoolRepository;
import pro.belbix.ethparser.repositories.eth.StrategyRepository;
import pro.belbix.ethparser.repositories.eth.TokenRepository;
import pro.belbix.ethparser.repositories.eth.UniPairRepository;
import pro.belbix.ethparser.repositories.eth.VaultRepository;


@SpringBootTest(classes = Application.class)
@ContextConfiguration
@AutoConfigureMockMvc
public class ContractsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private VaultRepository vaultRepository;

  @Autowired
  private PoolRepository poolRepository;

  @Autowired
  private StrategyRepository strategyRepository;

  @Autowired
  private TokenRepository tokenRepository;

  @Autowired
  private UniPairRepository uniPairRepository;

  @Test
  public void contractVaults() throws Exception {
    this.mockMvc.perform(get("/contracts/vaults"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(
            objectMapper.writeValueAsString(
                vaultRepository.fetchAllByNetwork(ETH_NETWORK)))));
  }

  @Test
  public void contractPools() throws Exception {
    this.mockMvc.perform(get("/contracts/pools"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(
            objectMapper.writeValueAsString(
                poolRepository.fetchAllByNetwork(ETH_NETWORK)))));
  }

  @Test
  public void contractStrategies() throws Exception {
    this.mockMvc.perform(get("/contracts/strategies"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(
            objectMapper.writeValueAsString(
                strategyRepository.fetchAllByNetwork(ETH_NETWORK)))));
  }

  @Test
  public void contractTokens() throws Exception {
    this.mockMvc.perform(get("/contracts/tokens"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(
            objectMapper.writeValueAsString(
                tokenRepository.fetchAllByNetwork(ETH_NETWORK)))));
  }

  @Test
  public void contractLps() throws Exception {
    this.mockMvc.perform(get("/contracts/lps"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(
            objectMapper.writeValueAsString(
                uniPairRepository.fetchAllByNetwork(ETH_NETWORK)))));
  }

}
