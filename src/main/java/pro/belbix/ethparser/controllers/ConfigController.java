package pro.belbix.ethparser.controllers;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.web3.harvest.contracts.Vaults;
import pro.belbix.ethparser.model.VaultsResponse;
import pro.belbix.ethparser.model.VaultsModel;
import pro.belbix.ethparser.web3.harvest.contracts.StakeContracts;

@RestController
@Log4j2
public class ConfigController {

    public ConfigController() {
    }

    @GetMapping(value = "/config/vault/{vault}")
    VaultsResponse vaultDetail(@PathVariable("vault") String vault) {
            String vaultName;
            String vaultHash;
            
        try {
            if (vault.startsWith("0x")) {
                vaultName = Vaults.vaultHashToName.get(vault.toLowerCase());
                if (vaultName == null) {
                    return VaultsResponse.error("Vault " + vault + " not found");
                }     
                vaultHash = vault.toLowerCase();
            } else if (Vaults.vaultNameToHash.containsKey(vault.toUpperCase())) {
                vaultName = vault.toUpperCase();
                vaultHash = Vaults.vaultNameToHash.get(vault.toUpperCase());
       
            } else {
                return VaultsResponse.error("Vault " + vault + " not found");
            }
                       
            VaultsModel vaultResponse = VaultsModel.init(vaultName, 
                                                        vaultHash, 
                                                        checkVaultActive(vaultName), 
                                                        Vaults.underlyingToken.get(vaultHash)
                                                        );
            return VaultsResponse.vault(vaultResponse);
        
        } catch (Exception e) {
            log.error("Error vault request", e.fillInStackTrace());
            return VaultsResponse.error("Server error");
        }
    }

    @GetMapping(value = "/config/vaults")
    VaultsResponse vaults(@RequestParam(value="inactive", required=false) boolean inactive) {
        try {
            List<VaultsModel> vaults = new ArrayList<>();
            Vaults.vaultHashToName.forEach((hash, name) ->
                vaults.add(VaultsModel.init(name, hash, checkVaultActive(name), Vaults.underlyingToken.get(hash)))
            );

            return VaultsResponse.vaults(vaults);
        } catch (Exception e) {
            log.error("Error vaults request", e.fillInStackTrace());
            return VaultsResponse.error("Server error");
        }
    }

    private static String checkVaultActive(String vault) {
        if (Vaults.vaultNameToOldVaultName.containsValue(vault)) {
            return "false";
        }
        return "true";
    }
}
