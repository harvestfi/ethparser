package pro.belbix.ethparser.model;

import static pro.belbix.ethparser.web3.uniswap.LpContracts.amountToDouble;
import static pro.belbix.ethparser.web3.uniswap.Tokens.findNameForContract;

import java.math.BigInteger;
import org.web3j.abi.datatypes.Address;
import pro.belbix.ethparser.dto.UniswapDTO;

public class UniswapTx implements EthTransactionI {

    public static final String SWAP = "SWAP";
    public static final String ADD_LIQ = "ADD";
    public static final String REMOVE_LIQ = "REM";

    private String hash;
    private long logId;
    private String type;
    private String owner;
    private BigInteger block;
    private BigInteger amountIn = new BigInteger("0");
    private Address coinIn;
    private BigInteger amountOut = new BigInteger("0");
    private BigInteger amountEth = new BigInteger("0");
    private Address coinOut;
    private BigInteger liquidity;
    private boolean success = false;
    private boolean enriched;
    private Boolean buy;
    private Address[] allAddresses;
    private String coinAddress;
    private String lpAddress;

    public String getLpAddress() {
        return lpAddress;
    }

    public void setLpAddress(String lpAddress) {
        this.lpAddress = lpAddress;
    }

    public String getCoinAddress() {
        return coinAddress;
    }

    public void setCoinAddress(String coinAddress) {
        this.coinAddress = coinAddress;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
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

    public BigInteger getBlock() {
        return block;
    }

    public void setBlock(BigInteger block) {
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

    public Boolean getBuy() {
        return buy;
    }

    public void setBuy(Boolean buy) {
        this.buy = buy;
    }

    public Address[] getAllAddresses() {
        return allAddresses;
    }

    public void setAllAddresses(Address[] allAddresses) {
        this.allAddresses = allAddresses;
    }

    public boolean isBuy() {
        return buy;
    }

    public void setBuy(boolean buy) {
        this.buy = buy;
    }

    @Override
    public String toString() {
        return "UniswapTx{" +
            "hash='" + hash + '\'' +
            ", type='" + type + '\'' +
            ", owner='" + owner + '\'' +
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
        for (Address a : allAddresses) {
            if (address.equals(a.getValue())) {
                return true;
            }
        }
        return false;
    }

    public boolean tokenIsFirstOrLast(String hash) {
        if (allAddresses == null || allAddresses.length == 0) {
            throw new IllegalStateException("Empty addresses for " + hash);
        }
        if (allAddresses.length == 1) {
            throw new IllegalStateException("Only one address for " + hash);
        }
        if (allAddresses[0].getValue().equals(hash)) {
            return true;
        }
        if (allAddresses[allAddresses.length - 1].getValue().equals(hash)) {
            return false;
        }
        throw new IllegalStateException("Token not the last or first for " + hash);
    }

    public void assertBuy(boolean expected) {
        if (buy == null) {
            throw new IllegalStateException("Buy now is null for " + hash);
        }
        if (buy != expected) {
            throw new IllegalStateException("Unexpected setup for " + hash);
        }
    }

    public UniswapDTO toDto() {
        UniswapDTO uniswapDTO = new UniswapDTO();
        uniswapDTO.setId(hash + "_" + logId);
        uniswapDTO.setHash(hash);
        uniswapDTO.setOwner(owner);
        uniswapDTO.setBlock(block);
        uniswapDTO.setCoin(findNameForContract(coinAddress));
        uniswapDTO.setConfirmed(success);

        if (coinAddress.equals(coinIn.getValue().toLowerCase())) {
            assertBuy(false);
            uniswapDTO.setAmount(amountToDouble(amountIn, lpAddress, coinIn.getValue()));
            uniswapDTO.setOtherCoin(addrToStr(coinOut));
            uniswapDTO.setOtherAmount(amountToDouble(amountOut, lpAddress, coinOut.getValue()));
            if (type.equals(SWAP)) {
                uniswapDTO.setType("SELL");
            } else {
                uniswapDTO.setType(type);
            }
        } else if (coinAddress.equals(coinOut.getValue().toLowerCase())) {
            assertBuy(true);
            uniswapDTO.setAmount(amountToDouble(amountOut, lpAddress, coinOut.getValue()));
            uniswapDTO.setOtherCoin(addrToStr(coinIn));
            uniswapDTO.setOtherAmount(amountToDouble(amountIn, lpAddress, coinIn.getValue()));
            if (type.equals(SWAP)) {
                uniswapDTO.setType("BUY");
            } else {
                uniswapDTO.setType(type);
            }
        } else {
            throw new IllegalStateException("Contract not found for " + hash);
        }

        if (uniswapDTO.getOtherCoin().equals("USDC")) {
            double price = (uniswapDTO.getOtherAmount() / uniswapDTO.getAmount());
            uniswapDTO.setLastPrice(price);
        }
        return uniswapDTO;
    }

    private static String addrToStr(Address adr) {
        return findNameForContract(adr.getValue());
    }
}
