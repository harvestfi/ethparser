package pro.belbix.ethparser.repositories.v0;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.v0.BlockCacheEntity;

public interface BlockCacheRepository extends JpaRepository<BlockCacheEntity, Long> {

    BlockCacheEntity findFirstByOrderByBlockDateDesc();

}
