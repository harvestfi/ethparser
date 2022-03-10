package pro.belbix.ethparser.controllers;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.model.ProfitResult;
import pro.belbix.ethparser.service.ProfitService;

@RestController
@Log4j2
public class ProfitController {

  private final ProfitService profitService;

  public ProfitController(ProfitService profitService){

    this.profitService = profitService;
  }

  @RequestMapping(value = "api/profit/total", method = RequestMethod.GET)
  public Double fetchProfit(
      @RequestParam("address") @Parameter(description = "Owner address") String address,
      @RequestParam(value = "start", required = false, defaultValue = "0")
      @Parameter(description = "Block creation time from") String start,
      @RequestParam(value = "end", required = false, defaultValue = Long.MAX_VALUE + "")
      @Parameter(description = "Block creation time to") String end
  ) {

    return profitService.calculationProfitForPeriod(address, start, end);
  }

  @RequestMapping(value = "api/profit/vaults", method = RequestMethod.GET)
  public Double fetchProfitByVault(
      @RequestParam("address") @Parameter(description = "Vault address") String address,
      @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK) String network,
      @RequestParam(value = "start", required = false, defaultValue = "0")
      @Parameter(description = "Block creation time from") String start,
      @RequestParam(value = "end", required = false, defaultValue = Long.MAX_VALUE + "")
      @Parameter(description = "Block creation time to") String end
  ) {

    return profitService.calculationProfitByVaultForPeriod(address, network, start, end);
  }

  @GetMapping("api/profit")
  public ProfitResult calculateProfit(@RequestParam String address, @RequestParam String network) {
    return new ProfitResult(profitService.calculateProfit(address, network));
  }

  @GetMapping("api/profit/vault")
  public ProfitResult calculateVaultProfit(@RequestParam String address, @RequestParam String network,
      @RequestParam long blockFrom, @RequestParam long blockTo) {
    return new ProfitResult(profitService.calculateVaultProfit(address, network, blockFrom, blockTo));
  }
}
