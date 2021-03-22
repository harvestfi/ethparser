package pro.belbix.ethparser.repositories.eth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.entity.contracts.PoolEntity;
import pro.belbix.ethparser.entity.contracts.VaultEntity;
import pro.belbix.ethparser.entity.contracts.VaultToPoolEntity;

public interface VaultToPoolRepository extends JpaRepository<VaultToPoolEntity, Integer> {

    @Query("select t from VaultToPoolEntity t "
        + "left join fetch t.vault f1 "
        + "left join fetch f1.contract f1f1 "
        + "left join fetch f1.underlying f1f2 "
        + "left join fetch f1.strategy f1f3 "
        + "left join fetch f1.controller f1f4 "
        + "left join fetch f1.governance f1f5 "
        + "left join fetch t.pool f2 "
        + "left join fetch f2.contract f2f1 "
        + "left join fetch f2.controller f2f2 "
        + "left join fetch f2.governance f2f3 "
        + "left join fetch f2.owner f2f4 "
        + "left join fetch f2.lpToken f2f5 "
        + "left join fetch f2.rewardToken f2f6 "
        + "where t.vault = :vault and t.pool = :pool"
    )
    VaultToPoolEntity findFirstByVaultAndPool(
        @Param("vault") VaultEntity vaultEntity,
        @Param("pool") PoolEntity poolEntity);

    @Query("select t from VaultToPoolEntity t "
        + "left join fetch t.vault f1 "
        + "left join fetch t.pool f2 "
        + "where t.vault = :vault order by t.blockStart desc"
    )
    VaultToPoolEntity findFirstByVault(@Param("vault") VaultEntity vaultEntity);

    @Query("select t from VaultToPoolEntity t "
        + "left join fetch t.vault f1 "
        + "left join fetch t.pool f2 "
        + "where t.pool = :pool order by t.blockStart desc"
    )
    VaultToPoolEntity findFirstByPool(@Param("pool") PoolEntity poolEntity);
}
