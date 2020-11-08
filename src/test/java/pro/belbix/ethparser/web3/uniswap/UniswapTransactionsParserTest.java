package pro.belbix.ethparser.web3.uniswap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.web3.Web3Service;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class UniswapTransactionsParserTest {

    @Autowired
    private Web3Service web3Service;
    @Autowired
    private UniswapTransactionsParser uniswapTransactionsParser;

    @Test
    public void shouldEnrichUniTxCorrect_swapExactTokensForTokensSupportingFeeOnTransferTokens() throws IOException {
        Transaction tx = web3Service
            .findTransaction("0x266519b5e5756ea500d505afdfaa7d8cbb1fa0acc895fb9b9e6dbfefd3e7ce48");
        UniswapDTO uniswapDTO = uniswapTransactionsParser.parseUniswapTransaction(tx);
        assertDto(uniswapDTO,
            true,
            "BUY",
            "FARM",
            "WETH",
            0.011966348304870486,
            0.003369976790396557
        );
    }

    @Test
    public void parse_swapExactTokensForETH2() throws IOException {
        Transaction tx = web3Service
            .findTransaction("0xa16181b55838897e58747c60c605e2b9c19866e82b5e8eab8d1b67d4b1c44039");
        UniswapDTO uniswapDTO = uniswapTransactionsParser.parseUniswapTransaction(tx);
        assertDto(uniswapDTO,
            true,
            "SELL",
            "FARM",
            "USDC",
            648.7881969315667,
            72961.935183
        );
    }

    @Test
    public void shouldEnrichUniTx_swapExactTokensForETH() throws IOException {
        Transaction tx = web3Service
            .findTransaction("0xd0c2a327772fcb4894688b4528909d98095ea77123719718d639dbd00cc11b41");
        UniswapDTO uniswapDTO = uniswapTransactionsParser.parseUniswapTransaction(tx);
        assertDto(uniswapDTO,
            true,
            "SELL",
            "FARM",
            "USDC",
            0.85,
            92.415767
        );
    }

    @Test
    public void parse_swapTokensForExactETH() throws IOException, InterruptedException {
        Transaction tx = web3Service
            .findTransaction("0x5026a67dd0577b0370f483a48d0869d6adca68e5a0339443fcadd3644a9a9e32");
        UniswapDTO uniswapDTO = uniswapTransactionsParser.parseUniswapTransaction(tx);
        assertDto(uniswapDTO,
            true,
            "SELL",
            "FARM",
            "USDC",
            55.440214001258916,
            5851.100961
        );
    }

    @Test
    public void parse_swapTokensForExactTokens_fail() throws IOException {
        Transaction tx = web3Service
            .findTransaction("0x5a9d8fa3fb5097ba4a75aad475497fab49b67efb31f3dc248e66fdab578b6208");
        UniswapDTO uniswapDTO = uniswapTransactionsParser.parseUniswapTransaction(tx);
        assertDto(uniswapDTO,
            false,
            "SELL",
            "FARM",
            "USDC",
            1.0121780834180945,
            1.0
        );
    }

    @Test
    public void parse_swapExactTokensForTokens_broken() throws IOException {
        Transaction tx = web3Service
            .findTransaction("0x51dc7e543bcd6246da52d260a1cf1713acd995543eff19ea121ff27752446b65");
        UniswapDTO uniswapDTO = uniswapTransactionsParser.parseUniswapTransaction(tx);
        assertNotNull(uniswapDTO);
        assertFalse(uniswapDTO.isConfirmed());
    }

    @Test
    public void parse_swapExactTokensForTokens_farmValue() throws IOException {
        Transaction tx = web3Service
            .findTransaction("0x770aa761f532176d2675e45639bb4a30f08412e8cd856bf502a59cbbed5ad7c8");
        UniswapDTO uniswapDTO = uniswapTransactionsParser.parseUniswapTransaction(tx);
        assertDto(uniswapDTO,
            true,
            "SELL",
            "FARM",
            "USDC",
            0.000487262469914664,
            0.047069
        );
    }

    @Test
    public void parse_swapExactTokensForTokens2() throws IOException {
        Transaction tx = web3Service
            .findTransaction("0x157e40ee084ff19ca90f22f8e0f9f490ca3e7484e694efeae2e6d5ed38491cda");
        UniswapDTO uniswapDTO = uniswapTransactionsParser.parseUniswapTransaction(tx);
        assertDto(uniswapDTO,
            true,
            "BUY",
            "FARM",
            "USDC",
            1077.563651807382282222,
            103611.527533
        );
    }

    private void assertDto(UniswapDTO dto,
                           boolean success,
                           String type,
                           String in,
                           String out,
                           double amount,
                           double otherAmount) {
        assertNotNull(dto);
        assertEquals("Confirmed", success, dto.isConfirmed());
        assertAll(
            () -> assertEquals("DTO type", type, dto.getType()),
            () -> assertEquals("In", in, dto.getCoin()),
            () -> assertEquals("Out", out, dto.getOtherCoin()),
            () -> assertEquals("Amount", amount, dto.getAmount(), 0.0),
            () -> assertEquals("Other Amount", otherAmount, dto.getOtherAmount(), 0.0)
        );
    }
}
