package pro.belbix.ethparser.repositories.eth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.entity.contracts.TokenEntity;

public interface TokenRepository extends JpaRepository<TokenEntity, Integer> {

    @Query("select t from TokenEntity t "
        + "left join fetch t.contract f1 "
        + "where t.contract = :contract")
    TokenEntity findFirstByContract(@Param("contract") ContractEntity tokenContract);
}
