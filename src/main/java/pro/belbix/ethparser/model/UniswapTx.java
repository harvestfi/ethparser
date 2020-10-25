package pro.belbix.ethparser.model;

import java.math.BigInteger;
import lombok.Data;
import lombok.ToString;
import org.web3j.abi.datatypes.Address;

@Data
@ToString
public class UniswapTx {
    public static final String SWAP = "swap";
    public static final String ADD_LIQ = "add_liq";
    public static final String REMOVE_LIQ = "remove_liq";

    private String hash;
    private String type;
    private String owner;
    private String timestamp;
    private String block;
    private BigInteger amountIn;
    private Address coinIn;
    private BigInteger amountOut;
    private Address coinOut;
    private BigInteger liquidity;

    public boolean isContainsAddress(String address) {
        return address.equals(coinIn.getValue()) || address.equals(coinOut.getValue());
    }
}
