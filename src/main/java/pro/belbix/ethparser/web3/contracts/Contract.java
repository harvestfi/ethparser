package pro.belbix.ethparser.web3.contracts;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class Contract {

    private int createdOnBlock;
    private final String name;
    private final String address;

    public Contract(int createdOnBlock, String name, String address) {
        this.createdOnBlock = createdOnBlock;
        this.name = name;
        this.address = address.toLowerCase();
    }

    public void setCreatedOnBlock(int createdOnBlock) {
        this.createdOnBlock = createdOnBlock;
    }

    public static List<Contract> createContracts(Contract... contracts) {
        List<Contract> result = new ArrayList<>();
        for (Contract contract : contracts) {
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
