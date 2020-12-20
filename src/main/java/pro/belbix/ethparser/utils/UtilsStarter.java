package pro.belbix.ethparser.utils;

import org.springframework.stereotype.Service;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.harvest.utils.HardWorkDownloader;
import pro.belbix.ethparser.web3.harvest.utils.HarvestVaultDownloader;
import pro.belbix.ethparser.web3.harvest.utils.RewardDownloader;
import pro.belbix.ethparser.web3.uniswap.utils.DownloadIncome;
import pro.belbix.ethparser.web3.uniswap.utils.UniswapLpDownloader;

@Service
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

    public UtilsStarter(AppProperties appProperties,
                        UniswapLpDownloader uniswapLpDownloader,
                        HarvestVaultDownloader harvestVaultDownloader,
                        TvlRecalculate tvlRecalculate,
                        DownloadIncome downloadIncome,
                        HardWorkDownloader hardWorkDownloader,
                        HardWorkRecalculate hardWorkRecalculate,
                        RewardDownloader rewardDownloader, BlockCacher blockCacher,
                        LpTvlRecalculate lpTvlRecalculate) {
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
    }

    public void startUtils() {
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
        }
    }



}
