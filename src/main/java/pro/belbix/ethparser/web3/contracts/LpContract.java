package pro.belbix.ethparser.web3.contracts;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
class LpContract {

    private final int createdOnBlock;
    private final String name;
    private final String keyToken;
    private final String address;

    public LpContract(int createdOnBlock, String name, String keyToken, String address) {
        this.createdOnBlock = createdOnBlock;
        this.name = name;
        this.keyToken = keyToken;
        this.address = address.toLowerCase();
    }

    public static List<LpContract> createLpContracts(LpContract... contracts) {
        List<LpContract> result = new ArrayList<>();
        for (LpContract contract : contracts) {
            if (result.stream().anyMatch(v -> v.getAddress().equals(contract.getAddress())
                || v.getName().equals(contract.getName()))) {
                System.out.println("Duplicate contract " + contract.getName());
                System.exit(-1);
            }
            result.add(contract);
        }
        return result;
    }
}
