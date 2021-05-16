package pro.belbix.ethparser.repositories.eth;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.entity.contracts.UniPairEntity;

public interface UniPairRepository extends JpaRepository<UniPairEntity, Integer> {

    @Query("select t from UniPairEntity t "
        + "left join fetch t.contract f1 "
        + "left join fetch t.token0 f2 "
        + "left join fetch t.token1 f3 "
        + "where f1.address = :lpAddress and f1.network = :network")
    UniPairEntity findFirstByAddress(
        @Param("lpAddress") String lpAddress,
        @Param("network") String network
    );

    @Query("select t from UniPairEntity t "
        + "left join fetch t.contract f1 "
        + "left join fetch t.token0 f2 "
        + "left join fetch t.token1 f3 "
        + "where f1.name = :name and f1.network = :network")
    UniPairEntity findFirstByName(
        @Param("name") String name,
        @Param("network") String network
    );

    @Query("select t from UniPairEntity t "
        + "left join fetch t.contract f1 "
        + "left join fetch t.token0 f2 "
        + "left join fetch t.token1 f3 "
        + "where f1.network = :network")
    List<UniPairEntity> fetchAllByNetwork(@Param("network") String network);

    @Query("select t from UniPairEntity t "
        + "left join fetch t.contract f1 "
        + "left join fetch t.token0 f2 "
        + "left join fetch t.token1 f3 "
        + "where f1.network = :network "
        + "and ((t.token0.address = :token0 and t.token1.address = :token1)"
        + "     or (t.token1.address = :token0 and t.token0.address = :token1))")
    List<UniPairEntity> findLpsForTokenPair(
        @Param("token0") String token0,
        @Param("token1") String token1,
        @Param("network") String network
    );


}
