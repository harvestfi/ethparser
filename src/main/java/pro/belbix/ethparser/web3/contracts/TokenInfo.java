package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.web3.ContractConstants.D18;

import java.util.Map.Entry;
import java.util.TreeMap;
import lombok.Data;
import org.web3j.tuples.generated.Tuple2;

@Data
public class TokenInfo {

    private final String tokenName;
    private final String tokenAddress;
    private final long createdOnBlock;
    private double divider = D18;

    private TreeMap<Long, Tuple2<String, String>> lps = new TreeMap<>();

    public TokenInfo(String tokenName, String tokenAddress, long createdOnBlock) {
        this.tokenName = tokenName;
        this.tokenAddress = tokenAddress;
        this.createdOnBlock = createdOnBlock;
    }

    public TokenInfo addLp(String lpName, long lpBlockStart, String otherTokenName) {
        lps.put(lpBlockStart, new Tuple2<>(lpName, otherTokenName));
        return this;
    }

    public TokenInfo setDivider(double d) {
        divider = d;
        return this;
    }

    public Tuple2<String, String> findLp(Long block) {
        if (block == null) {
            block = 1L;
        }
        Entry<Long, Tuple2<String, String>> entry = lps.floorEntry(block);
        if (entry == null) {
            throw new IllegalStateException("Not found lp by block " + block + " " + this);
        }
        return entry.getValue();
    }
}
