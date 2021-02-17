package pro.belbix.ethparser.web3.harvest.db;

import static java.time.temporal.ChronoUnit.DAYS;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.PARSABLE_UNI_PAIRS;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.entity.HarvestTvlEntity;
import pro.belbix.ethparser.model.LpStat;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.HarvestRepository;
import pro.belbix.ethparser.repositories.HarvestTvlRepository;
import pro.belbix.ethparser.repositories.UniswapRepository;
import pro.belbix.ethparser.web3.contracts.ContractUtils;

@Service
@Log4j2
public class HarvestDBService {

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

    public static double aprToApy(double apr, double period) {
        return (Math.pow(1 + (apr / period), period) - 1.0);
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
            dto.getVault() + "_V0", dto.getBlockDate());
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
            ContractUtils.getAllVaultNames().stream()
                .filter(v -> !ContractUtils.isPsName(v))
                .collect(Collectors.toList()),
            dto.getBlockDate());
        if (allPoolsOwnerCount == null) {
            allPoolsOwnerCount = 0;
        }
        dto.setAllPoolsOwnersCount(allPoolsOwnerCount);
    }

    public HarvestTvlEntity calculateHarvestTvl(HarvestDTO dto, boolean checkTheSame) {
        if (checkTheSame && harvestTvlRepository.existsById(dto.getId())) {
            log.warn("Found the same harvestTvl record for " + dto);
        }
        HarvestTvlEntity harvestTvl = new HarvestTvlEntity();
        harvestTvl.setCalculateHash(dto.getId());
        harvestTvl.setCalculateTime(dto.getBlockDate());

        fillLastFarmPrice(dto, harvestTvl);
        fillSimpleDataFromDto(dto, harvestTvl);
        //should be after price filling
        fillTvl(dto, harvestTvl);
        return harvestTvl;
    }

    public void fillSimpleDataFromDto(HarvestDTO dto, HarvestTvlEntity harvestTvl) {
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
    }

    public void fillLastFarmPrice(HarvestDTO dto, HarvestTvlEntity harvestTvl) {
        UniswapDTO uniswapDTO = uniswapRepository
            .findFirstByBlockDateBeforeAndCoinOrderByBlockDesc(
                dto.getBlockDate(), "FARM");
        if (uniswapDTO != null) {
            harvestTvl.setLastPrice(uniswapDTO.getLastPrice());
        } else {
            harvestTvl.setLastPrice(0.0);
        }
    }

    public void fillTvl(HarvestDTO dto, HarvestTvlEntity harvestTvl) {
        double tvl = 0.0;

        List<String> contracts = new ArrayList<>(ContractUtils.getAllVaultNames());
        PARSABLE_UNI_PAIRS.stream()
            .map(c -> ContractUtils.getNameByAddress(c)
                .orElseThrow(() -> new IllegalStateException("Not found name for " + c)))
            .forEach(contracts::add);

        for (String vaultName : contracts) {
            HarvestDTO lastHarvest = harvestRepository.fetchLastByVaultAndDate(vaultName, dto.getBlockDate());
            if (lastHarvest == null) {
                continue;
            }
            if (lastHarvest.getId().equalsIgnoreCase(dto.getId())) {
                lastHarvest = dto; // for avoiding JPA wrong synchronisation
            }
            tvl += calculateActualTvl(lastHarvest, harvestTvl.getLastPrice());
        }

        harvestTvl.setLastTvl(tvl);
    }

    private double calculateActualTvl(HarvestDTO dto, Double farmPrice) {
        String lpStatStr = dto.getLpStat();
        double tvl = 0.0;
        try {
            if (lpStatStr == null) {
                double coinPrice = 0.0;
                if (("PS".equals(dto.getVault()) || "PS_V0".equals(dto.getVault())) && farmPrice != null) {
                    coinPrice = farmPrice;
                } else {
                    coinPrice = dto.getUnderlyingPrice();
                }
                tvl = dto.getLastTvl() * coinPrice;
            } else {
                LpStat lpStat = objectMapper.readValue(lpStatStr, LpStat.class);

                double coin1Price;
                if ("FARM".equalsIgnoreCase(lpStat.getCoin1())) {
                    coin1Price = farmPrice;
                } else {
                    coin1Price = lpStat.getPrice1();
                }

                double coin2Price;
                if ("FARM".equalsIgnoreCase(lpStat.getCoin2())) {
                    coin2Price = farmPrice;
                } else {
                    coin2Price = lpStat.getPrice2();
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

    public BigInteger lastBlock() {
        HarvestDTO dto = harvestRepository.findFirstByOrderByBlockDesc();
        if (dto == null) {
            return new BigInteger("0");
        }
        return BigInteger.valueOf(dto.getBlock());
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

    public void fillProfit(HarvestDTO dto) {
        if (!"Withdraw".equals(dto.getMethodName())
            || dto.getOwnerBalance() == null
            || dto.getOwnerBalance() != 0.0
            || dto.getAmount() == null
            || dto.getAmount() == 0.0
            || "PS".equals(dto.getVault())
            || "PS_V0".equals(dto.getVault())) {
            return;
        }

        // find relevant transfers (from last full withdraw)
        List<HarvestDTO> transfers = harvestRepository.fetchLatestSinceLastWithdraw(
            dto.getOwner(),
            dto.getVault(),
            dto.getBlockDate());

        // for new transaction DB can still not write the object at this moment
        if (transfers.stream().noneMatch(h -> h.getId().equalsIgnoreCase(dto.getId()))) {
            transfers.add(dto);
        }

        dto.setProfit(calculateProfit(transfers));
        dto.setProfitUsd(calculateProfitUsd(dto));
    }

    static double calculateProfit(List<HarvestDTO> transfers) {
        double withdraws = 0.0;
        double deposits = 0.0;
        double profit = 0.0;
        for (HarvestDTO transfer : transfers) {
            if ((!"Withdraw".equals(transfer.getMethodName())
                && !"Deposit".equals(transfer.getMethodName()))
                || transfer.getAmount() == null
                || transfer.getAmount() == 0.0
            ) {
                continue;
            }

            Double sharePrice = transfer.getSharePrice();
            if (transfer.getSharePrice() == null
                || transfer.getSharePrice() == 0.0) {
                sharePrice = 1.0;
            }

            //count all withdraws
            if ("Withdraw".equals(transfer.getMethodName())) {
                withdraws += transfer.getAmount() * sharePrice;
            }
            //count all deposits
            if ("Deposit".equals(transfer.getMethodName())) {
                deposits += transfer.getAmount() * sharePrice;
            }

            // will not work in rare situation when holder has profit more than initial stake amount (impossible I guess)
            if (transfer.getOwnerBalance() == 0) {
                profit = withdraws - deposits;
                deposits = 0;
                withdraws = 0;
            }
        }
        return profit;
    }

    static double calculateProfitUsd(HarvestDTO dto) {
        if (dto.getLastUsdTvl() == null
            || dto.getLastUsdTvl() == 0.0
            || dto.getLastTvl() == null
            || dto.getLastTvl() == 0.0
            || dto.getProfit() == null
            || dto.getProfit() == 0.0
            || Double.isNaN(dto.getProfit())
            || Double.isInfinite(dto.getProfit())
        ) {
            return 0.0;
        }

        return (dto.getLastUsdTvl() / dto.getLastTvl()) * dto.getProfit();
    }
}
