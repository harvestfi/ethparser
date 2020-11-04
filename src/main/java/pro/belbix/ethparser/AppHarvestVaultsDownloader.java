package pro.belbix.ethparser;

import static pro.belbix.ethparser.web3.Web3Service.BLOCK_NUMBER_30_AUGUST_2020;
import static pro.belbix.ethparser.web3.harvest.Vaults.UNI_ETH_DAI;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.web3j.protocol.core.DefaultBlockParameter;
import pro.belbix.ethparser.web3.harvest.HarvestVaultDownloader;
import pro.belbix.ethparser.web3.harvest.Vaults;

@SpringBootApplication
public class AppHarvestVaultsDownloader {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AppHarvestVaultsDownloader.class, args);
        HarvestVaultDownloader harvestVaultDownloader = context.getBean(HarvestVaultDownloader.class);
//        DefaultBlockParameterName from = DefaultBlockParameterName.EARLIEST;
        DefaultBlockParameter from = DefaultBlockParameter.valueOf(new BigInteger("11176203"));
//        harvestVaultDownloader.parseVault(UNI_ETH_DAI, BLOCK_NUMBER_30_AUGUST_2020);

        for (String vaultName : Vaults.vaultNames.keySet()) {
//            if (exclude.contains(Vaults.vaultNames.get(vaultName))) {
//                continue;
//            }
            harvestVaultDownloader.parseVault(vaultName, BLOCK_NUMBER_30_AUGUST_2020); //fromBlock = null means last from DB
        }

        context.close();
    }

    private static Set<String> exclude = new HashSet<>(
        Arrays.asList(
            "RENBTC",
            "TUSD",
            "USDT",
            "WBTC",
            "WBTC_TBTC",
            "WETH",
            "WETH_DAI_LP",
            "WETH_USDC_LP",
            "WETH_USDT_LP",
            "WETH_WBTC_LP"
        )
    );

}
