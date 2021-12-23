package pro.belbix.ethparser.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.repositories.v0.HarvestRepository;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
@AutoConfigureMockMvc
public class ProfitControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private HarvestRepository harvestRepository;


  @ParameterizedTest(name = "{index} => address={0}, start={1}, end={2}")
  @CsvSource({
      "0x858128d2f83dbb226b6cf29bffe5e7e129c3a128, 1613588289, 1615434501",
      "0xf1b85414473eccd659deeacee347bbc17171dd32, 1613588289, 1640168651"
  })
  public void testCalculation(String address, String start, String end) throws Exception {

    this.mockMvc.perform(
            get("/api/profit/total?address=" + address + "&start=" + start + "&end=" + end))
        .andExpect(status().isOk());
  }

  @ParameterizedTest(name = "{index} => address={0}, start={1}, end={2}")
  @CsvSource({
      "0xe7c9d242137896741b70cefef701bbb4dcb158ec, matic, 1613588289, 1640168651"
  })
  public void testCalculationByVault(String address, String network, String start, String end)
      throws Exception {

    this.mockMvc.perform(
            get("/api/profit/vault?address=" + address
                + "&network=" + network + "&start=" + start + "&end=" + end))
        .andExpect(status().isOk());
  }

}
