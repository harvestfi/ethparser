package pro.belbix.ethparser.web3.harvest.utils;

import static java.util.Collections.singletonList;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.TUSD;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.harvest.contracts.Vaults;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;
import pro.belbix.ethparser.web3.harvest.parser.HarvestVaultParserV2;

@SuppressWarnings("rawtypes")
@Service
public class HarvestVaultDownloader {

    private static final int BATCH = 10000;
    private static final Logger logger = LoggerFactory.getLogger(HarvestVaultDownloader.class);
    private final Web3Service web3Service;
    private final HarvestDBService harvestDBService;
    private final HarvestVaultParserV2 harvestVaultParser;
    private final PriceProvider priceProvider;

    public HarvestVaultDownloader(Web3Service web3Service, HarvestDBService harvestDBService,
                                  HarvestVaultParserV2 harvestVaultParser,
                                  PriceProvider priceProvider) {
        this.web3Service = web3Service;
        this.harvestDBService = harvestDBService;
        this.harvestVaultParser = harvestVaultParser;
        this.priceProvider = priceProvider;
    }

    public void start() {

        Set<String> include = new HashSet<>(
            Arrays.asList(
//                WETH_V0,
//                USDC_V0
//                USDT_V0,
//                DAI_V0,
//                WBTC_V0,
//                RENBTC_V0,
//                CRVRENWBTC_V0,
//                UNI_ETH_DAI_V0,
//                UNI_ETH_USDC_V0,
//                UNI_ETH_USDT_V0,
//                UNI_ETH_WBTC_V0
//                UNI_ETH_DAI,
//                UNI_ETH_USDC,
//                UNI_ETH_USDT,
//                UNI_ETH_WBTC,
//                WETH,
//                USDC,
//                USDT,
//                DAI,
//                WBTC,
//                RENBTC,
//                CRVRENWBTC,
//                SUSHI_WBTC_TBTC,
//                YCRV,
//                _3CRV,
                TUSD
//                CRV_TBTC,
//                PS
//                CRV_CMPND
//                CRV_BUSD
//                CRV_USDN
//                PS_V0
//                SUSHI_ETH_DAI,
//                SUSHI_ETH_USDC,
//                SUSHI_ETH_USDT,
//                SUSHI_ETH_WBTC
//                YCRV_V0
//                IDX_ETH_DPI
            )
        );

        for (String vaultHash : Vaults.vaultNames.keySet()) {
            if (!include.contains(vaultHash)) {
                continue;
            }

            LoopUtils.handleLoop(10770000, null, (start, end) -> parse(vaultHash, start, end));
//            parseVault(vaultHash, 11021480, 11223256);
        }
    }

    private void parse(String vaultHash, int start, Integer end) {
        List<LogResult> logResults = web3Service.fetchContractLogs(singletonList(vaultHash), start, end);
        if (logResults.isEmpty()) {
            logger.info("Empty log {} {} {}", start, end, vaultHash);
            return;
        }
        for (LogResult logResult : logResults) {
            try {
                HarvestDTO dto = harvestVaultParser.parseVaultLog((Log) logResult.get());
                if (dto != null) {
                    dto.setPrices(priceProvider.getAllPrices(dto.getBlock().longValue()));
                    harvestDBService.saveHarvestDTO(dto);
                }
            } catch (Exception e) {
                logger.error("error with " + logResult.get(), e);
            }
        }
    }


}
