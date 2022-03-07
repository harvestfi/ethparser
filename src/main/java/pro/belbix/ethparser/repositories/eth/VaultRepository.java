package pro.belbix.ethparser.repositories.eth;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.entity.contracts.VaultEntity;

public interface VaultRepository extends JpaRepository<VaultEntity, Integer> {

    @Query("select t from VaultEntity t "
        + "left join fetch t.contract f1 "
        + "left join fetch t.controller f2 "
        + "left join fetch t.governance f3 "
        + "left join fetch t.strategy f4 "
        + "left join fetch t.underlying f5 "
        + "where lower(f1.address) = lower(:vaultAdr) and f1.network = :network")
    VaultEntity findFirstByContract(
        @Param("vaultAdr") String vaultAdr,
        @Param("network") String network
    );

    @Query("select t from VaultEntity t "
        + "left join fetch t.contract f1 "
        + "left join fetch t.controller f2 "
        + "left join fetch t.governance f3 "
        + "left join fetch t.strategy f4 "
        + "left join fetch t.underlying f5 "
        + "where f1.network = :network")
    List<VaultEntity> fetchAllByNetwork(@Param("network") String network);

    @Query("select t from VaultEntity t "
        + "left join fetch t.contract f1 "
        + "where f1.network = 'eth' AND f1.name like 'V_UNI%' OR f1.name like  'V_SUSHI%'")
    List<VaultEntity> findAllOnlyUniAndSushi();
}
