package pro.belbix.ethparser.web3.uniswap;

import static pro.belbix.ethparser.web3.uniswap.UniswapTransactionsParser.FARM_TOKEN_CONTRACT;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.model.UniswapTx;

@SuppressWarnings({"rawtypes", "unchecked"})
public class UniswapPoolDecoder {

    private final static BigInteger ZERO = new BigInteger("0");
    private final static String USDC_ADDRESS = "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48";
    private final static String WETH_ADDRESS = "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2";
    private final static String SWAP_TOPIC = "0xd78ad95fa46c994b6551d0da85fc275fe613ce37657fb8d5e3d130840159d822";
    private final static String WETH_TOPIC_ADDRESS = "0x000000000000000000000000fed6b9243748e5a5bb5c1f373fd7da9fca235334";
    private final static String USDC_TOPIC_ADDRESS = "0x000000000000000000000000558806e332b2cdab47d61cf96ef7753d8c97e33b";
    private final static String USDC_TOPIC_ADDRESS_2 = "0x000000000000000000000000bd33caaedf06e436932522a9284d8312f325cae8";
    private final static String FARM_TOPIC_ADDRESS = "0x0000000000000000000000007a250d5630b4cf539739df2c5dacb4c659f2488d";

    private Map<String, List<TypeReference<Type>>> parametersByMethodName;

    public UniswapPoolDecoder() {
        initParameters();
    }

    public void enrichUniTx(UniswapTx tx, List<Log> logs) {
        Log log;
        if (tx.getCoinIn().getValue().equals(FARM_TOKEN_CONTRACT)) {
            log = findFirstSwapLog(logs);
        } else {
            log = findLastSwapLog(logs);
        }

        mapAddresses(tx, log);
        List<Type> types = decodeSwap(log.getData());
        enrichPrice(tx, types);
        tx.setEnriched(true);
    }

    private void mapAddresses(UniswapTx tx, Log log) {
        if (log.getTopics().size() == 3) {
            String firstTopicAddress = log.getTopics().get(1).toLowerCase();
            String lastTopicAddress = log.getTopics().get(2).toLowerCase();
            if (USDC_TOPIC_ADDRESS.equals(lastTopicAddress)) {
                tx.setCoinOut(new Address(USDC_ADDRESS));
            }
            if (USDC_TOPIC_ADDRESS.equals(firstTopicAddress)) {
                tx.setCoinIn(new Address(USDC_ADDRESS));
            }
            if (WETH_TOPIC_ADDRESS.equals(lastTopicAddress)) {
                tx.setCoinOut(new Address(WETH_ADDRESS));
            }
            if (WETH_TOPIC_ADDRESS.equals(firstTopicAddress)) {
                tx.setCoinIn(new Address(WETH_ADDRESS));
            }
        }
    }

    public List<Type> decodeSwap(String data) {
        return FunctionReturnDecoder.decode(data, parametersByMethodName.get("Swap"));
    }

    public Log findFirstSwapLog(List<Log> logs) {
        for (Log log : logs) {
            if (!log.getTopics().isEmpty()
                && log.getTopics().get(0).toLowerCase().equals(SWAP_TOPIC)) {
                return log;
            }
        }
        return null;
    }

    public Log findLastSwapLog(List<Log> logs) {
        for (int i = logs.size() - 1; i != 0; i--) {
            Log log = logs.get(i);
            if (!log.getTopics().isEmpty()
                && log.getTopics().get(0).toLowerCase().equals(SWAP_TOPIC)) {
                return log;
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
