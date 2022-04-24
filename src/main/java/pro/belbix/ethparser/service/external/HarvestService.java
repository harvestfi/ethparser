package pro.belbix.ethparser.service.external;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.belbix.ethparser.model.HarvestPoolInfo;
import pro.belbix.ethparser.model.HarvestVaultInfo;
import pro.belbix.ethparser.properties.ExternalProperties;
import pro.belbix.ethparser.utils.UrlUtils.HarvestUrl;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class HarvestService {
  ExternalProperties externalProperties;
  RestTemplate restTemplate;

  public HarvestPoolInfo getPools() {
    var url = String.format(HarvestUrl.POOLS, externalProperties.getHarvest().getUrl(), externalProperties.getHarvest().getKey());
    log.info("Starting get pools from harvest {} ", url);
    return restTemplate.getForObject(url, HarvestPoolInfo.class);
  }

  public HarvestVaultInfo getVaults() {
    var url = String.format(HarvestUrl.VAULTS, externalProperties.getHarvest().getUrl(), externalProperties.getHarvest().getKey());
    log.info("Starting get vaults from harvest {} ", url);
    var test = restTemplate.getForEntity("https://httpbin.org/get", String.class);
    log.info("Test request: {}", test);
    var result =  restTemplate.getForEntity(url, HarvestVaultInfo.class);
    log.info("Result getting vaults from harvest API : {}", result);
    return result.getBody();
  }
}
