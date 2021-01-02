package pro.belbix.ethparser.model;

import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.amountToDouble;
import static pro.belbix.ethparser.web3.erc20.Tokens.findNameForContract;

import java.math.BigInteger;
import lombok.Data;
import org.web3j.abi.datatypes.Address;
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.web3.uniswap.contracts.LpContracts;

@Data
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
        uniswapDTO.setLp(LpContracts.findNameForLpHash(lpAddress));

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
            throw new IllegalStateException("Contract can't identified " + toString());
        }

        if (uniswapDTO.getOtherCoin().equals("USDC")) {
            double price = (uniswapDTO.getOtherAmount() / uniswapDTO.getAmount());
            uniswapDTO.setPrice(price);
        }
        return uniswapDTO;
    }

    private static String addrToStr(Address adr) {
        return findNameForContract(adr.getValue());
    }
}
