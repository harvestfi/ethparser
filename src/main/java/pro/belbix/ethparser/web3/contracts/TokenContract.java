package pro.belbix.ethparser.web3.contracts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
class TokenContract {

    private final int createdOnBlock;
    private final String name;
    private final String address;
    private final Map<String, Integer> lps = new HashMap<>();

    public TokenContract(int createdOnBlock, String name, String address) {
        this.createdOnBlock = createdOnBlock;
        this.name = name;
        this.address = address.toLowerCase();
    }

    public TokenContract addLp(int useFrom, String lpName) {
        lps.put(lpName, useFrom);
        return this;
    }

    public static List<TokenContract> createTokenContracts(TokenContract... contracts) {
        List<TokenContract> result = new ArrayList<>();
        for (TokenContract contract : contracts) {
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
