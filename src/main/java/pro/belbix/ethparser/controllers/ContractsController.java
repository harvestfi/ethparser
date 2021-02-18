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
import pro.belbix.ethparser.entity.eth.VaultEntity;
import pro.belbix.ethparser.entity.eth.PoolEntity;
import pro.belbix.ethparser.entity.eth.TokenEntity;
import pro.belbix.ethparser.entity.eth.UniPairEntity;


@RestController
@Log4j2
public class ContractsController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping(value = "/contracts/vault/{vault}")
    RestResponse vaultDetail(@PathVariable("vault") String vault) {
        Optional<VaultEntity> vaultEntity;
        try {
            if (vault.startsWith("0x")) {
                vaultEntity = ContractUtils.getVaultByAddress(vault);              
            } else {
                vaultEntity = ContractUtils.getVaultByName(vault);
            }

            if (vaultEntity.isEmpty()) {
                return RestResponse.error("Vault " + vault + " not found");
            }
            return RestResponse.ok(objectMapper.writeValueAsString(vaultEntity.get()));
        } catch (Exception e) {
            log.error("Error vault request", e.fillInStackTrace());
            return RestResponse.error("Server error");
        }
    }

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
 
    @GetMapping(value = "/contracts/pool/{pool}")
    RestResponse poolDetail(@PathVariable("pool") String pool) {
        Optional<PoolEntity> poolEntity;
        try {
            if (pool.startsWith("0x")) {
                poolEntity = ContractUtils.getPoolByAddress(pool);              
            } else {
                poolEntity = ContractUtils.getPoolByName(pool);
            }

            if (poolEntity.isEmpty()) {
                return RestResponse.error("Pool " + pool + " not found");
            }
            return RestResponse.ok(objectMapper.writeValueAsString(poolEntity.get()));
        } catch (Exception e) {
            log.error("Error pool request", e.fillInStackTrace());
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
   
    @GetMapping(value = "/contracts/token/{token}")
    RestResponse tokenDetail(@PathVariable("token") String token) {
        Optional<TokenEntity> tokenEntity;
        try {
            if (token.startsWith("0x")) {
                tokenEntity = ContractUtils.getTokenByAddress(token);              
            } else {
                tokenEntity = ContractUtils.getTokenByName(token);
            }

            if (tokenEntity.isEmpty()) {
                return RestResponse.error("Token " + token + " not found");
            }
            return RestResponse.ok(objectMapper.writeValueAsString(tokenEntity.get()));
        } catch (Exception e) {
            log.error("Error token request", e.fillInStackTrace());
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

    @GetMapping(value = "/contracts/unipair/{uniPair}")
    RestResponse unipairDetail(@PathVariable("uniPair") String uniPair) {
        Optional<UniPairEntity> unipairEntity;
        try {
            if (uniPair.startsWith("0x")) {
                unipairEntity = ContractUtils.getUniPairByAddress(uniPair);              
            } else {
                unipairEntity = ContractUtils.getUniPairByName(uniPair);
            }

            if (unipairEntity.isEmpty()) {
                return RestResponse.error("UniPair " + uniPair + " not found");
            }
            return RestResponse.ok(objectMapper.writeValueAsString(unipairEntity.get()));
        } catch (Exception e) {
            log.error("Error unipair request", e.fillInStackTrace());
            return RestResponse.error("Server error");
        }
    }

    @GetMapping(value = "/contracts/unipairs")
    RestResponse unipairs() {
        try {
            Collection<UniPairEntity> uniPairs = ContractUtils.getAllUniPairs();
            return RestResponse.ok(objectMapper.writeValueAsString(uniPairs));
        } catch (Exception e) {
            log.error("Error unipairs request", e.fillInStackTrace());
            return RestResponse.error("Server error");
        }
    }
}
