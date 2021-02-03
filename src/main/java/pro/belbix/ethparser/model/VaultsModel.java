package pro.belbix.ethparser.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class VaultsModel {

    private String name;
    private String address;
    private String active;
    private String underlying;
    

    

    public VaultsModel(String name,
                        String address,
                        String active,
                        String underlying) {
        this.name = name;
        this.address = address;
        this.active = active;
        this.underlying = underlying;

    }

    public static VaultsModel init(String name,
                                String address,
                                String active,
                                String underlying) {
        return new VaultsModel(
            name,
            address,
            active,
            underlying
        );
    }


}
