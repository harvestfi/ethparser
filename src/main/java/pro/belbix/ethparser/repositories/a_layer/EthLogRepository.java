package pro.belbix.ethparser.repositories.a_layer;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.a_layer.EthLogEntity;

public interface EthLogRepository extends JpaRepository<EthLogEntity, Long> {

}
