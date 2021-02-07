package pro.belbix.ethparser.web3.contracts;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.DEPLOYER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.disposables.Disposable;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.entity.eth.PoolEntity;
import pro.belbix.ethparser.entity.eth.TokenEntity;
import pro.belbix.ethparser.entity.eth.UniPairEntity;
import pro.belbix.ethparser.entity.eth.VaultEntity;
import pro.belbix.ethparser.entity.eth.VaultToPoolEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.eth.PoolRepository;
import pro.belbix.ethparser.repositories.eth.TokenRepository;
import pro.belbix.ethparser.repositories.eth.UniPairRepository;
import pro.belbix.ethparser.repositories.eth.VaultRepository;
import pro.belbix.ethparser.repositories.eth.VaultToPoolRepository;
import pro.belbix.ethparser.web3.Web3Service;

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
    @Autowired
    private VaultToPoolRepository vaultToPoolRepository;
    @Autowired
    private Web3Service web3Service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Ignore
    public void fullRunShouldBeOk() throws JsonProcessingException {
//        appProperties.setUpdateContracts(true);
        contractLoader.load();
        System.out.println("**************** VAULTS ************************");
        for (VaultEntity vaultEntity : vaultRepository.findAll()) {
            assertNotNull(vaultEntity);
            System.out.println(objectMapper.writeValueAsString(vaultEntity));
        }
        System.out.println("**************** POOLS ************************");
        for (PoolEntity poolEntity : poolRepository.findAll()) {
            assertNotNull(poolEntity);
            System.out.println(objectMapper.writeValueAsString(poolEntity));
        }
        System.out.println("**************** UNI PAIRS ************************");
        for (UniPairEntity uniPairEntity : uniPairRepository.findAll()) {
            assertNotNull(uniPairEntity);
            System.out.println(objectMapper.writeValueAsString(uniPairEntity));
        }
        System.out.println("**************** TOKENS ************************");
        for (TokenEntity tokenEntity : tokenRepository.findAll()) {
            assertNotNull(tokenEntity);
            System.out.println(objectMapper.writeValueAsString(tokenEntity));
        }
        System.out.println("**************** VAULT TO POOLS ************************");
        for (VaultToPoolEntity vaultToPoolEntity : vaultToPoolRepository.findAll()) {
            assertNotNull(vaultToPoolEntity);
            System.out.println(objectMapper.writeValueAsString(vaultToPoolEntity));
        }
    }

    @Test
    @Ignore
    public void loadKeyBlocks() {
        appProperties.setUpdateContracts(true);
        contractLoader.loadKeyBlocks();
    }

    //    @Test
//    public void containsAllVaults() {
//        contractLoader.load();
//        for (String vaultAddress : ContractUtils.getAllVaultAddresses()) {
//            boolean found = false;
//            for (PoolEntity poolEntity : ContractLoader.poolsCacheByAddress.values()) {
//                if (vaultAddress.equals(poolEntity.getLpToken().getAddress())) {
//                    found = true;
//                    break;
//                }
//            }
//            if (!found) {
//                System.out.println("not found " + vaultAddress);
//            }
//        }
//    }

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

    @Test
    @Ignore
    public void getCreateDate() throws InterruptedException {
        DefaultBlockParameter start = DefaultBlockParameter
            .valueOf(new BigInteger("10770072"));
        DefaultBlockParameter end = DefaultBlockParameter
            .valueOf(new BigInteger("11792110"));

//        DefaultBlockParameter start = DefaultBlockParameter
//            .valueOf(new BigInteger("11792065"));
        // , 10770072, 11792110
        AtomicInteger counter = new AtomicInteger();
        Disposable disposable = web3Service.getWeb3().replayPastTransactionsFlowable(start, end)
            .subscribe(tx -> {
                counter.incrementAndGet();
                if (counter.get() % 10000 == 0) {
                    System.out.println("Handle " + counter.get() + " " + tx.getBlockNumber());
                }
                if (DEPLOYER.equals(tx.getFrom())) {
                    TransactionReceipt receipt = web3Service.fetchTransactionReceipt(tx.getHash());

                    Contract vault = HarvestVaultAddresses.VAULTS.stream()
                        .filter(v -> v.getAddress().equals(receipt.getContractAddress()))
                        .findFirst().orElse(null);
                    if (vault != null) {
                        System.out.println(receipt.getBlockNumber() + " " + vault.getName());
                        vault.setCreatedOnBlock(receipt.getBlockNumber().intValue());
                    }

                    Contract pool = HarvestPoolAddresses.POOLS.stream()
                        .filter(v -> v.getAddress().equals(receipt.getContractAddress()))
                        .findFirst().orElse(null);
                    if (pool != null) {
                        System.out.println(receipt.getBlockNumber() + " " + pool.getName());
                        pool.setCreatedOnBlock(receipt.getBlockNumber().intValue());
                    }
                }
            });
        while (!disposable.isDisposed()) {
            Thread.sleep(100);
        }
        System.out.println("***************************************************");
        System.out.println("******************* POOLS ********************************");
        System.out.println("***************************************************");
        HarvestPoolAddresses.POOLS.forEach(c ->
            System.out.println(c.getCreatedOnBlock() + " " + c.getName()));

        System.out.println("***************************************************");
        System.out.println("******************* VAULTS ********************************");
        System.out.println("***************************************************");

        HarvestVaultAddresses.VAULTS.forEach(c ->
            System.out.println(c.getCreatedOnBlock() + " " + c.getName()));

    }
}
