package pro.belbix.ethparser.repositories.eth;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.entity.contracts.TokenEntity;

public interface TokenRepository extends JpaRepository<TokenEntity, Integer> {

    @Query("select t from TokenEntity t "
        + "left join fetch t.contract f1 "
        + "where lower(f1.address) = lower(:address) and f1.network = :network")
    TokenEntity findFirstByAddress(
        @Param("address") String address,
        @Param("network") String network
    );

    @Query("select t from TokenEntity t "
        + "left join fetch t.contract f1 "
        + "where f1.name = :name and f1.network = :network")
    TokenEntity findFirstByName(
        @Param("name") String name,
        @Param("network") String network
    );

    @Query("select t from TokenEntity t "
        + "left join fetch t.contract f1 "
        + "where f1.network = :network")
    List<TokenEntity> fetchAllByNetwork(@Param("network") String network);

    @Query("select max(t.id) from TokenEntity t")
    int findMaxId();
}
