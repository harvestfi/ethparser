package pro.belbix.ethparser.repositories.v0;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.entity.c_layer.UniPriceViewEntity;
import pro.belbix.ethparser.entity.v0.HarvestTvlEntity;
import pro.belbix.ethparser.entity.v0.HarvestTvlViewEntity;

public interface HarvestTvlViewRepository extends JpaRepository<HarvestTvlViewEntity, Long> {
  List<HarvestTvlEntity> findAllByNetworkOrderByCalculateTime(String network);

//  @Query(nativeQuery = true, value = "" +
//      "select " +
//      "       MAX(agg.calculate_hash) calculate_hash,  " +
//      "       MAX(agg.network) network,  " +
//      "       MAX(agg.calculate_time) calculate_time,  " +
//      "       MAX(agg.last_tvl) last_tvl,  " +
//      "       MAX(agg.last_owners_count) last_owners_count,  " +
//      "       MAX(agg.last_all_owners_count) last_all_owners_count,  " +
//      "       MAX(agg.last_price) last_price  " +
//      "from (  " +
//      "         select  " +
//      "             t.calculate_hash calculate_hash,  " +
//      "             t.network network,  " +
//      "             t.calculate_time calculate_time,  " +
//      "             t.last_tvl last_tvl,  " +
//      "             t.last_owners_count last_owners_count,  " +
//      "             t.last_all_owners_count last_all_owners_count,  " +
//      "             t.last_price last_price,  " +
//      "             to_char(date(to_timestamp(t.calculate_time)), 'YYYY-MM-DD HH') grp  " +
//      "         from harvest_tvl t  " +
//      "         where t.calculate_time between :startTime and :endTime " +
//      "              and t.network = :network " +
//      "     ) agg  " +
//      "group by agg.grp  " +
//      "order by calculate_time")

  //TODO вопрос про groupBy и order?
  @Query("select t from HarvestTvlViewEntity t where "
      + "t.calculateTime between :startTime and :endTime "
      + "and t.network = :network "
  )
  List<HarvestTvlEntity> getHistoryOfAllTvl(
      @Param("startTime") long startTime,
      @Param("endTime") long endTime,
      @Param("network") String network
  );
}
