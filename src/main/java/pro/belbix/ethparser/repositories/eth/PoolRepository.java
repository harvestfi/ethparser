package pro.belbix.ethparser.repositories.eth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.entity.eth.ContractEntity;
import pro.belbix.ethparser.entity.eth.PoolEntity;

public interface PoolRepository extends JpaRepository<PoolEntity, Integer> {

    @Query("select t from PoolEntity t "
        + "left join fetch t.contract f1 "
        + "left join fetch f1.type f1f "
        + "left join fetch t.controller f2 "
        + "left join fetch f2.type f2f "
        + "left join fetch t.governance f3 "
        + "left join fetch f3.type f3f "
        + "left join fetch t.owner f4 "
        + "left join fetch f4.type f4f "
        + "left join fetch t.lpToken f5 "
        + "left join fetch f5.type f5f "
        + "left join fetch t.rewardToken f6 "
        + "left join fetch f6.type f6f "
        + "where t.contract = :contract")
    PoolEntity findFirstByContract(@Param("contract") ContractEntity address);

}
