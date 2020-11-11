package pro.belbix.ethparser.web3.harvest;

import static java.util.Collections.singletonList;
import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;
import static pro.belbix.ethparser.web3.harvest.Vaults.*;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.web3.Web3Service;

@SuppressWarnings("rawtypes")
@Service
public class HarvestVaultDownloader {

    private static final int BATCH = 100000;
    private static final Logger logger = LoggerFactory.getLogger(HarvestVaultDownloader.class);
    private final Web3Service web3Service;
    private final HarvestDBService harvestDBService;
    private final HarvestVaultParserV2 harvestVaultParser;

    public HarvestVaultDownloader(Web3Service web3Service, HarvestDBService harvestDBService,
                                  HarvestVaultParserV2 harvestVaultParser) {
        this.web3Service = web3Service;
        this.harvestDBService = harvestDBService;
        this.harvestVaultParser = harvestVaultParser;
    }

    public void start() {

        Set<String> include = new HashSet<>(
            Arrays.asList(
//                WETH_V0,
//                USDC_V0,
//                USDT_V0,
//                DAI_V0,
//                WBTC_V0,
//                RENBTC_V0,
//                CRVRENWBTC_V0,
//                UNI_ETH_DAI_V0,
//                UNI_ETH_USDC_V0,
//                UNI_ETH_USDT_V0,
//                UNI_ETH_WBTC_V0,
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
//                TUSD,
                CRV_TBTC
            )
        );

        for (String vaultHash : Vaults.vaultNames.keySet()) {
            if (!include.contains(vaultHash)) {
                continue;
            }
            parseVault(vaultHash, 10770000, null);
//            parseVault(vaultHash, 11021480, 11223256);
        }
    }

    public void parseVault(String vaultHash, Integer from, Integer to) {
        if (from == null) {
            BigInteger lastBlock = harvestDBService.lastBlock();
            from = lastBlock.intValue();
            logger.info("Use last block " + lastBlock);
        }

        while (true) {
            Integer end = null;
            if (to != null) {
                end = from + BATCH;
            }
            parse(vaultHash, from, end);
            from = end;
            if (to != null) {
                if (end > to) {
                    break;
                }
            } else {
                break;
            }
        }
    }

    private void parse(String vaultHash, int start, Integer end) {
        DefaultBlockParameter fromBlock = new DefaultBlockParameterNumber(new BigInteger(start + ""));
        DefaultBlockParameter toBlock;
        if (end == null) {
            toBlock = LATEST;
        } else {
            toBlock = new DefaultBlockParameterNumber(new BigInteger(end + ""));
        }
        List<LogResult> logResults = web3Service.fetchContractLogs(singletonList(vaultHash), fromBlock, toBlock);
        if (logResults.isEmpty()) {
            logger.info("Empty log {} {} {}", start, end, vaultHash);
            return;
        }
        for (LogResult logResult : logResults) {
            try {
                HarvestDTO dto = harvestVaultParser.parseVaultLog((Log) logResult.get());
                if (dto != null) {
                    harvestDBService.saveHarvestDTO(dto);
                }
            } catch (Exception e) {
                logger.error("error with " + logResult.get(), e);
            }
        }
    }


}
