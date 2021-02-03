package pro.belbix.ethparser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.eth.ContractEntity;
import pro.belbix.ethparser.entity.eth.VaultEntity;

public interface VaultRepository extends JpaRepository<VaultEntity, Integer> {

    VaultEntity findFirstByAddress(ContractEntity vaultContract);
}
