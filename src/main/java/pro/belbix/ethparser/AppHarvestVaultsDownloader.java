package pro.belbix.ethparser;

import static pro.belbix.ethparser.web3.Web3Service.BLOCK_NUMBER_30_AUGUST_2020;
import static pro.belbix.ethparser.web3.harvest.Vaults.UNI_ETH_DAI;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import pro.belbix.ethparser.web3.harvest.HarvestVaultDownloader;
import pro.belbix.ethparser.web3.harvest.Vaults;

@SpringBootApplication
public class AppHarvestVaultsDownloader {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AppHarvestVaultsDownloader.class, args);
        HarvestVaultDownloader harvestVaultDownloader = context.getBean(HarvestVaultDownloader.class);
//        harvestVaultDownloader.parseVault(UNI_ETH_DAI, new DefaultBlockParameterNumber(11059006));
//        System.exit(0);
        for (String vaultName : Vaults.vaultNames.keySet()) {
            if (!include.contains(Vaults.vaultNames.get(vaultName))) {
                continue;
            }
            harvestVaultDownloader.parseVault(vaultName, BLOCK_NUMBER_30_AUGUST_2020); //fromBlock = null means last from DB
        }

        context.close();
    }

    private static Set<String> include = new HashSet<>(
        Arrays.asList(
//            "UNI_ETH_DAI",
//            "UNI_ETH_USDC",
//            "UNI_ETH_USDT",
//            "UNI_ETH_WBTC",
            "WETH",
            "USDC",
            "USDT",
            "DAI",
            "WBTC",
            "RENBTC",
            "CRVRENWBTC",
            "SUSHI_WBTC_TBTC"
        )
    );

}
