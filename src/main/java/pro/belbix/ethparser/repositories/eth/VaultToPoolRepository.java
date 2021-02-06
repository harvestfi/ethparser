package pro.belbix.ethparser.repositories.eth;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.eth.PoolEntity;
import pro.belbix.ethparser.entity.eth.VaultEntity;
import pro.belbix.ethparser.entity.eth.VaultToPoolEntity;

public interface VaultToPoolRepository extends JpaRepository<VaultToPoolEntity, Integer> {

    VaultToPoolEntity findFirstByVaultAndPool(VaultEntity vaultEntity, PoolEntity poolEntity);

    VaultToPoolEntity findFirstByVault(VaultEntity vaultEntity);

    VaultToPoolEntity findFirstByPool(PoolEntity poolEntity);
}
