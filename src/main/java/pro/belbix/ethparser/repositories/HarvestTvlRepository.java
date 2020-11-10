package pro.belbix.ethparser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.HarvestTvlEntity;

public interface HarvestTvlRepository extends JpaRepository<HarvestTvlEntity, String> {

    HarvestTvlEntity findFirstByCalculateTimeAndLastTvl(long time, double lastTvl);

}
