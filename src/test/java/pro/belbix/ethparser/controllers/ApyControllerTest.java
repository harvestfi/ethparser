package pro.belbix.ethparser.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pro.belbix.ethparser.TestAddresses.V_USDC;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.service.ApyService;
import pro.belbix.ethparser.web3.contracts.ContractConstants;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
@AutoConfigureMockMvc
public class ApyControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ApyService apyService;

  @Test
  public void poolPS() throws Exception {
    this.mockMvc.perform(get("/apy/average/" + ContractConstants.PS_ADDRESS + "?days=1000"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(String.format("%.8f",
            apyService.averageApyForPool(ContractConstants.PS_ADDRESS, 1000, ETH_NETWORK)))));
  }

  @Test
  public void poolUDT() throws Exception {
    this.mockMvc.perform(get("/apy/average/" + V_USDC + "?days=1000"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(String.format("%.8f",
            apyService.averageApyForPool(V_USDC, 1000, ETH_NETWORK)))));
  }

  @Test
  public void poolNonExistingWithDefaultParams() throws Exception {
    this.mockMvc.perform(get("/apy/average/" + ContractConstants.PS_ADDRESS + "?days=1000"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(String.format("%.8f",
            apyService.averageApyForPool("xxx", 700, ETH_NETWORK)))));
  }

  @Test
  public void poolWithInvalidDays() throws Exception {
    this.mockMvc.perform(get("/apy/average/xxx?days=aa"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("500")))
        .andExpect(content().string(containsString("Wrong days value")));
  }

}
