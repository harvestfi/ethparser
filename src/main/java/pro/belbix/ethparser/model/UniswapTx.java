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
    private BigInteger amountIn;
    private Address coinIn;
    private BigInteger amountOut;
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
            if (type.equals(SWAP)) {
                printable.setType("SELL");
                printable.setOtherCoin(ContractMapper.findName(coinOut.getValue()));
                printable.setOtherAmount(
                    amountOut.divide(new BigInteger(ContractMapper.findDivider(coinOut.getValue()))).toString());
                printable
                    .setAmount(amountIn.divide(new BigInteger(ContractMapper.findDivider(contract))).toString());
            } else {
                printable.setCoin(ContractMapper.findName(coinIn.getValue()));
                printable.setAmount(
                    amountIn.divide(new BigInteger(ContractMapper.findDivider(coinIn.getValue()))).toString());
                printable.setOtherCoin(ContractMapper.findName(coinOut.getValue()));
                printable.setOtherAmount(
                    amountOut.divide(new BigInteger(ContractMapper.findDivider(coinOut.getValue()))).toString());
            }
        } else if (contract.equals(coinOut.getValue())) {
            if (type.equals(SWAP)) {
                printable.setType("BUY");
                printable.setOtherCoin(ContractMapper.findName(coinIn.getValue()));
                printable.setOtherAmount(
                    amountIn.divide(new BigInteger(ContractMapper.findDivider(coinIn.getValue()))).toString());
                printable
                    .setAmount(amountOut.divide(new BigInteger(ContractMapper.findDivider(contract))).toString());
            } else {
                printable.setCoin(ContractMapper.findName(coinOut.getValue()));
                printable.setAmount(
                    amountOut.divide(new BigInteger(ContractMapper.findDivider(coinOut.getValue()))).toString());
                printable.setOtherCoin(ContractMapper.findName(coinIn.getValue()));
                printable.setOtherAmount(
                    amountIn.divide(new BigInteger(ContractMapper.findDivider(coinIn.getValue()))).toString());
            }
        }
        return printable;
    }
}
