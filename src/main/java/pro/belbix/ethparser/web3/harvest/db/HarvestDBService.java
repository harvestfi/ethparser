package pro.belbix.ethparser.web3.harvest.db;

import static java.time.temporal.ChronoUnit.DAYS;
import static pro.belbix.ethparser.web3.erc20.Tokens.FARM_NAME;
import static pro.belbix.ethparser.web3.harvest.parser.UniToHarvestConverter.allowContracts;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.entity.HarvestTvlEntity;
import pro.belbix.ethparser.model.LpStat;
import pro.belbix.ethparser.model.PricesModel;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.HarvestRepository;
import pro.belbix.ethparser.repositories.HarvestTvlRepository;
import pro.belbix.ethparser.repositories.UniswapRepository;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.harvest.contracts.Vaults;
import pro.belbix.ethparser.web3.uniswap.contracts.LpContracts;

@Service
public class HarvestDBService {

    private static final Logger log = LoggerFactory.getLogger(HarvestDBService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();

    private final HarvestRepository harvestRepository;
    private final AppProperties appProperties;
    private final HarvestTvlRepository harvestTvlRepository;
    private final UniswapRepository uniswapRepository;

    public HarvestDBService(HarvestRepository harvestRepository,
                            AppProperties appProperties,
                            HarvestTvlRepository harvestTvlRepository,
                            UniswapRepository uniswapRepository) {
        this.harvestRepository = harvestRepository;
        this.appProperties = appProperties;
        this.harvestTvlRepository = harvestTvlRepository;
        this.uniswapRepository = uniswapRepository;
    }

    public boolean saveHarvestDTO(HarvestDTO dto) {
        if (!appProperties.isOverrideDuplicates() && harvestRepository.existsById(dto.getId())) {
            log.info("Duplicate Harvest entry " + dto.getId());
            return false;
        }

        harvestRepository.saveAndFlush(dto);

        fillOwnersCount(dto);
        harvestRepository.saveAndFlush(dto);

        HarvestTvlEntity harvestTvl = calculateHarvestTvl(dto, true);
        harvestTvlRepository.save(harvestTvl);

        fillProfit(dto);
        harvestRepository.save(dto);
        return true;
    }

    public void fillOwnersCount(HarvestDTO dto) {
        Integer ownerCount = harvestRepository.fetchActualOwnerQuantity(dto.getVault(),
            Vaults.vaultNameToOldVaultName.get(dto.getVault()), dto.getBlockDate());
        if (ownerCount == null) {
            ownerCount = 0;
        }
        dto.setOwnerCount(ownerCount);

        Integer allOwnersCount = harvestRepository.fetchAllUsersQuantity(dto.getBlockDate());
        if (allOwnersCount == null) {
            allOwnersCount = 0;
        }
        dto.setAllOwnersCount(allOwnersCount);

        Integer allPoolsOwnerCount = harvestRepository.fetchAllPoolsUsersQuantity(
            Vaults.vaultNameToHash.keySet().stream()
                .filter(v -> !Vaults.isPsName(v))
                .collect(Collectors.toList()),
            dto.getBlockDate());
        if (allPoolsOwnerCount == null) {
            allPoolsOwnerCount = 0;
        }
        dto.setAllPoolsOwnersCount(allPoolsOwnerCount);
    }

    public HarvestTvlEntity calculateHarvestTvl(HarvestDTO dto, boolean checkTheSame) {
        double tvl = 0.0;
        Double farmPrice = 0.0;
        UniswapDTO uniswapDTO = uniswapRepository
            .findFirstByBlockDateBeforeAndCoinOrderByBlockDesc(dto.getBlockDate(), "FARM");
        if (uniswapDTO != null) {
            farmPrice = uniswapDTO.getLastPrice();
        }
        List<String> contracts = new ArrayList<>(Vaults.vaultHashToName.values());
        allowContracts.stream()
            .map(LpContracts.lpHashToName::get)
            .forEach(contracts::add);
        if (dto.getPrices() != null && !dto.getPrices().contains("NaN")) {
            for (String vaultName : contracts) {
                HarvestDTO lastHarvest = harvestRepository.fetchLastByVaultAndDate(vaultName, dto.getBlockDate());
                if (lastHarvest == null) {
                    continue;
                }
                if (lastHarvest.getId().equalsIgnoreCase(dto.getId())) {
                    lastHarvest = dto; // for avoiding JPA wrong synchronisation
                }
                tvl += calculateActualTvl(lastHarvest, dto.getPrices(), farmPrice);
            }
        } else {
            log.warn("Wrong prices " + dto);
        }

        HarvestTvlEntity harvestTvl = new HarvestTvlEntity();
        harvestTvl.setCalculateTime(dto.getBlockDate());
        harvestTvl.setLastTvl(tvl);
        if (dto.getAllPoolsOwnersCount() != null) {
            harvestTvl.setLastOwnersCount(dto.getAllPoolsOwnersCount());
        } else {
            log.warn("Empty AllPoolsOwnersCount " + dto.print());
        }
        if (dto.getAllOwnersCount() != null) {
            harvestTvl.setLastAllOwnersCount(dto.getAllOwnersCount());
        } else {
            log.warn("Empty AllPoolsOwnersCount " + dto.print());
        }
        harvestTvl.setCalculateHash(dto.getHash());
        harvestTvl.setLastPrice(farmPrice);

        if (uniswapDTO != null) {
            harvestTvl.setLastPrice(uniswapDTO.getLastPrice());
        }

        if (checkTheSame && harvestTvlRepository.existsById(dto.getId())) {
            log.info("Found the same (" + tvl + ") last TVL record for " + dto);
        }
        dto.setLastAllUsdTvl(tvl);
        return harvestTvl;
    }

    public BigInteger lastBlock() {
        HarvestDTO dto = harvestRepository.findFirstByOrderByBlockDesc();
        if (dto == null) {
            return new BigInteger("0");
        }
        return BigInteger.valueOf(dto.getBlock());
    }

    private double calculateActualTvl(HarvestDTO dto, String currentPrices, Double farmPrice) {
        if (currentPrices == null) {
            return dto.getLastUsdTvl();
        }
        String lpStatStr = dto.getLpStat();
        double tvl = 0.0;
        try {
            PricesModel pricesModel = objectMapper.readValue(currentPrices, PricesModel.class);
            if (lpStatStr == null) {
                double coinPrice = 0.0;
                if (("PS".equals(dto.getVault()) || "PS_V0".equals(dto.getVault())) && farmPrice != null) {
                    coinPrice = farmPrice;
                } else {
                    coinPrice = PriceProvider.readPrice(pricesModel, dto.getVault());
                }
                tvl = dto.getLastTvl() * coinPrice;
            } else {
                LpStat lpStat = objectMapper.readValue(lpStatStr, LpStat.class);

                double coin1Price;
                if (FARM_NAME.equalsIgnoreCase(lpStat.getCoin1())) {
                    coin1Price = farmPrice;
                } else {
                    coin1Price = PriceProvider.readPrice(pricesModel, lpStat.getCoin1());
                }

                double coin2Price;
                if (FARM_NAME.equalsIgnoreCase(lpStat.getCoin2())) {
                    coin2Price = farmPrice;
                } else {
                    coin2Price = PriceProvider.readPrice(pricesModel, lpStat.getCoin2());
                }

                tvl = (lpStat.getAmount1() * coin1Price) + (lpStat.getAmount2() * coin2Price);
            }
        } catch (Exception ignored) {
        }
        if (tvl == 0.0) {
            return dto.getLastUsdTvl();
        }
        if (Double.isInfinite(tvl) || Double.isNaN(tvl)) {
            throw new IllegalStateException("TVL is wrong for " + dto);
        }
        return tvl;
    }

    public List<HarvestDTO> fetchHarvest(String from, String to) {
        if (from == null && to == null) {
            return harvestRepository.fetchAllFromBlockDate(
                Instant.now().minus(1, DAYS).toEpochMilli() / 1000);
        }
        int fromI = 0;
        int toI = Integer.MAX_VALUE;
        if (from != null) {
            fromI = Integer.parseInt(from);
        }
        if (to != null) {
            toI = Integer.parseInt(to);
        }
        return harvestRepository.fetchAllByPeriod(fromI, toI);
    }

    public static double aprToApy(double apr, double period) {
        return (Math.pow(1 + (apr / period), period) - 1.0);
    }

    public void fillProfit(HarvestDTO dto) {
        if (!"Withdraw".equals(dto.getMethodName()) 
        || dto.getAmount().equals(0D) 
        || "PS".equals(dto.getVault()) 
        || "PS_V0".equals(dto.getVault())) {
            return;
        }

        // check if there is last full withdrawal in the past
        Long lastWithdrawBlockDate = harvestRepository.findLastFullWithdrawByOwnerAndVault(
            dto.getOwner(),
            dto.getVault(),
            dto.getBlockDate()
        );
        
        // find relevant transfers (from last full withdraw)
        List<HarvestDTO> transfers = harvestRepository.fetchPeriodForOwnerAndVault(
            dto.getOwner(),
            dto.getVault(),
            dto.getBlockDate(),
            lastWithdrawBlockDate != null ? lastWithdrawBlockDate : 0
            );

        dto.setProfit(calculateProfit(transfers));
        dto.setProfitUsd(calculateProfitUsd(dto));
    }

    static double calculateProfit(List<HarvestDTO> transfers) {
        double withdraws = 0.0;
        double deposits = 0.0;
        double profit = 0.0;
        for (HarvestDTO transfer : transfers) {
            //lastProfit = 0;
            if (!"Withdraw".equals(transfer.getMethodName())
                && !"Deposit".equals(transfer.getMethodName())) {
                continue;
            }

            if ("Withdraw".equals(transfer.getMethodName())) {
                withdraws += transfer.getAmount();
            }
            //count all deposits
            if ("Deposit".equals(transfer.getMethodName())) {
                deposits += transfer.getAmount();
            }

            // will not work in rare situation when holder has profit more than initial stake amount (impossible I guess)
            // todo: negative withdraws after usdc and usdt exploit
            if (withdraws >= deposits) {
                profit = withdraws - deposits;
            }
        }
        return profit;
    }

    private double calculateProfitUsd(HarvestDTO dto) {
        if (dto.getLastUsdTvl() == null 
        || dto.getLastUsdTvl().equals(0D)
        || dto.getLastTvl() == null 
        || dto.getLastTvl().equals(0D)
        || dto.getProfit().equals(0D)
        ) {
            return 0.0;
        }

        return (dto.getLastUsdTvl() / dto.getLastTvl()) * dto.getProfit();
    }
}
