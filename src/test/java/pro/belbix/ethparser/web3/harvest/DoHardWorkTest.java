package pro.belbix.ethparser.web3.harvest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static pro.belbix.ethparser.web3.harvest.parser.HardWorkParser.CONTROLLER;

import java.util.Collections;
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
import pro.belbix.ethparser.dto.HardWorkDTO;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.harvest.parser.HardWorkParser;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class DoHardWorkTest {

    @Autowired
    private HardWorkParser hardWorkParser;
    @Autowired
    private Web3Service web3Service;
    @Autowired
    private PriceProvider priceProvider;

    @Before
    public void setUp() throws Exception {
        priceProvider.setUpdateTimeout(0);
    }

    @Test
    public void parseSUSHI_MIC_USDT() {
        assertOnBlock(
            11615905,
            "0,000000",
            "0,000000",
            "0,000000"
        );
    }

    @Test
    public void parseUSDC_V0() {
        assertOnBlock(
            10772976,
            "-0,000405",
            "-170,167614",
            "-0,000405"
        );
    }

    @Test
    public void parseSUSHI_ETH_USDC() {
        assertOnBlock(
            11299287,
            "0,000073",
            "3060,000000",
            "0,000073"
        );
    }

    private void assertOnBlock(int onBlock,
                               String sharePrice,
                               String sharePriceUsd,
                               String shareChange) {
        List<LogResult> logResults = web3Service
            .fetchContractLogs(Collections.singletonList(CONTROLLER), onBlock, onBlock);
        assertNotNull(logResults);
        assertFalse(logResults.isEmpty());
        HardWorkDTO dto = hardWorkParser.parseLog((Log) logResults.get(0));
        assertNotNull(dto);
        assertAll(
            () -> assertEquals("ShareChange", shareChange, String.format("%f", dto.getShareChange())),
            () -> assertEquals("sharePrice", sharePrice, String.format("%f", dto.getShareChange())),
            () -> assertEquals("sharePriceUsd", sharePriceUsd, String.format("%f", dto.getShareChangeUsd()))
        );


    }

}
