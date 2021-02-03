package pro.belbix.ethparser.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;;

@Data
@NoArgsConstructor
public class VaultsResponse {

    @JsonInclude(Include.NON_NULL)
    private List<VaultsModel> vaults;
    @JsonInclude(Include.NON_NULL)
    private VaultsModel vault;
    private String code;
    private String status;
    

    public VaultsResponse(List<VaultsModel> vaults, String code, String status) {
        this.vaults = vaults;
        this.code = code;
        this.status = status;
    }

    public VaultsResponse(VaultsModel vault, String code, String status) {
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

    public static VaultsResponse vaults(List<VaultsModel> vaults) {
        return new VaultsResponse(
            vaults,
            "200",
            "OK"
        );
    }

    public static VaultsResponse vault(VaultsModel vault) {
        return new VaultsResponse(
            vault,
            "200",
            "OK"
        );
    }
}
