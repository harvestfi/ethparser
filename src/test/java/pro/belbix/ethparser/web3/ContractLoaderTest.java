package pro.belbix.ethparser.web3;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map.Entry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.entity.eth.PoolEntity;
import pro.belbix.ethparser.entity.eth.TokenEntity;
import pro.belbix.ethparser.entity.eth.UniPairEntity;
import pro.belbix.ethparser.entity.eth.VaultEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.PoolRepository;
import pro.belbix.ethparser.repositories.TokenRepository;
import pro.belbix.ethparser.repositories.UniPairRepository;
import pro.belbix.ethparser.repositories.VaultRepository;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.contracts.HarvestVaultAddresses;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class ContractLoaderTest {

    @Autowired
    private ContractLoader contractLoader;
    @Autowired
    private AppProperties appProperties;
    @Autowired
    private VaultRepository vaultRepository;
    @Autowired
    private PoolRepository poolRepository;
    @Autowired
    private UniPairRepository uniPairRepository;
    @Autowired
    private TokenRepository tokenRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void fullRunShouldBeOk() throws JsonProcessingException {
//        appProperties.setUpdateContracts(true);
        contractLoader.load();
        for (VaultEntity vaultEntity : vaultRepository.findAll()) {
            assertNotNull(vaultEntity);
            System.out.println(objectMapper.writeValueAsString(vaultEntity));
        }
        for (PoolEntity poolEntity : poolRepository.findAll()) {
            assertNotNull(poolEntity);
            System.out.println(objectMapper.writeValueAsString(poolEntity));
        }
        for (UniPairEntity uniPairEntity : uniPairRepository.findAll()) {
            assertNotNull(uniPairEntity);
            System.out.println(objectMapper.writeValueAsString(uniPairEntity));
        }
        for (TokenEntity tokenEntity : tokenRepository.findAll()) {
            assertNotNull(tokenEntity);
            System.out.println(objectMapper.writeValueAsString(tokenEntity));
        }
    }

    @Test
    public void containsAllVaults() {
        contractLoader.load();
        for (String vaultAddress : ContractUtils.getAllVaultAddresses()) {
            boolean found = false;
            for (PoolEntity poolEntity : ContractLoader.poolsCacheByAddress.values()) {
                if (vaultAddress.equals(poolEntity.getLpToken().getAddress())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("not found " + vaultAddress);
            }
        }
    }

    @Test
    public void isLpTest() {
        contractLoader.load();
        assertAll(
            () -> assertTrue("UNI_ETH_DAI_V0", ContractUtils.isLp("UNI_ETH_DAI_V0")),
            () -> assertTrue("UNI_ETH_USDC_V0", ContractUtils.isLp("UNI_ETH_USDC_V0")),
            () -> assertTrue("UNI_ETH_USDT_V0", ContractUtils.isLp("UNI_ETH_USDT_V0")),
            () -> assertTrue("UNI_ETH_WBTC_V0", ContractUtils.isLp("UNI_ETH_WBTC_V0")),
            () -> assertTrue("UNI_ETH_DAI", ContractUtils.isLp("UNI_ETH_DAI")),
            () -> assertTrue("UNI_ETH_USDC", ContractUtils.isLp("UNI_ETH_USDC")),
            () -> assertTrue("UNI_ETH_USDT", ContractUtils.isLp("UNI_ETH_USDT")),
            () -> assertTrue("UNI_ETH_WBTC", ContractUtils.isLp("UNI_ETH_WBTC")),
            () -> assertTrue("SUSHI_WBTC_TBTC", ContractUtils.isLp("SUSHI_WBTC_TBTC")),
            () -> assertTrue("SUSHI_ETH_DAI", ContractUtils.isLp("SUSHI_ETH_DAI")),
            () -> assertTrue("SUSHI_ETH_USDC", ContractUtils.isLp("SUSHI_ETH_USDC")),
            () -> assertTrue("SUSHI_ETH_USDT", ContractUtils.isLp("SUSHI_ETH_USDT")),
            () -> assertTrue("SUSHI_ETH_WBTC", ContractUtils.isLp("SUSHI_ETH_WBTC")),
            () -> assertTrue("IDX_ETH_DPI", ContractUtils.isLp("IDX_ETH_DPI")),
            () -> assertTrue("UNI_BAC_DAI", ContractUtils.isLp("UNI_BAC_DAI")),
            () -> assertTrue("UNI_DAI_BAS", ContractUtils.isLp("UNI_DAI_BAS")),
            () -> assertTrue("SUSHI_MIC_USDT", ContractUtils.isLp("SUSHI_MIC_USDT")),
            () -> assertTrue("SUSHI_MIS_USDT", ContractUtils.isLp("SUSHI_MIS_USDT")),
            () -> assertTrue("ONEINCH_ETH_DAI", ContractUtils.isLp("ONEINCH_ETH_DAI")),
            () -> assertTrue("ONEINCH_ETH_USDC", ContractUtils.isLp("ONEINCH_ETH_USDC")),
            () -> assertTrue("ONEINCH_ETH_USDT", ContractUtils.isLp("ONEINCH_ETH_USDT")),
            () -> assertTrue("ONEINCH_ETH_WBTC", ContractUtils.isLp("ONEINCH_ETH_WBTC")),
            () -> assertTrue("DAI_BSG", ContractUtils.isLp("DAI_BSG")),
            () -> assertTrue("DAI_BSGS", ContractUtils.isLp("DAI_BSGS")),
            () -> assertTrue("MAAPL_UST", ContractUtils.isLp("MAAPL_UST")),
            () -> assertTrue("MAMZN_UST", ContractUtils.isLp("MAMZN_UST")),
            () -> assertTrue("MGOOGL_UST", ContractUtils.isLp("MGOOGL_UST")),
            () -> assertTrue("MTSLA_UST", ContractUtils.isLp("MTSLA_UST"))
        );


    }
}
