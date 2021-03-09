package pro.belbix.ethparser.repositories.b_layer;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.b_layer.ContractLogEntity;

public interface ContractLogRepository extends JpaRepository<ContractLogEntity, Long> {

}
