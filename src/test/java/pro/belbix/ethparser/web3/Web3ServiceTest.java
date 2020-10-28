package pro.belbix.ethparser.web3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static pro.belbix.ethparser.web3.uniswap.UniswapTransactionsParser.FARM_WETH_UNI_CONTRACT;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.web3.uniswap.UniswapPoolDecoder;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class Web3ServiceTest {

    private UniswapPoolDecoder decoder = new UniswapPoolDecoder();
    @Autowired
    private Web3Service web3Service;

    @Test
    public void fetchDataForTxSwapWETHtoFARM() throws ClassNotFoundException {
        TransactionReceipt transactionReceipt = web3Service
            .fetchTransactionReceipt("0x266519b5e5756ea500d505afdfaa7d8cbb1fa0acc895fb9b9e6dbfefd3e7ce48");
        assertNotNull(transactionReceipt);
        List<Log> logs = transactionReceipt.getLogs();
        for(Log log: logs) {
            System.out.println(log.toString());
        }
        Log lastLog = logs.get(logs.size() - 1);
        assertEquals(FARM_WETH_UNI_CONTRACT, lastLog.getAddress().toLowerCase());
        String data = lastLog.getData();

        List<Type> types = FunctionReturnDecoder.decode(data,
            Arrays.asList(
                TypeReference.makeTypeReference("uint"),
                TypeReference.makeTypeReference("uint"),
                TypeReference.makeTypeReference("uint"),
                TypeReference.makeTypeReference("uint")
            )
        );

        assertNotNull(types);
        assertEquals(new BigInteger("0"), types.get(0).getValue());
        assertEquals(new BigInteger("3369976790396557"), types.get(1).getValue());
        assertEquals(new BigInteger("11966348304870486"), types.get(2).getValue());
        assertEquals(new BigInteger("0"), types.get(3).getValue());


    }
}
