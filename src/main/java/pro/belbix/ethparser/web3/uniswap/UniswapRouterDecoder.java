package pro.belbix.ethparser.web3.uniswap;

import static pro.belbix.ethparser.web3.uniswap.LpContracts.UNI_LP_USDC_FARM;
import static pro.belbix.ethparser.web3.uniswap.Tokens.FARM_TOKEN;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.model.EthTransactionI;
import pro.belbix.ethparser.model.UniswapTx;
import pro.belbix.ethparser.web3.MethodDecoder;

@SuppressWarnings({"rawtypes", "unchecked"})
public class UniswapRouterDecoder extends MethodDecoder {

    private final static Address WETH_ADDRESS = new Address("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2");

    @Override
    public EthTransactionI mapTypesToModel(List<Type> types, String methodId, Transaction transaction) {
        String methodName = methodNamesByMethodId.get(methodId);
        UniswapTx tx = new UniswapTx();
        tx.setHash(transaction.getHash());
        tx.setOwner(transaction.getFrom());
        tx.setBlock(transaction.getBlockNumber());
        tx.setCoinAddress(FARM_TOKEN);
        tx.setLpAddress(UNI_LP_USDC_FARM);
        parseMethod(tx, types, methodName);

        //ensure that FARM coin setCorrect
        if (tx.isContainsAddress(FARM_TOKEN)) {
            if (tx.tokenIsFirstOrLast(FARM_TOKEN)) { //BUY
                tx.setCoinIn(new Address(FARM_TOKEN));
                tx.setCoinOut(tx.getAllAddresses()[1]);
                tx.setBuy(false);
            } else { //SELL
                tx.setCoinOut(new Address(FARM_TOKEN));
                tx.setCoinIn(tx.getAllAddresses()[tx.getAllAddresses().length - 2]);
                tx.setBuy(true);
            }
        }
        return tx;
    }

    private void parseMethod(UniswapTx tx, List<Type> types, String methodName) {
        Address[] addresses = new Address[2];
        switch (methodName) {
            case "addLiquidityETH":
                tx.setType(UniswapTx.ADD_LIQ);
                addresses[0] = WETH_ADDRESS;
                addresses[1] = new Address((String) types.get(0).getValue());
                tx.setAllAddresses(addresses);
                tx.setAmountOut((BigInteger) types.get(1).getValue());
                tx.setAmountIn((BigInteger) types.get(3).getValue());
                return;
            case "addLiquidity":
                tx.setType(UniswapTx.ADD_LIQ);
                Object coinIn = types.get(0).getValue();
                if (coinIn instanceof String) {
                    addresses[0] = new Address((String) coinIn);
                } else {
                    addresses[0] = (Address) coinIn;
                }
                Object coinOut = types.get(1).getValue();
                if (coinOut instanceof String) {
                    addresses[1] = new Address((String) coinOut);
                } else {
                    addresses[1] = (Address) coinOut;
                }
                tx.setAllAddresses(addresses);
                tx.setAmountIn((BigInteger) types.get(2).getValue());
                tx.setAmountOut((BigInteger) types.get(3).getValue());
                return;
            case "removeLiquidityETH":
            case "removeLiquidityETHWithPermit":
            case "removeLiquidityETHWithPermitSupportingFeeOnTransferTokens":
            case "removeLiquidityETHSupportingFeeOnTransferTokens":
                tx.setType(UniswapTx.REMOVE_LIQ);
                addresses[0] = WETH_ADDRESS;
                addresses[1] = new Address((String) types.get(0).getValue());
                tx.setAllAddresses(addresses);
                tx.setLiquidity((BigInteger) types.get(1).getValue());
                tx.setAmountOut((BigInteger) types.get(2).getValue());
                tx.setAmountIn((BigInteger) types.get(3).getValue());
                return;
            case "removeLiquidity":
            case "removeLiquidityWithPermit":
                tx.setType(UniswapTx.REMOVE_LIQ);
                addresses[1] = new Address((String) types.get(0).getValue());
                addresses[0] = new Address((String) types.get(1).getValue());
                tx.setAllAddresses(addresses);
                tx.setLiquidity((BigInteger) types.get(2).getValue());
                tx.setAmountOut((BigInteger) types.get(3).getValue());
                tx.setAmountIn((BigInteger) types.get(4).getValue());
                return;
            case "swapExactTokensForETH": //0x75e17566b36eb7fc9bc1be4c95d2e36cd30b09faa803fd1e06732c504ecee1a9
            case "swapExactTokensForETHSupportingFeeOnTransferTokens":
                tx.setType(UniswapTx.SWAP);
                tx.setAmountIn((BigInteger) types.get(0).getValue());
                tx.setAmountEth((BigInteger) types.get(1).getValue());
//                tx.setCoinIn(parseAddress(types.get(2), 0));
//                tx.setCoinOut(parseAddress(types.get(2), 1));
                tx.setAllAddresses(parseAddresses(types.get(2)));
                return;
            case "swapExactTokensForTokens":
            case "swapExactTokensForTokensSupportingFeeOnTransferTokens":
                tx.setType(UniswapTx.SWAP);
                tx.setAmountIn((BigInteger) types.get(0).getValue());
                tx.setAmountOut((BigInteger) types.get(1).getValue());
//                tx.setCoinIn(parseAddress(types.get(2), -2)); //should parse last pair for detect farm
//                tx.setCoinOut(parseAddress(types.get(2), -1));
                tx.setAllAddresses(parseAddresses(types.get(2)));
                return;
            case "swapExactETHForTokens": //0xb28bfbcc048fca2193b4c56518f42a7a1c1951720b07e86fe171c9db19cda71b
            case "swapETHForExactTokens":
            case "swapExactETHForTokensSupportingFeeOnTransferTokens":
                tx.setType(UniswapTx.SWAP);
                tx.setAmountEth((BigInteger) types.get(0).getValue());
//                tx.setCoinIn(parseAddress(types.get(1), -2));
//                tx.setCoinOut(parseAddress(types.get(1), -1));
                tx.setAllAddresses(parseAddresses(types.get(1)));
                return;
            case "swapTokensForExactTokens":
            case "swapTokensForExactETH":
                tx.setType(UniswapTx.SWAP);
                tx.setAmountOut((BigInteger) types.get(0).getValue());
                tx.setAmountIn((BigInteger) types.get(1).getValue());
//                tx.setCoinIn(parseAddress(types.get(2), 0));
//                tx.setCoinOut(parseAddress(types.get(2), 1));
                tx.setAllAddresses(parseAddresses(types.get(2)));
                return;
        }
        throw new IllegalStateException("Unknown method " + methodName + " for " + tx.getHash());
    }

    private static Address parseAddress(Type type, int i) {
        if (i < 0) {
            List adrs = (List) type.getValue();
            return (Address) adrs.get(adrs.size() + i);
        }
        return (Address) ((List) type.getValue()).get(i);
    }
}
