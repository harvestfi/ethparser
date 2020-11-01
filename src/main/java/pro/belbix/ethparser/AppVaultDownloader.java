package pro.belbix.ethparser;

import static pro.belbix.ethparser.web3.harvest.Vaults.DAI;
import static pro.belbix.ethparser.web3.harvest.Vaults.WBTC;
import static pro.belbix.ethparser.web3.harvest.Vaults.WETH;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.web3j.protocol.core.DefaultBlockParameterName;
import pro.belbix.ethparser.web3.harvest.VaultDownloader;

@SpringBootApplication
public class AppVaultDownloader {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AppVaultDownloader.class, args);
        VaultDownloader vaultDownloader = context.getBean(VaultDownloader.class);
        DefaultBlockParameterName from = DefaultBlockParameterName.EARLIEST;
//        DefaultBlockParameterName from = DefaultBlockParameter.valueOf(new BigInteger(""));
//        vaultDownloader.parseVault(WBTC, from);
//        vaultDownloader.parseVault(WETH, from);
        vaultDownloader.parseVault(DAI, from);

        System.exit(0);
    }

}
