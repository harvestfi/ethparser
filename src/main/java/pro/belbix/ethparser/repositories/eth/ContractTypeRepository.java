package pro.belbix.ethparser.repositories.eth;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.eth.ContractTypeEntity;

public interface ContractTypeRepository extends JpaRepository<ContractTypeEntity, Integer> {

}
