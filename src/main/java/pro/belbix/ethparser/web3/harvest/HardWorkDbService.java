package pro.belbix.ethparser.web3.harvest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.HardWorkDTO;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.properties.Web3Properties;
import pro.belbix.ethparser.repositories.HardWorkRepository;
import pro.belbix.ethparser.repositories.HarvestRepository;

@Service
public class HardWorkDbService {

    private static final Logger log = LoggerFactory.getLogger(HardWorkDbService.class);
    private final HardWorkRepository hardWorkRepository;
    private final HarvestRepository harvestRepository;
    private final Web3Properties web3Properties;

    public HardWorkDbService(HardWorkRepository hardWorkRepository,
                             HarvestRepository harvestRepository,
                             Web3Properties web3Properties) {
        this.hardWorkRepository = hardWorkRepository;
        this.harvestRepository = harvestRepository;
        this.web3Properties = web3Properties;
    }

    public boolean save(HardWorkDTO dto) {
        if (!web3Properties.isOverrideDuplicates() && hardWorkRepository.existsById(dto.getId())) {
            log.info("Duplicate HardWork entry " + dto.getId());
            return false;
        }
        hardWorkRepository.save(dto);
        hardWorkRepository.flush();
        saveTotalProfit(dto);
        return true;
    }

    private void saveTotalProfit(HardWorkDTO dto) {
        Double all = hardWorkRepository.getSumForVault(dto.getVault(), dto.getBlockDate());
        if (all == null) {
            all = 0.0;
        }
        dto.setShareUsdTotal(all);

        HarvestDTO harvestDTO = harvestRepository.fetchLastByVaultAndDate(dto.getVault(), dto.getBlockDate());
        if (harvestDTO != null) {
            dto.setTvl(harvestDTO.getLastUsdTvl());
        } else {
            log.warn("Not found harvest for " + dto.print());
        }

        hardWorkRepository.save(dto);
    }
}
