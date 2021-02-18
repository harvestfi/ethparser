package pro.belbix.ethparser.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import pro.belbix.ethparser.entity.eth.VaultEntity;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;;

@Data
@NoArgsConstructor
public class VaultsResponse {

    @JsonInclude(Include.NON_NULL)
    private Collection<VaultEntity> vaults;
    @JsonInclude(Include.NON_NULL)
    private VaultEntity vault;
    private String code;
    private String status;
    

    public VaultsResponse(Collection<VaultEntity> vaults, String code, String status) {
        this.vaults = vaults;
        this.code = code;
        this.status = status;
    }

    public VaultsResponse(VaultEntity vault, String code, String status) {
        this.vault = vault;
        this.code = code;
        this.status = status;
    }

    public VaultsResponse(String code, String status) {
        this.code = code;
        this.status = status;
    }

    public static VaultsResponse error(String status) {
        return new VaultsResponse(
            "500",
            status
        );
    }

    public static VaultsResponse vaults(Collection<VaultEntity> vaults) {
        return new VaultsResponse(
            vaults,
            "200",
            "OK"
        );
    }

    public static VaultsResponse vault(VaultEntity vault) {
        return new VaultsResponse(
            vault,
            "200",
            "OK"
        );
    }
}
