package pro.belbix.ethparser.model;

import java.math.BigInteger;
import lombok.Data;
import lombok.ToString;
import org.web3j.abi.datatypes.Address;
import pro.belbix.ethparser.web3.ContractMapper;

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
    private BigInteger amountIn = new BigInteger("0");
    private Address coinIn;
    private BigInteger amountOut = new BigInteger("0");
    private Address coinOut;
    private BigInteger liquidity;
    private String status;

    public boolean isContainsAddress(String address) {
        return address.equals(coinIn.getValue()) || address.equals(coinOut.getValue());
    }

    public Printable print(String contract) {
        Printable printable = new Printable();
        printable.setHash(hash);
        printable.setCoin(ContractMapper.findName(contract));

        if (contract.equals(coinIn.getValue())) {
            printable.setAmount(amountToStr(amountIn, coinIn));
            printable.setOtherCoin(addrToStr(coinOut));
            printable.setOtherAmount(amountToStr(amountOut, coinOut));
            if (type.equals(SWAP)) {
                printable.setType("SELL");
            } else {
                printable.setType(type);
            }
        } else if (contract.equals(coinOut.getValue())) {
            printable.setAmount(amountToStr(amountOut, coinOut));
            printable.setOtherCoin(addrToStr(coinIn));
            printable.setOtherAmount(amountToStr(amountIn, coinIn));
            if (type.equals(SWAP)) {
                printable.setType("BUY");
            } else {
                printable.setType(type);
            }
        }
        return printable;
    }

    private static String addrToStr(Address adr) {
        return ContractMapper.findName(adr.getValue());
    }

    private static double amountToStr(BigInteger amount, Address coin) {
        return amount.doubleValue() / new BigInteger(ContractMapper.findDivider(coin.getValue())).doubleValue();
//        return String.format("%.2f", value);
    }
}
