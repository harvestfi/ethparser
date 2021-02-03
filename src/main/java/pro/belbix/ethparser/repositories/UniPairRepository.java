package pro.belbix.ethparser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.eth.ContractEntity;
import pro.belbix.ethparser.entity.eth.UniPairEntity;

public interface UniPairRepository extends JpaRepository<UniPairEntity, Integer> {

    UniPairEntity findFirstByAddress(ContractEntity poolContract);
}
