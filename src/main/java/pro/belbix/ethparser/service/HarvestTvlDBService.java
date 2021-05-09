package pro.belbix.ethparser.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.model.TvlHistory;
import pro.belbix.ethparser.repositories.v0.HarvestRepository;

@Service
@Log4j2
public class HarvestTvlDBService {

    private final HarvestRepository harvestRepository;

    public HarvestTvlDBService(HarvestRepository harvestRepository) {
        this.harvestRepository = harvestRepository;
    }

    //todo move calculation on the frontend
    public List<TvlHistory> fetchTvlByVault(
        String address, long startTime, long endTime, String network
    ) {
        log.debug("get tvl for " + address);
        List<HarvestDTO> harvestTxEntities = harvestRepository
            .findAllByVaultOrderByBlockDate(address, startTime, endTime, network);
        List<TvlHistory> tvlHistoryDTOS = new ArrayList<>();
        if (harvestTxEntities == null) {
            return tvlHistoryDTOS;
        }
        Instant lastDate = null;
        for (HarvestDTO harvestTxEntity : harvestTxEntities) {
            try {
                Instant date = Instant.ofEpochSecond(harvestTxEntity.getBlockDate());
                if (lastDate != null && Duration.between(lastDate, date).getSeconds() < 60 * 60) {
                    continue;
                }

                TvlHistory tvlHistoryDTO = new TvlHistory();
                tvlHistoryDTO.setCalculateTime(harvestTxEntity.getBlockDate());
                tvlHistoryDTO.setLastTvl(harvestTxEntity.getLastUsdTvl());
                tvlHistoryDTO.setLastTvlNative(harvestTxEntity.getLastTvl());
                tvlHistoryDTO.setSharePrice(harvestTxEntity.getSharePrice());
                tvlHistoryDTO.setLastOwnersCount(harvestTxEntity.getOwnerCount());

                lastDate = date;

                tvlHistoryDTOS.add(tvlHistoryDTO);
            } catch (Exception e) {
                log.error("Error convert " + harvestTxEntity, e);
                break;
            }
        }
        return tvlHistoryDTOS;
    }

}
