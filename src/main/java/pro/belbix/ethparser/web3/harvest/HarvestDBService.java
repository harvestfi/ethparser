package pro.belbix.ethparser.web3.harvest;

import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.entity.HarvestTvlEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.HarvestRepository;
import pro.belbix.ethparser.repositories.HarvestTvlRepository;
import pro.belbix.ethparser.repositories.UniswapRepository;

@Service
public class HarvestDBService {

    private static final Logger log = LoggerFactory.getLogger(HarvestDBService.class);
    private final HarvestRepository harvestRepository;
    private final AppProperties appProperties;
    private final HarvestTvlRepository harvestTvlRepository;
    private final UniswapRepository uniswapRepository;

    public HarvestDBService(HarvestRepository harvestRepository, AppProperties appProperties,
                            HarvestTvlRepository harvestTvlRepository, UniswapRepository uniswapRepository) {
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

        Integer ownerCount = harvestRepository.fetchActualOwnerCount(dto.getVault(),
            Vaults.vaultNameToOldVaultName.get(dto.getVault()), dto.getBlockDate());
        if (ownerCount == null) {
            ownerCount = 0;
        }
        dto.setOwnerCount(ownerCount);

        harvestRepository.save(dto);
        harvestRepository.flush();
        saveHarvestTvl(dto, true);
        harvestRepository.save(dto);
        return true;
    }

    public void saveHarvestTvl(HarvestDTO dto, boolean checkTheSame) {
        double tvl = 0.0;
        int owners = 0;
        for (String vaultName : Vaults.vaultNames.values()) {
            HarvestDTO lastHarvest = harvestRepository.fetchLastByVaultAndDate(vaultName, dto.getBlockDate());
            if (lastHarvest == null) {
                continue;
            }
            tvl += lastHarvest.getLastUsdTvl();
            owners += lastHarvest.getOwnerCount();
        }

        HarvestTvlEntity newTvl = new HarvestTvlEntity();
        newTvl.setCalculateTime(dto.getBlockDate());
        newTvl.setLastTvl(tvl);
        newTvl.setLastOwnersCount(owners);
        newTvl.setCalculateHash(dto.getHash());

        UniswapDTO uniswapDTO = uniswapRepository.findFirstByBlockDateBeforeOrderByBlockDesc(dto.getBlockDate());
        if (uniswapDTO != null) {
            newTvl.setLastPrice(uniswapDTO.getLastPrice());
        }

        if (checkTheSame && harvestTvlRepository.existsById(dto.getId())) {
            log.info("Found the same (" + tvl + ") last TVL record for " + dto);
        }
        dto.setLastAllUsdTvl(tvl);
        harvestTvlRepository.save(newTvl);
    }

    public BigInteger lastBlock() {
        HarvestDTO dto = harvestRepository.findFirstByOrderByBlockDesc();
        if (dto == null) {
            return new BigInteger("0");
        }
        return dto.getBlock();
    }

    public static double aprToApy(double apr, double period){
        return (Math.pow(1 + (apr / period), period) - 1.0);
    }

}
