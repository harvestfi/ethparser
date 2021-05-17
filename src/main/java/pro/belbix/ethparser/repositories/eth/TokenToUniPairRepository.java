package pro.belbix.ethparser.repositories.eth;

import java.util.List;
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
        + "left join fetch f2.token1 f2f3 "
        + "left join fetch f2.token0 f2f4 "
        + "where f1f1.address = :tokenAdr "
        + "and f1f1.network = :network "
        + "and f2f1.address = :uniPairAdr "
        + "and f2f1.network = :network"
    )
    TokenToUniPairEntity findFirstByTokenAndUniPair(
        @Param("tokenAdr") String tokenAdr,
        @Param("uniPairAdr") String uniPairAdr,
        @Param("network") String network
    );

    @Query("select t from TokenToUniPairEntity t "
        + "left join fetch t.token f1 "
        + "left join fetch f1.contract f1f1 "
        + "left join fetch t.uniPair f2 "
        + "left join fetch f2.contract f2f1 "
        + "left join fetch f2.token1 f2f3 "
        + "left join fetch f2.token0 f2f4 "
        + "where f1f1.address = :tokenAdr and f1f1.network = :network "
        + "order by t.blockStart desc"
    )
    List<TokenToUniPairEntity> findByToken(
        @Param("tokenAdr") String tokenAdr,
        @Param("network") String network
    );

    @Query("select t from TokenToUniPairEntity t "
        + "left join fetch t.token f1 "
        + "left join fetch f1.contract f1f1 "
        + "left join fetch t.uniPair f2 "
        + "left join fetch f2.contract f2f1 "
        + "left join fetch f2.token1 f2f3 "
        + "left join fetch f2.token0 f2f4 "
        + "where f2f1.address = :uniPairAdr and f2f1.network = :network "
        + "order by t.blockStart desc"
    )
    List<TokenToUniPairEntity> findByUniPair(
        @Param("uniPairAdr") String uniPairAdr,
        @Param("network") String network
    );

}
