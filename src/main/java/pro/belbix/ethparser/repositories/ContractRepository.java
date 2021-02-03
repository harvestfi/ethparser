package pro.belbix.ethparser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.eth.ContractEntity;

public interface ContractRepository extends JpaRepository<ContractEntity, Integer> {

    ContractEntity findFirstByAddress(String address);

}
