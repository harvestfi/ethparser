package pro.belbix.ethparser.web3;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;
import static pro.belbix.ethparser.web3.Functions.GET_PRICE_PER_FULL_SHARE;
import static pro.belbix.ethparser.web3.Functions.GET_RESERVES;
import static pro.belbix.ethparser.web3.Web3Service.BLOCK_NUMBER_30_AUGUST_2020;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.WBTC;
import static pro.belbix.ethparser.web3.uniswap.LpContracts.UNI_LP_ETH_DAI;
import static pro.belbix.ethparser.web3.uniswap.UniswapTransactionsParser.FARM_WETH_UNI_CONTRACT;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint112;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.model.HarvestTx;
import pro.belbix.ethparser.web3.harvest.contracts.Vaults;
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
        for (Log log : logs) {
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

    @Test
    public void testFetchBlock() {
        Block block = web3Service.findBlock("0x185e7b9fa5700b045cb319472b2e7e73540aa56392389d7789d1d6b6e72dd832");
        assertNotNull(block);
        Instant date = Instant.ofEpochSecond(block.getTimestamp().longValue());
        assertEquals(Instant.ofEpochSecond(1603810501L), date);
    }

    @Test
    @Ignore
    public void checkLogsForAllVaults() {
        for (String hash : Vaults.vaultNames.keySet()) {
            List<LogResult> logs = web3Service
                .fetchContractLogs(singletonList(hash), null, null);
            assertNotNull(logs);
            System.out.println(hash + " " + logs.size());
        }
    }

    @Test
    @Ignore
    public void getBalanceTest() {
        double balance = web3Service.fetchBalance(WBTC);
        assertTrue(balance > 0);
    }

    @Test
    public void ethCallGET_PRICE_PER_FULL_SHARETest() {
        List<Type> types = web3Service.callMethod(GET_PRICE_PER_FULL_SHARE, WBTC, LATEST);
        assertNotNull(types);
        assertFalse(types.isEmpty());
        assertTrue(HarvestTx.parseAmount((BigInteger) types.get(0).getValue(), WBTC) > 0);
    }

    @Test
    public void ethCallGET_RESERVESTest() {
        List<Type> types = web3Service.callMethod(GET_RESERVES, UNI_LP_ETH_DAI, BLOCK_NUMBER_30_AUGUST_2020);
        assertNotNull(types);
        assertEquals(3, types.size());
        assertTrue(((Uint112) types.get(0)).getValue()
            .divide(new BigInteger("1000000000000000000")).longValue() > 0);
        assertTrue(((Uint112) types.get(1)).getValue()
            .divide(new BigInteger("1000000000000000000")).longValue() > 0);
        assertTrue(((Uint32) types.get(2)).getValue().longValue() > 0);
    }
}
