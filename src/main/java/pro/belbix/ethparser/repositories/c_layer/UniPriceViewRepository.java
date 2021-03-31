package pro.belbix.ethparser.repositories.c_layer;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.entity.c_layer.UniPriceViewEntity;

public interface UniPriceViewRepository extends JpaRepository<UniPriceViewEntity, Long> {

  @Query("select t from UniPriceViewEntity t where "
      + "t.address in :addresses "
      + "and t.blockNumber between :startBlock and :endBlock"
  )
  List<UniPriceViewEntity> findByAddressesAndLogNames(
      @Param("addresses") List<String> addresses,
      @Param("startBlock") long startBlock,
      @Param("endBlock") long endBlock,
      Pageable pageable
  );

}
