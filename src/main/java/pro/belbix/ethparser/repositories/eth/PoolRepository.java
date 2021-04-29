package pro.belbix.ethparser.repositories.eth;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.entity.contracts.PoolEntity;

public interface PoolRepository extends JpaRepository<PoolEntity, Integer> {

    @Query("select t from PoolEntity t "
        + "left join fetch t.contract f1 "
        + "left join fetch t.controller f2 "
        + "left join fetch t.governance f3 "
        + "left join fetch t.owner f4 "
        + "left join fetch t.lpToken f5 "
        + "left join fetch t.rewardToken f6 "
        + "where f1.address = :address and f1.network = :network")
    PoolEntity findFirstByAddress(
        @Param("address") String address,
        @Param("network") String network
    );

    @Query("select t from PoolEntity t "
        + "left join fetch t.contract f1 "
        + "left join fetch t.controller f2 "
        + "left join fetch t.governance f3 "
        + "left join fetch t.owner f4 "
        + "left join fetch t.lpToken f5 "
        + "left join fetch t.rewardToken f6 "
        + "where f1.network = :network order by f1.created desc")
    List<PoolEntity> fetchAllByNetwork(@Param("network") String network);

}
