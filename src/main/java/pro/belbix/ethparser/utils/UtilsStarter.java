package pro.belbix.ethparser.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.harvest.utils.HardWorkDownloader;
import pro.belbix.ethparser.web3.harvest.utils.HarvestVaultDownloader;
import pro.belbix.ethparser.web3.harvest.utils.RewardDownloader;
import pro.belbix.ethparser.web3.uniswap.utils.DownloadIncome;
import pro.belbix.ethparser.web3.uniswap.utils.UniswapLpDownloader;

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
                        OwnerCountRecalculate ownerCountRecalculate) {
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
        }
        log.info("Utils completed");
        System.exit(0);
    }


}
