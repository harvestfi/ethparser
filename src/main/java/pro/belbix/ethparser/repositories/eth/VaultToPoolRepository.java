package pro.belbix.ethparser.repositories.eth;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.contracts.PoolEntity;
import pro.belbix.ethparser.entity.contracts.VaultEntity;
import pro.belbix.ethparser.entity.contracts.VaultToPoolEntity;

public interface VaultToPoolRepository extends JpaRepository<VaultToPoolEntity, Integer> {

    VaultToPoolEntity findFirstByVaultAndPool(VaultEntity vaultEntity, PoolEntity poolEntity);

    VaultToPoolEntity findFirstByVault(VaultEntity vaultEntity);

    VaultToPoolEntity findFirstByPool(PoolEntity poolEntity);
}
