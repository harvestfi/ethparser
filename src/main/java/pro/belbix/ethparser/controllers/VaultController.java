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


@RestController
@Log4j2
public class VaultController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping(value = "/vault/{vault}")
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

    @GetMapping(value = "/vaults")
    RestResponse vaults() {
        try {
            Collection<VaultEntity> vaults = ContractUtils.getAllVaults();
            return RestResponse.ok(objectMapper.writeValueAsString(vaults));
        } catch (Exception e) {
            log.error("Error vaults request", e.fillInStackTrace());
            return RestResponse.error("Server error");
        }
    }

}
