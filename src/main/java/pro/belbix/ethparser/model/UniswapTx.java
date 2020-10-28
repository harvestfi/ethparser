package pro.belbix.ethparser.model;

import java.math.BigInteger;
import org.web3j.abi.datatypes.Address;
import pro.belbix.ethparser.web3.ContractMapper;


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
    private BigInteger amountEth = new BigInteger("0");
    private Address coinOut;
    private BigInteger liquidity;
    private boolean success = false;
    private boolean enriched;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public BigInteger getAmountIn() {
        return amountIn;
    }

    public void setAmountIn(BigInteger amountIn) {
        this.amountIn = amountIn;
    }

    public Address getCoinIn() {
        return coinIn;
    }

    public void setCoinIn(Address coinIn) {
        this.coinIn = coinIn;
    }

    public BigInteger getAmountOut() {
        return amountOut;
    }

    public void setAmountOut(BigInteger amountOut) {
        this.amountOut = amountOut;
    }

    public BigInteger getAmountEth() {
        return amountEth;
    }

    public void setAmountEth(BigInteger amountEth) {
        this.amountEth = amountEth;
    }

    public Address getCoinOut() {
        return coinOut;
    }

    public void setCoinOut(Address coinOut) {
        this.coinOut = coinOut;
    }

    public BigInteger getLiquidity() {
        return liquidity;
    }

    public void setLiquidity(BigInteger liquidity) {
        this.liquidity = liquidity;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isEnriched() {
        return enriched;
    }

    public void setEnriched(boolean enriched) {
        this.enriched = enriched;
    }

    @Override
    public String toString() {
        return "UniswapTx{" +
            "hash='" + hash + '\'' +
            ", type='" + type + '\'' +
            ", owner='" + owner + '\'' +
            ", timestamp='" + timestamp + '\'' +
            ", block='" + block + '\'' +
            ", amountIn=" + amountIn +
            ", coinIn=" + coinIn +
            ", amountOut=" + amountOut +
            ", coinOut=" + coinOut +
            ", liquidity=" + liquidity +
            ", success=" + success +
            ", enriched=" + enriched +
            '}';
    }

    public boolean isContainsAddress(String address) {
        return address.equals(coinIn.getValue().toLowerCase()) || address.equals(coinOut.getValue().toLowerCase());
    }

    public TransactionDTO toPrintable(String contract) {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setHash(hash);
        transactionDTO.setCoin(ContractMapper.findName(contract));
        transactionDTO.setConfirmed(success);
        transactionDTO.setEthAmount(amountToDouble(amountEth, coinIn));


        if (contract.equals(coinIn.getValue().toLowerCase())) {
            transactionDTO.setAmount(amountToDouble(amountIn, coinIn));
            transactionDTO.setOtherCoin(addrToStr(coinOut));
            transactionDTO.setOtherAmount(amountToDouble(amountOut, coinOut));
            if (type.equals(SWAP)) {
                transactionDTO.setType("SELL");
            } else {
                transactionDTO.setType(type);
            }
        } else if (contract.equals(coinOut.getValue().toLowerCase())) {
            transactionDTO.setAmount(amountToDouble(amountOut, coinOut));
            transactionDTO.setOtherCoin(addrToStr(coinIn));
            transactionDTO.setOtherAmount(amountToDouble(amountIn, coinIn));
            if (type.equals(SWAP)) {
                transactionDTO.setType("BUY");
            } else {
                transactionDTO.setType(type);
            }
        }
        return transactionDTO;
    }

    private static String addrToStr(Address adr) {
        return ContractMapper.findName(adr.getValue());
    }

    private static double amountToDouble(BigInteger amount, Address coin) {
        //really, it is totally unclear for me how it's work
        String divider = "1000000000000000000";
        if(amount.toString().length() < 16) {
            divider = "1000000";
        }
        return amount.doubleValue() / new BigInteger(divider).doubleValue();
    }
}
