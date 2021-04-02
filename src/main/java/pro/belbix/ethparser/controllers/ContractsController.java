package pro.belbix.ethparser.controllers;

import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collection;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    RestResponse vaults() {
        try {
            Collection<VaultEntity> vaults = ContractUtils.getAllVaults();
            return RestResponse.ok(objectMapper.writeValueAsString(vaults));
        } catch (Exception e) {
            log.error("Error vaults request", e.fillInStackTrace());
            return RestResponse.error("Server error");
        }
    }

    @GetMapping(value = "/contracts/pools")
    RestResponse pools() {
        try {
            Collection<PoolEntity> pools = ContractUtils.getAllPools();
            return RestResponse.ok(objectMapper.writeValueAsString(pools));
        } catch (Exception e) {
            log.error("Error pools request", e.fillInStackTrace());
            return RestResponse.error("Server error");
        }
    }

    @GetMapping(value = "/contracts/tokens")
    RestResponse tokens() {
        try {
            Collection<TokenEntity> tokens = ContractUtils.getAllTokens();
            return RestResponse.ok(objectMapper.writeValueAsString(tokens));
        } catch (Exception e) {
            log.error("Error tokens request", e.fillInStackTrace());
            return RestResponse.error("Server error");
        }
    }

    @GetMapping(value = "/contracts/unipairs")
    RestResponse uniPairs() {
        try {
            Collection<UniPairEntity> uniPairs = ContractUtils.getAllUniPairs();
            return RestResponse.ok(objectMapper.writeValueAsString(uniPairs));
        } catch (Exception e) {
            log.error("Error uniPairs request", e.fillInStackTrace());
            return RestResponse.error("Server error");
        }
    }
}
