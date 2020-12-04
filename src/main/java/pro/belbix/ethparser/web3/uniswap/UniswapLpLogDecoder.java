package pro.belbix.ethparser.web3.uniswap;

import static pro.belbix.ethparser.web3.uniswap.LpContracts.UNI_LP_USDC_FARM;
import static pro.belbix.ethparser.web3.uniswap.LpContracts.UNI_LP_WBTC_BADGER;
import static pro.belbix.ethparser.web3.uniswap.LpContracts.UNI_LP_WETH_FARM;
import static pro.belbix.ethparser.web3.uniswap.LpContracts.keyCoinForLp;
import static pro.belbix.ethparser.web3.uniswap.Tokens.firstCoinIsKey;
import static pro.belbix.ethparser.web3.uniswap.Tokens.mapLpAddressToCoin;
import static pro.belbix.ethparser.web3.uniswap.Tokens.mapLpAddressToOtherCoin;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.model.EthTransactionI;
import pro.belbix.ethparser.model.UniswapTx;
import pro.belbix.ethparser.web3.MethodDecoder;

@SuppressWarnings({"unchecked", "rawtypes"})
public class UniswapLpLogDecoder extends MethodDecoder {

    private static final Set<String> allowedMethods = new HashSet<>(Arrays.asList("Mint", "Burn", "Swap"));

    public void decode(UniswapTx tx, Log log) {
        if (!isValidLog(log)) {
            return;
        }
        String topic0 = log.getTopics().get(0);
        String methodId = methodIdByFullHex.get(topic0);

        if (methodId == null) {
            throw new IllegalStateException("Unknown topic " + topic0);
        }
        String methodName = methodNamesByMethodId.get(methodId);

        if (!allowedMethods.contains(methodName)) {
            return;
        }

        List<TypeReference<Type>> parameters = parametersByMethodId.get(methodId);
        if (parameters == null) {
            throw new IllegalStateException("Not found parameters for topic " + topic0 + " with " + methodId);
        }

        List<Type> types = extractLogIndexedValues(log, parameters);
        tx.setHash(log.getTransactionHash());
        tx.setLogId(log.getLogIndex().longValue());
        tx.setBlock(log.getBlockNumber());
        tx.setSuccess(true);
        tx.setCoinAddress(keyCoinForLp.get(log.getAddress()));
        tx.setLpAddress(log.getAddress());
        enrich(types, methodName, tx, log);
    }

    private boolean isValidLog(Log log) {
        if (log == null || log.getTopics() == null || log.getTopics().isEmpty()) {
            return false;
        }
        return UNI_LP_USDC_FARM.equals(log.getAddress())
            || UNI_LP_WETH_FARM.equals(log.getAddress())
            || UNI_LP_WBTC_BADGER.equals(log.getAddress())
            ;
    }

    private void enrich(List<Type> types, String methodName, UniswapTx tx, Log log) {
        switch (methodName) {
            case "Swap":
                tx.setType(UniswapTx.SWAP);
                String sender = (String) types.get(0).getValue();
                BigInteger amount0In = (BigInteger) types.get(2).getValue();
                BigInteger amount1In = (BigInteger) types.get(3).getValue();
                BigInteger amount0Out = (BigInteger) types.get(4).getValue();
                BigInteger amount1Out = (BigInteger) types.get(5).getValue();

                if (!amount0In.equals(BigInteger.ZERO)) {
                    tx.setAmountIn(amount0In);
                } else if (!amount1In.equals(BigInteger.ZERO)) {
                    tx.setAmountIn(amount1In);
                } else {
                    throw new IllegalStateException("Zero amountIn for " + tx.getHash());
                }
                if (!amount0Out.equals(BigInteger.ZERO)) {
                    tx.setAmountOut(amount0Out);
                } else if (!amount1Out.equals(BigInteger.ZERO)) {
                    tx.setAmountOut(amount1Out);
                } else {
                    throw new IllegalStateException("Zero amountOut for " + tx.getHash());
                }

                if (
                    (amount1In.equals(BigInteger.ZERO) && firstCoinIsKey(log.getAddress()))
                    || (amount0In.equals(BigInteger.ZERO) && !firstCoinIsKey(log.getAddress()))
                    ) {
                    tx.setBuy(false);
                    tx.setCoinIn(new Address(mapLpAddressToCoin(log.getAddress())));
                    tx.setCoinOut(new Address(mapLpAddressToOtherCoin(log.getAddress())));
                } else if (
                    (amount0In.equals(BigInteger.ZERO) && firstCoinIsKey(log.getAddress()))
                    || (amount1In.equals(BigInteger.ZERO) && !firstCoinIsKey(log.getAddress()))
                ) {
                    tx.setBuy(true);
                    tx.setCoinIn(new Address(mapLpAddressToOtherCoin(log.getAddress())));
                    tx.setCoinOut(new Address(mapLpAddressToCoin(log.getAddress())));
                } else {
                    throw new IllegalStateException("Wrong amount in " + log);
                }

                return;
            case "Mint":
                tx.setType(UniswapTx.ADD_LIQ);
                tx.setBuy(true);
                tx.setCoinIn(new Address(mapLpAddressToOtherCoin(log.getAddress())));
                tx.setCoinOut(new Address(mapLpAddressToCoin(log.getAddress())));
                if(firstCoinIsKey(log.getAddress())) {
                    tx.setAmountOut((BigInteger) types.get(1).getValue());
                    tx.setAmountIn((BigInteger) types.get(2).getValue());
                } else {
                    tx.setAmountIn((BigInteger) types.get(1).getValue());
                    tx.setAmountOut((BigInteger) types.get(2).getValue());
                }

                return;
            case "Burn":
                tx.setType(UniswapTx.REMOVE_LIQ);
                tx.setBuy(false);
                tx.setCoinIn(new Address(mapLpAddressToCoin(log.getAddress())));
                tx.setCoinOut(new Address(mapLpAddressToOtherCoin(log.getAddress())));
                if(firstCoinIsKey(log.getAddress())) {
                    tx.setAmountIn((BigInteger) types.get(2).getValue());
                    tx.setAmountOut((BigInteger) types.get(3).getValue());
                } else {
                    tx.setAmountOut((BigInteger) types.get(2).getValue());
                    tx.setAmountIn((BigInteger) types.get(3).getValue());
                }

                return;
        }
        throw new IllegalStateException("Unknown method " + methodName);
    }

    @Override
    public EthTransactionI mapTypesToModel(List<Type> types, String methodID, Transaction transaction) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void initParameters() {
        if (parametersByMethodId.isEmpty()) {
            Map<String, List<TypeReference<Type>>> parameters = new HashMap<>();
            try {
                parameters.put("Swap",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address", true, false)
                    ));
                parameters.put("Mint",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("Burn",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address", true, false)
                    ));
                parameters.put("Sync",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint112"),
                        TypeReference.makeTypeReference("uint112")
                    ));
                parameters.put("Approval",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("Transfer",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("uint256")
                    ));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            writeParameters(parameters);
        }
    }
}
