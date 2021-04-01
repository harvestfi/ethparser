package pro.belbix.ethparser.web3.erc20.parser;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static pro.belbix.ethparser.TestUtils.numberFormat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.TransferDTO;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.contracts.EthContractConstants;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class TransferParserTest {

  @Autowired
  private Web3Functions web3Functions;
  @Autowired
  private PriceProvider priceProvider;
  @Autowired
  private TransferParser transferParser;

  @Autowired
  private ContractLoader contractLoader;

   @BeforeEach
  public void setUp() {
    contractLoader.load();
    priceProvider.setUpdateBlockDifference(1);
  }

  // it is a self destructed contract
  @Test
  public void testParseFARM_OneInch() {
        TransferDTO dto = parserTest(EthContractConstants.FARM_TOKEN,
            11631545,
            0,
            "FARM",
            "0x514906fc121c7878424a5c928cad1852cc545892",
            "0x56feaccb7f750b997b36a68625c7c596f0b41a58",
            "99.79670382",
            "LP_RECEIVE",
            "0x030341f1"
        );
        assertNotNull(dto);
    }

    @Test
    public void testParseFARM_firstMint() {
        TransferDTO dto = parserTest(EthContractConstants.FARM_TOKEN,
            10776698,
            0,
            "FARM",
            "0x0000000000000000000000000000000000000000",
            "0xf00dd244228f51547f0563e60bca65a30fbf5f7f",
            "40298.37000000",
            "MINT",
            "mint"
        );
        assertNotNull(dto);
    }

    @Test
    public void testParseFARM_LP_REM2() {
        TransferDTO dto = parserTest(EthContractConstants.FARM_TOKEN,
            11558046,
            0,
            "FARM",
            "0x56feaccb7f750b997b36a68625c7c596f0b41a58",
            "0x7a250d5630b4cf539739df2c5dacb4c659f2488d",
            "123.30207029",
            "LP_REM",
            "removeLiquidityETH"
        );
        assertNotNull(dto);
    }

    @Test
    public void testParseFARM_LP_REM() {
        TransferDTO dto = parserTest(EthContractConstants.FARM_TOKEN,
            10931588,
            0,
            "FARM",
            "0x514906fc121c7878424a5c928cad1852cc545892",
            "0xe8615e363d10660f939e9bc979255ca9a2799077",
            "455.86301384",
            "LP_REM",
            "removeLiquidityWithPermit"
        );
        assertNotNull(dto);
    }

    @Test
    public void testParseFARM_LP_ADD() {
        TransferDTO dto = parserTest(EthContractConstants.FARM_TOKEN,
            10997139,
            0,
            "FARM",
            "0x152ee76773492c2d9e996a3212bd26fa8f9366ec",
            "0x514906fc121c7878424a5c928cad1852cc545892",
            "28.00000000",
            "LP_ADD",
            "addLiquidity"
        );
        assertNotNull(dto);
    }

    @Test
    public void testParseFARM_LP_BUY() {
        TransferDTO dto = parserTest(EthContractConstants.FARM_TOKEN,
            11366155,
            0,
            "FARM",
            "0x514906fc121c7878424a5c928cad1852cc545892",
            "0x05f8fa4430a899dbdb3aef05f436a613d2f2a6b3",
            "11.00000000",
            "LP_BUY",
            "swapTokensForExactTokens"
        );
        assertNotNull(dto);
    }

    @Test
    public void testParseFARM_LP_SELL() {
        TransferDTO dto = parserTest(EthContractConstants.FARM_TOKEN,
            11373041,
            0,
            "FARM",
            "0xec8fd26fe6583b4e78c63aa8cf0b7c1835950b6a",
            "0x514906fc121c7878424a5c928cad1852cc545892",
            "50.00000000",
            "LP_SELL",
            "swapExactTokensForTokens"
        );
        assertNotNull(dto);
    }

    @Test
    public void testParseFARM_exit1() {
        TransferDTO dto = parserTest(EthContractConstants.FARM_TOKEN,
            11337723,
            0,
            "FARM",
            "0x8f5adc58b32d4e5ca02eac0e293d35855999436c",
            "0x25550cccbd68533fa04bfd3e3ac4d09f9e00fc50",
            "180981.24181470",
            "PS_INTERNAL",
            "exit"
        );
        assertNotNull(dto);
    }

    @Test
    public void testParseFARM_exit2() {
        TransferDTO dto = parserTest(EthContractConstants.FARM_TOKEN,
            11337723,
            1,
            "FARM",
            "0x8f5adc58b32d4e5ca02eac0e293d35855999436c",
            "0x25550cccbd68533fa04bfd3e3ac4d09f9e00fc50",
            "2.07976153",
            "PS_INTERNAL",
            "exit"
        );
        assertNotNull(dto);
    }

    @Test
    public void testParseFARM_exit3() {
        TransferDTO dto = parserTest(EthContractConstants.FARM_TOKEN,
            11337723,
            2,
            "FARM",
            "0x25550cccbd68533fa04bfd3e3ac4d09f9e00fc50",
            "0x27c7e3758983f00085c5bbc91ecf0c91baae7146",
            "1.06503922",
            "PS_EXIT",
            "exit"
        );
        assertNotNull(dto);
    }

    @Test
    public void testParseFARM_exit4() {
        TransferDTO dto = parserTest(EthContractConstants.FARM_TOKEN,
            11337723,
            5,
            "FARM",
            "0x25550cccbd68533fa04bfd3e3ac4d09f9e00fc50",
            "0x8f5adc58b32d4e5ca02eac0e293d35855999436c",
            "180982.25653701",
            "PS_INTERNAL",
            "exit"
        );
        assertNotNull(dto);
    }

    @Test
    public void testParseFARM_stake1() {
        TransferDTO dto = parserTest(EthContractConstants.FARM_TOKEN,
            11337691,
            0,
            "FARM",
            "0x8f5adc58b32d4e5ca02eac0e293d35855999436c",
            "0x25550cccbd68533fa04bfd3e3ac4d09f9e00fc50",
            "180905.75962281",
            "PS_INTERNAL",
            "stake"
        );
        assertNotNull(dto);
    }

    @Test
    public void testParseFARM_stake2() {
        TransferDTO dto = parserTest(EthContractConstants.FARM_TOKEN,
            11337691,
            1,
            "FARM",
            "0x8f5adc58b32d4e5ca02eac0e293d35855999436c",
            "0x25550cccbd68533fa04bfd3e3ac4d09f9e00fc50",
            "10.56597522",
            "PS_INTERNAL",
            "stake"
        );
        assertNotNull(dto);
    }

    @Test
    public void testParseFARM_stake3() {
        TransferDTO dto = parserTest(EthContractConstants.FARM_TOKEN,
            11337691,
            6,
            "FARM",
            "0x25550cccbd68533fa04bfd3e3ac4d09f9e00fc50",
            "0x8f5adc58b32d4e5ca02eac0e293d35855999436c",
            "180981.24181470",
            "PS_INTERNAL",
            "stake"
        );
        assertNotNull(dto);
    }

    @Test
    public void testParseFARM_balancer() {
        TransferDTO dto = parserTest(EthContractConstants.FARM_TOKEN,
            10777054,
            1,
            "FARM",
            "0x3e66b66fd1d0b02fda6c811da9e0547970db2f21",
            "0xefd0199657b444856e3259ed8e3c39ee43cf51dc",
            "101.21369473",
            "BAL_TX",
            "0xe2b39746"
        );
        assertNotNull(dto);
    }

    @Test
    public void testParseFARM_bot() {
        TransferDTO dto = parserTest(EthContractConstants.FARM_TOKEN,
            10850197,
            1,
            "FARM",
            "0x7da20beeb68f40181418c5e58127ee4e7cd12f62",
            "0xb39ce7fa5953bebc6697112e88cd11579cbca579",
            "5.68416749",
            "BOT",
            "0x375e243b"
        );
        assertNotNull(dto);
    }

    @Test
    public void testParseFARM_swap() {
        TransferDTO dto = parserTest(EthContractConstants.FARM_TOKEN,
            11055960,
            0,
            "FARM",
            "0x8d98f2bcaf61811a2cc813a4db65286b5db785f6",
            "0x11111254369792b2ca5d084ab5eea397ca8fa48b",
            "1.58631881",
            "ONE_INCH",
            "swap"
        );
        assertNotNull(dto);
    }

    @Test
    public void testParseFARM_addLiquidity() {
        TransferDTO dto = parserTest(EthContractConstants.FARM_TOKEN,
            11362801,
            1,
            "FARM",
            "0xc3aee7f07034e846243c60acbe8cf5b8a71e4584",
            "0x514906fc121c7878424a5c928cad1852cc545892",
            "9.64157915",
            "LP_ADD",
            "addLiquidity"
        );
        assertNotNull(dto);
    }

    @Test
    public void testParseFARM_transfer() {
        parserTest(EthContractConstants.FARM_TOKEN,
            11571359,
            0,
            "FARM",
            "0xa910f92acdaf488fa6ef02174fb86208ad7722ba",
            "0x7a77784d32fef468c2a46cdf4ef2e15ef2cb2226",
            "4.25506623",
            "COMMON",
            "transfer"
        );
    }

    @Test
    public void testParseFARM_HARD_WORK() {
        parserTest(EthContractConstants.FARM_TOKEN,
            11045532,
            0,
            "FARM",
            "0x514906fc121c7878424a5c928cad1852cc545892",
            "0xef08a639cac2009fdad3773cc9f56d6a8feb1153",
            "5.85376525",
            "HARD_WORK",
            "doHardWork#V2"
        );
    }

    @Test
    public void testParseFARM_HARD_WORK2() {
        parserTest(EthContractConstants.FARM_TOKEN,
            11045532,
            1,
            "FARM",
            "0xef08a639cac2009fdad3773cc9f56d6a8feb1153",
            "0x8f5adc58b32d4e5ca02eac0e293d35855999436c",
            "5.85376525",
            "HARD_WORK",
            "doHardWork#V2"
        );
    }

    @Test
    public void testParseFARM_swapExactTokensForETH() {
        parserTest(EthContractConstants.FARM_TOKEN,
            10777107,
            0,
            "FARM",
            "0xefd0199657b444856e3259ed8e3c39ee43cf51dc",
            "0x514906fc121c7878424a5c928cad1852cc545892",
            "25.00000000",
            "LP_SELL",
            "swapExactTokensForETH"
        );
    }

    private TransferDTO parserTest(
        String contractHash,
        int onBlock,
        int logId,
        String name,
        String owner,
        String recipient,
        String value,
        String type,
        String methodName
    ) {
      value = numberFormat(value);
        List<LogResult> logResults = web3Functions
            .fetchContractLogs(singletonList(contractHash), onBlock, onBlock);
        assertTrue("Log smaller then necessary", logId < logResults.size());
        TransferDTO dto = transferParser.parseLog((Log) logResults.get(logId).get());
        assertDto(dto, name, owner, recipient, value, type, methodName);
        return dto;
    }

    private void assertDto(TransferDTO dto, String name, String owner,
                           String recipient, String value, String type, String methodName) {
      assertNotNull(dto, "Dto is null");
      assertAll(
          () -> assertEquals("name", name, dto.getName()),
          () -> assertEquals("owner", owner, dto.getOwner()),
          () -> assertEquals("recipient", recipient, dto.getRecipient()),
          () -> assertEquals("value", value, String.format("%.8f", dto.getValue())),
          () -> assertEquals("type", type, dto.getType()),
          () -> assertEquals("methodName", methodName, dto.getMethodName())
      );
    }

}
