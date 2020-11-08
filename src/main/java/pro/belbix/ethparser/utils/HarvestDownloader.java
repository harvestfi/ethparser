package pro.belbix.ethparser.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import pro.belbix.ethparser.web3.harvest.HarvestVaultDownloader;
import pro.belbix.ethparser.web3.harvest.Vaults;

@Service
public class HarvestDownloader {
    private static final Logger log = LoggerFactory.getLogger(HarvestDownloader.class);
    @Autowired
    private HarvestVaultDownloader harvestVaultDownloader;

    public void start() {

        Set<String> include = new HashSet<>(
            Arrays.asList(
//            "UNI_ETH_DAI",
//            "UNI_ETH_USDC",
//            "UNI_ETH_USDT",
//            "UNI_ETH_WBTC",
//            "WETH",
//            "USDC",
//            "USDT",
//            "DAI",
//            "WBTC",
//            "RENBTC",
//            "CRVRENWBTC",
//            "SUSHI_WBTC_TBTC",
//            "YCRV",
//            "3CRV",
//            "TUSD",
                "WETH_V0",
                "USDC_V0",
                "USDT_V0",
                "DAI_V0",
                "WBTC_V0",
                "RENBTC_V0",
                "CRVRENWBTC_V0",
                "UNI_ETH_DAI_V0",
                "UNI_ETH_USDC_V0"
            )
        );

        for (String vaultName : Vaults.vaultNames.keySet()) {
            if (!include.contains(Vaults.vaultNames.get(vaultName))) {
                continue;
            }
            harvestVaultDownloader.parseVault(vaultName, new DefaultBlockParameterNumber(10765094)); //fromBlock = null means last from DB
        }
    }

}
