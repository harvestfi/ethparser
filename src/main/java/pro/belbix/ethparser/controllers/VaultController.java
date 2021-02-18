package pro.belbix.ethparser.controllers;

import java.util.Optional;
import java.util.Collection;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.model.VaultsResponse;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.entity.eth.VaultEntity;


@RestController
@Log4j2
public class VaultController {

    @GetMapping(value = "/api/vault/{vault}")
    VaultsResponse vaultDetail(@PathVariable("vault") String vault) {
            Optional<VaultEntity> vaultEntity;
        try {
            if (vault.startsWith("0x")) {
                vaultEntity = ContractUtils.getVaultByAddress(vault);              
            } else {
                vaultEntity = ContractUtils.getVaultByName(vault);
            }

            if (vaultEntity.isPresent()) {
                return VaultsResponse.vault(vaultEntity.get());
            } else {
                return VaultsResponse.error("Vault " + vault + " not found");
            }
        
        } catch (Exception e) {
            log.error("Error vault request", e.fillInStackTrace());
            return VaultsResponse.error("Server error");
        }
    }



    @GetMapping(value = "/api/vaults")
    VaultsResponse vaults() {
        try {
            Collection<VaultEntity> vaults = ContractUtils.getAllVaults();
            return VaultsResponse.vaults(vaults);
        } catch (Exception e) {
            log.error("Error vaults request", e.fillInStackTrace());
            return VaultsResponse.error("Server error");
        }
    }

}
