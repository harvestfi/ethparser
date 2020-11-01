package pro.belbix.ethparser.web3.uniswap;

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
        switch (methodName) {
            case "addLiquidityETH":
                tx.setType(UniswapTx.ADD_LIQ);
                tx.setCoinIn(WETH_ADDRESS);
                tx.setCoinOut(new Address((String) types.get(0).getValue()));
                tx.setAmountOut((BigInteger) types.get(1).getValue());
                tx.setAmountIn((BigInteger) types.get(3).getValue());
                return tx;
            case "addLiquidity":
                tx.setType(UniswapTx.ADD_LIQ);
                Object coinIn = types.get(0).getValue();
                if (coinIn instanceof String) {
                    tx.setCoinIn(new Address((String) coinIn));
                } else {
                    tx.setCoinIn((Address) coinIn);
                }
                Object coinOut = types.get(1).getValue();
                if (coinOut instanceof String) {
                    tx.setCoinOut(new Address((String) coinOut));
                } else {
                    tx.setCoinOut((Address) coinOut);
                }
                tx.setAmountIn((BigInteger) types.get(2).getValue());
                tx.setAmountOut((BigInteger) types.get(3).getValue());
                return tx;
            case "removeLiquidityETH":
            case "removeLiquidityETHWithPermit":
            case "removeLiquidityETHWithPermitSupportingFeeOnTransferTokens":
            case "removeLiquidityETHSupportingFeeOnTransferTokens":
                tx.setType(UniswapTx.REMOVE_LIQ);
                tx.setCoinIn(WETH_ADDRESS);
                tx.setCoinOut(new Address((String) types.get(0).getValue()));
                tx.setLiquidity((BigInteger) types.get(1).getValue());
                tx.setAmountOut((BigInteger) types.get(2).getValue());
                tx.setAmountIn((BigInteger) types.get(3).getValue());
                return tx;
            case "removeLiquidity":
            case "removeLiquidityWithPermit":
                tx.setType(UniswapTx.REMOVE_LIQ);
                tx.setCoinOut(new Address((String) types.get(0).getValue()));
                tx.setCoinIn(new Address((String) types.get(1).getValue()));
                tx.setLiquidity((BigInteger) types.get(2).getValue());
                tx.setAmountOut((BigInteger) types.get(3).getValue());
                tx.setAmountIn((BigInteger) types.get(4).getValue());
                return tx;
            case "swapExactTokensForETH": //0x75e17566b36eb7fc9bc1be4c95d2e36cd30b09faa803fd1e06732c504ecee1a9
            case "swapExactTokensForETHSupportingFeeOnTransferTokens":
                tx.setType(UniswapTx.SWAP);
                tx.setAmountIn((BigInteger) types.get(0).getValue());
                tx.setAmountEth((BigInteger) types.get(1).getValue());
                tx.setCoinIn(parseAddress(types.get(2), 0));
                tx.setCoinOut(parseAddress(types.get(2), 1));
                return tx;
            case "swapExactTokensForTokens":
            case "swapExactTokensForTokensSupportingFeeOnTransferTokens":
                tx.setType(UniswapTx.SWAP);
                tx.setAmountIn((BigInteger) types.get(0).getValue());
                tx.setAmountOut((BigInteger) types.get(1).getValue());
                tx.setCoinIn(parseAddress(types.get(2), 0));
                tx.setCoinOut(parseAddress(types.get(2), 1));
                return tx;
            case "swapExactETHForTokens": //0xb28bfbcc048fca2193b4c56518f42a7a1c1951720b07e86fe171c9db19cda71b
            case "swapETHForExactTokens":
            case "swapExactETHForTokensSupportingFeeOnTransferTokens":
                tx.setType(UniswapTx.SWAP);
                tx.setAmountEth((BigInteger) types.get(0).getValue());
                tx.setCoinIn(parseAddress(types.get(1), -2));
                tx.setCoinOut(parseAddress(types.get(1), -1));
                return tx;
            case "swapTokensForExactTokens":
            case "swapTokensForExactETH":
                tx.setType(UniswapTx.SWAP);
                tx.setAmountOut((BigInteger) types.get(0).getValue());
                tx.setAmountIn((BigInteger) types.get(1).getValue());
                tx.setCoinIn(parseAddress(types.get(2), 0));
                tx.setCoinOut(parseAddress(types.get(2), 1));
                return tx;
        }
        throw new IllegalStateException("Unknown method");
    }

    private static Address parseAddress(Type type, int i) {
        if (i < 0) {
            List adrs = (List) type.getValue();
            return (Address) adrs.get(adrs.size() + i);
        }
        return (Address) ((List) type.getValue()).get(i);
    }

    @Override
    protected void initParameters() {
        if (parametersByMethodId.isEmpty()) {
            Map<String, List<TypeReference<Type>>> parameters = new HashMap<>();
            try {
                parameters.put("addLiquidity",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")

                    ));
                parameters.put("addLiquidityETH",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")

                    ));
                parameters.put("removeLiquidity",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")

                    ));
                parameters.put("removeLiquidityETH",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")

                    ));
                parameters.put("removeLiquidityWithPermit",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("bool"),
                        TypeReference.makeTypeReference("uint8"),
                        TypeReference.makeTypeReference("bytes32"),
                        TypeReference.makeTypeReference("bytes32")
                    ));
                parameters.put("removeLiquidityETHWithPermit",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("bool"),
                        TypeReference.makeTypeReference("uint8"),
                        TypeReference.makeTypeReference("bytes32"),
                        TypeReference.makeTypeReference("bytes32")
                    ));
                parameters.put("removeLiquidityETHSupportingFeeOnTransferTokens",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("removeLiquidityETHWithPermitSupportingFeeOnTransferTokens",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("bool"),
                        TypeReference.makeTypeReference("uint8"),
                        TypeReference.makeTypeReference("bytes32"),
                        TypeReference.makeTypeReference("bytes32")
                    ));
                parameters.put("swapExactTokensForTokens",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address[]"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("swapTokensForExactTokens",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address[]"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("swapExactETHForTokens",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address[]"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("swapTokensForExactETH",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address[]"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("swapExactTokensForETH",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address[]"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("swapETHForExactTokens",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address[]"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("swapExactTokensForTokensSupportingFeeOnTransferTokens",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address[]"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("swapExactETHForTokensSupportingFeeOnTransferTokens",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address[]"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("swapExactTokensForETHSupportingFeeOnTransferTokens",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address[]"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            writeParameters(parameters);
        }
    }
}
