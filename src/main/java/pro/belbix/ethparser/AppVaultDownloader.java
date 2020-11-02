package pro.belbix.ethparser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.web3j.protocol.core.DefaultBlockParameterName;
import pro.belbix.ethparser.web3.harvest.VaultDownloader;
import pro.belbix.ethparser.web3.harvest.Vaults;

@SpringBootApplication
public class AppVaultDownloader {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AppVaultDownloader.class, args);
        VaultDownloader vaultDownloader = context.getBean(VaultDownloader.class);
        DefaultBlockParameterName from = DefaultBlockParameterName.EARLIEST;
//        DefaultBlockParameterName from = DefaultBlockParameter.valueOf(new BigInteger(""));
//        vaultDownloader.parseVault(WBTC, from);

        for (String vaultName : Vaults.vaultNames.keySet()) {
//            if (exclude.contains(Vaults.vaultNames.get(vaultName))) {
//                continue;
//            }
            vaultDownloader.parseVault(vaultName, from);
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
