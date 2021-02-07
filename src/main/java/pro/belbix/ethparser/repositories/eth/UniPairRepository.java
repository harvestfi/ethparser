package pro.belbix.ethparser.repositories.eth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.entity.eth.ContractEntity;
import pro.belbix.ethparser.entity.eth.UniPairEntity;

public interface UniPairRepository extends JpaRepository<UniPairEntity, Integer> {

    @Query("select t from UniPairEntity t "
        + "left join fetch t.contract f1 "
        + "left join fetch f1.type f1f "
        + "left join fetch t.token0 f2 "
        + "left join fetch f2.type f2f "
        + "left join fetch t.token1 f3 "
        + "left join fetch f3.type f3f "
        + "where t.contract = :contract")
    UniPairEntity findFirstByContract(@Param("contract") ContractEntity poolContract);
}
