package pro.belbix.ethparser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.BlockCacheEntity;

public interface BlockCacheRepository extends JpaRepository<BlockCacheEntity, Long> {

    BlockCacheEntity findFirstByOrderByBlockDateDesc();

}
