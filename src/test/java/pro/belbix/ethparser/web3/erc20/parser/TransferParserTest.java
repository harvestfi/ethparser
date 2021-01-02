package pro.belbix.ethparser.web3.erc20.parser;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static pro.belbix.ethparser.web3.erc20.Tokens.FARM_TOKEN;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.TransferDTO;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.Web3Service;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class TransferParserTest {

    @Autowired
    private Web3Service web3Service;
    @Autowired
    private PriceProvider priceProvider;
    @Autowired
    private TransferParser transferParser;

    @Before
    public void setUp() {
        priceProvider.setUpdateTimeout(0);
    }

    @Test
    public void testParseFARM() {
        parserTest(FARM_TOKEN,
            11571359,
            0,
            "FARM",
            "0xa910f92acdaf488fa6ef02174fb86208ad7722ba",
            "0x7a77784d32fef468c2a46cdf4ef2e15ef2cb2226",
            "4,25506623"
        );
    }

    private TransferDTO parserTest(
        String contractHash,
        int onBlock,
        int logId,
        String name,
        String owner,
        String recipient,
        String value
    ) {
        List<LogResult> logResults = web3Service.fetchContractLogs(singletonList(contractHash), onBlock, onBlock);
        assertTrue("Log smaller then necessary", logId < logResults.size());
        TransferDTO dto = transferParser.parseLog((Log) logResults.get(logId).get());
        assertDto(dto, name, owner, recipient, value);
        return dto;
    }

    private void assertDto(TransferDTO dto, String name, String owner, String recipient, String value) {
        assertNotNull("Dto is null", dto);
        assertAll(
            () -> assertEquals("name", name, dto.getName()),
            () -> assertEquals("owner", owner, dto.getOwner()),
            () -> assertEquals("recipient", recipient, dto.getRecipient()),
            () -> assertEquals("value", value, String.format("%.8f", dto.getValue()))
        );
    }

}
