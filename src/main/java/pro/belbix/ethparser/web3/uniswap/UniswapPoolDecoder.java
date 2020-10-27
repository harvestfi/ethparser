package pro.belbix.ethparser.web3.uniswap;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.model.UniswapTx;

public class UniswapPoolDecoder {

    private final static BigInteger ZERO = new BigInteger("0");
    private final static String SWAP_TOPIC = "0xd78ad95fa46c994b6551d0da85fc275fe613ce37657fb8d5e3d130840159d822";

    private Map<String, List<TypeReference<Type>>> parametersByMethodName;

    public UniswapPoolDecoder() {
        initParameters();
    }

    public void enrichUniTx(UniswapTx tx, List<Log> logs) {
        String data = findLastSwapLogData(logs);
        List<Type> types = decodeSwap(data);
        enrichPrice(tx, types);
        tx.setEnriched(true);
    }

    public List<Type> decodeSwap(String data) {
        return FunctionReturnDecoder.decode(data, parametersByMethodName.get("Swap"));
    }

    public String findLastSwapLogData(List<Log> logs) {
        for (int i = logs.size() - 1; i != 0; i--) {
            Log log = logs.get(i);
            if (!log.getTopics().isEmpty()
                && log.getTopics().get(0).toLowerCase().equals(SWAP_TOPIC)) {
                return log.getData();
            }
        }
        return null;
    }

    public void enrichPrice(UniswapTx tx, List<Type> types) {
        if (types.size() != 4) {
            throw new IllegalStateException("not valid type size " + types.size());
        }
        BigInteger amountIn0 = (BigInteger) types.get(0).getValue();
        BigInteger amountIn1 = (BigInteger) types.get(1).getValue();
        BigInteger amountOut0 = (BigInteger) types.get(2).getValue();
        BigInteger amountOut1 = (BigInteger) types.get(3).getValue();
        BigInteger amountIn;
        BigInteger amountOut;
        if (!ZERO.equals(amountIn0)) {
            amountIn = amountIn0;
        } else if (!ZERO.equals(amountIn1)) {
            amountIn = amountIn1;
        } else {
            throw new IllegalStateException("amountIn in is zero");
        }
        if (!ZERO.equals(amountOut0)) {
            amountOut = amountOut0;
        } else if (!ZERO.equals(amountOut1)) {
            amountOut = amountOut1;
        } else {
            throw new IllegalStateException("amountOut in is zero");
        }

        tx.setAmountIn(amountIn);
        tx.setAmountOut(amountOut);
    }

    void initParameters() {
        if (parametersByMethodName == null) {
            parametersByMethodName = new HashMap<>();
            try {
                parametersByMethodName.put("Swap",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint"),
                        TypeReference.makeTypeReference("uint"),
                        TypeReference.makeTypeReference("uint"),
                        TypeReference.makeTypeReference("uint")
                    ));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
