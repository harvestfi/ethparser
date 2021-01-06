package pro.belbix.ethparser.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.erc20.downloader.TransferDownloader;
import pro.belbix.ethparser.web3.harvest.downloader.HardWorkDownloader;
import pro.belbix.ethparser.web3.harvest.downloader.HarvestVaultDownloader;
import pro.belbix.ethparser.web3.harvest.downloader.RewardDownloader;
import pro.belbix.ethparser.web3.uniswap.downloader.DownloadIncome;
import pro.belbix.ethparser.web3.uniswap.downloader.UniswapLpDownloader;

@Service
@Log4j2
public class UtilsStarter {

    private final AppProperties appProperties;
    private final UniswapLpDownloader uniswapLpDownloader;
    private final HarvestVaultDownloader harvestVaultDownloader;
    private final TvlRecalculate tvlRecalculate;
    private final DownloadIncome downloadIncome;
    private final HardWorkDownloader hardWorkDownloader;
    private final HardWorkRecalculate hardWorkRecalculate;
    private final RewardDownloader rewardDownloader;
    private final BlockCacher blockCacher;
    private final LpTvlRecalculate lpTvlRecalculate;
    private final OwnerBalanceRecalculate ownerBalanceRecalculate;
    private final OwnerCountRecalculate ownerCountRecalculate;
    private final MigrationRecalculate migrationRecalculate;
    private final TransferDownloader transferDownloader;
    private final TransfersRecalculate transfersRecalculate;
    private final RewardRecalculate rewardRecalculate;

    public UtilsStarter(AppProperties appProperties,
                        UniswapLpDownloader uniswapLpDownloader,
                        HarvestVaultDownloader harvestVaultDownloader,
                        TvlRecalculate tvlRecalculate,
                        DownloadIncome downloadIncome,
                        HardWorkDownloader hardWorkDownloader,
                        HardWorkRecalculate hardWorkRecalculate,
                        RewardDownloader rewardDownloader, BlockCacher blockCacher,
                        LpTvlRecalculate lpTvlRecalculate,
                        OwnerBalanceRecalculate ownerBalanceRecalculate,
                        OwnerCountRecalculate ownerCountRecalculate,
                        MigrationRecalculate migrationRecalculate,
                        TransferDownloader transferDownloader,
                        TransfersRecalculate transfersRecalculate,
                        RewardRecalculate rewardRecalculate) {
        this.appProperties = appProperties;
        this.uniswapLpDownloader = uniswapLpDownloader;
        this.harvestVaultDownloader = harvestVaultDownloader;
        this.tvlRecalculate = tvlRecalculate;
        this.downloadIncome = downloadIncome;
        this.hardWorkDownloader = hardWorkDownloader;
        this.hardWorkRecalculate = hardWorkRecalculate;
        this.rewardDownloader = rewardDownloader;
        this.blockCacher = blockCacher;
        this.lpTvlRecalculate = lpTvlRecalculate;
        this.ownerBalanceRecalculate = ownerBalanceRecalculate;
        this.ownerCountRecalculate = ownerCountRecalculate;
        this.migrationRecalculate = migrationRecalculate;
        this.transferDownloader = transferDownloader;
        this.transfersRecalculate = transfersRecalculate;
        this.rewardRecalculate = rewardRecalculate;
    }

    public void startUtils() {
        log.info("Start utils");
        if ("cache-blocks".equals(appProperties.getStartUtil())) {
            blockCacher.cacheBlocks();
        } else if ("uniswap-download".equals(appProperties.getStartUtil())) {
            uniswapLpDownloader.start();
        } else if ("harvest-download".equals(appProperties.getStartUtil())) {
            harvestVaultDownloader.start();
        } else if ("tvl-recalculate".equals(appProperties.getStartUtil())) {
            tvlRecalculate.start();
        } else if ("income-download".equals(appProperties.getStartUtil())) {
            downloadIncome.start();
        } else if ("hardwork-download".equals(appProperties.getStartUtil())) {
            hardWorkDownloader.start();
        } else if ("hardwork-recalculate".equals(appProperties.getStartUtil())) {
            hardWorkRecalculate.start();
        } else if ("reward-download".equals(appProperties.getStartUtil())) {
            rewardDownloader.start();
        } else if ("lp-tvl-recalculate".equals(appProperties.getStartUtil())) {
            lpTvlRecalculate.start();
        } else if ("balances-recalculate".equals(appProperties.getStartUtil())) {
            ownerBalanceRecalculate.start();
        } else if ("owners-count-recalculate".equals(appProperties.getStartUtil())) {
            ownerCountRecalculate.start();
        } else if ("migration-recalculate".equals(appProperties.getStartUtil())) {
            migrationRecalculate.start();
        } else if ("rewards-recalculate".equals(appProperties.getStartUtil())) {
            rewardRecalculate.start();
        }else if ("transfer-download".equals(appProperties.getStartUtil())) {
            transferDownloader.start();
        } else if ("transfer-recalculate".equals(appProperties.getStartUtil())) {
            transfersRecalculate.start();
        }
        log.info("Utils completed");
        System.exit(0);
    }


}
