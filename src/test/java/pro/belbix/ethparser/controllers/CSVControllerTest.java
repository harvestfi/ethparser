package pro.belbix.ethparser.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.HardWorkDTO;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.dto.v0.RewardDTO;
import pro.belbix.ethparser.model.TvlHistory;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
@AutoConfigureMockMvc
public class CSVControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Test
  public void harvestHistoryDataForVault() throws Exception {
    this.mockMvc.perform(get("/csv/transactions/history/harvest/USDC"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(CSVController.collectFields(HarvestDTO.class)[0])));
  }

  @Test
  public void rewardHistoryDataForVault() throws Exception {
    this.mockMvc.perform(get("/csv/transactions/history/reward/USDC"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(CSVController.collectFields(RewardDTO.class)[0])));
  }

  @Test
  public void hardworkHistoryDataForVault() throws Exception {
    this.mockMvc.perform(get("/csv/transactions/history/hardwork/USDC"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(CSVController.collectFields(HardWorkDTO.class)[0])));
  }

  @Test
  public void tvlHistoryDataForVault() throws Exception {
    this.mockMvc.perform(get("/csv/transactions/history/tvl/USDC"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(CSVController.collectFields(TvlHistory.class)[0])));
  }
}
