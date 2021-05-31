package pro.belbix.ethparser.repositories.eth;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.entity.contracts.PoolEntity;
import pro.belbix.ethparser.entity.contracts.StrategyEntity;

public interface StrategyRepository extends JpaRepository<StrategyEntity, Integer> {

  @Query("select t from StrategyEntity t "
      + "left join fetch t.contract f1 "
      + "where f1.address = :address and f1.network = :network")
  StrategyEntity findFirstByAddress(
      @Param("address") String address,
      @Param("network") String network
  );

  @Query("select t from StrategyEntity t "
      + "left join fetch t.contract f1 "
      + "where f1.network = :network order by f1.created desc")
  List<StrategyEntity> fetchAllByNetwork(@Param("network") String network);

}
