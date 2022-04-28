package pro.belbix.ethparser.controllers;

import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.entity.HarvestVaultData;
import pro.belbix.ethparser.service.HarvestVaultDataService;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HarvestVaultDataController {
  HarvestVaultDataService harvestVaultDataService;

  @GetMapping("/harvest/vaults")
  @ResponseStatus(HttpStatus.OK)
  public List<HarvestVaultData> getAll() {
    return harvestVaultDataService.getAllVaultInfo();
  }

}
