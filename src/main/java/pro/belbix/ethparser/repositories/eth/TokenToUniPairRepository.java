package pro.belbix.ethparser.repositories.eth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.entity.contracts.TokenEntity;
import pro.belbix.ethparser.entity.contracts.TokenToUniPairEntity;
import pro.belbix.ethparser.entity.contracts.UniPairEntity;

public interface TokenToUniPairRepository extends JpaRepository<TokenToUniPairEntity, Integer> {

    @Query("select t from TokenToUniPairEntity t "
        + "left join fetch t.token f1 "
        + "left join fetch f1.contract f1f1 "
        + "left join fetch t.uniPair f2 "
        + "left join fetch f2.contract f2f1 "
        + "left join fetch f2.keyToken f2f2 "
        + "left join fetch f2.token1 f2f3 "
        + "left join fetch f2.token0 f2f4 "
        + "where t.token = :token and t.uniPair = :uniPair"
    )
    TokenToUniPairEntity findFirstByTokenAndUniPair(
        @Param("token") TokenEntity token,
        @Param("uniPair") UniPairEntity uniPair);

    @Query("select t from TokenToUniPairEntity t "
        + "left join fetch t.token f1 "
        + "left join fetch f1.contract f1f1 "
        + "left join fetch t.uniPair f2 "
        + "left join fetch f2.contract f2f1 "
        + "left join fetch f2.keyToken f2f2 "
        + "left join fetch f2.token1 f2f3 "
        + "left join fetch f2.token0 f2f4 "
        + "where t.token = :token order by t.blockStart desc"
    )
    TokenToUniPairEntity findFirstByToken(@Param("token") TokenEntity token);

    @Query("select t from TokenToUniPairEntity t "
        + "left join fetch t.token f1 "
        + "left join fetch f1.contract f1f1 "
        + "left join fetch t.uniPair f2 "
        + "left join fetch f2.contract f2f1 "
        + "left join fetch f2.keyToken f2f2 "
        + "left join fetch f2.token1 f2f3 "
        + "left join fetch f2.token0 f2f4 "
        + "where t.uniPair = :uniPair order by t.blockStart desc"
    )
    TokenToUniPairEntity findFirstByUniPair(@Param("uniPair") UniPairEntity uniPair);

}
