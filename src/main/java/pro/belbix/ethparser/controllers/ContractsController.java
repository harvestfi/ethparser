package pro.belbix.ethparser.controllers;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collection;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.model.RestResponse;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.entity.contracts.VaultEntity;
import pro.belbix.ethparser.entity.contracts.PoolEntity;
import pro.belbix.ethparser.entity.contracts.TokenEntity;
import pro.belbix.ethparser.entity.contracts.UniPairEntity;


@RestController
@Log4j2
public class ContractsController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping(value = "/contracts/vaults")
    RestResponse vaults(@RequestParam(value = "network", required = false) String network) {
        try {
            if (network == null || Strings.isBlank(network)) {
                network = ETH_NETWORK;
            }
            Collection<VaultEntity> vaults = ContractUtils.getInstance(network).getAllVaults();
            return RestResponse.ok(objectMapper.writeValueAsString(vaults));
        } catch (Exception e) {
            log.error("Error vaults request", e.fillInStackTrace());
            return RestResponse.error("Server error");
        }
    }

    @GetMapping(value = "/contracts/pools")
    RestResponse pools(@RequestParam(value = "network", required = false) String network) {
        try {
            if (network == null || Strings.isBlank(network)) {
                network = ETH_NETWORK;
            }
            Collection<PoolEntity> pools = ContractUtils.getInstance(network).getAllPools();
            return RestResponse.ok(objectMapper.writeValueAsString(pools));
        } catch (Exception e) {
            log.error("Error pools request", e.fillInStackTrace());
            return RestResponse.error("Server error");
        }
    }

    @GetMapping(value = "/contracts/tokens")
    RestResponse tokens(@RequestParam(value = "network", required = false) String network) {
        try {
            if (network == null || Strings.isBlank(network)) {
                network = ETH_NETWORK;
            }
            Collection<TokenEntity> tokens = ContractUtils.getInstance(network).getAllTokens();
            return RestResponse.ok(objectMapper.writeValueAsString(tokens));
        } catch (Exception e) {
            log.error("Error tokens request", e.fillInStackTrace());
            return RestResponse.error("Server error");
        }
    }

    @GetMapping(value = "/contracts/lps")
    RestResponse lps(@RequestParam(value = "network", required = false) String network) {
        try {
            if (network == null || Strings.isBlank(network)) {
                network = ETH_NETWORK;
            }
            Collection<UniPairEntity> uniPairs = ContractUtils.getInstance(network).getAllUniPairs();
            return RestResponse.ok(objectMapper.writeValueAsString(uniPairs));
        } catch (Exception e) {
            log.error("Error uniPairs request", e.fillInStackTrace());
            return RestResponse.error("Server error");
        }
    }
}
