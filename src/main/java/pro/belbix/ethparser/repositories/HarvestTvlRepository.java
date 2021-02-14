package pro.belbix.ethparser.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.entity.HarvestTvlEntity;

public interface HarvestTvlRepository extends JpaRepository<HarvestTvlEntity, String> {

    HarvestTvlEntity findFirstByCalculateTimeAndLastTvl(long time, double lastTvl);

    List<HarvestTvlEntity> findAllByOrderByCalculateTime();

    @Query(nativeQuery = true, value = "" +
        "select " +
        "       MAX(agg.calculate_hash) calculate_hash,  " +
        "       MAX(agg.calculate_time) calculate_time,  " +
        "       MAX(agg.last_tvl) last_tvl,  " +
        "       MAX(agg.last_owners_count) last_owners_count,  " +
        "       MAX(agg.last_all_owners_count) last_all_owners_count,  " +
        "       MAX(agg.last_price) last_price  " +
        "from (  " +
        "         select  " +
        "             t.calculate_hash calculate_hash,  " +
        "             t.calculate_time calculate_time,  " +
        "             t.last_tvl last_tvl,  " +
        "             t.last_owners_count last_owners_count,  " +
        "             t.last_all_owners_count last_all_owners_count,  " +
        "             t.last_price last_price,  " +
        "             date_format(from_unixtime(t.calculate_time), '%Y-%m-%d %H' ) grp  " +
        "         from harvest_tvl t  " +
        "         where t.calculate_time between :startTime and :endTime" +
        "     ) agg  " +
        "group by agg.grp  " +
        "order by calculate_time")
    List<HarvestTvlEntity> getHistoryOfAllTvl(@Param("startTime") long startTime, @Param("endTime") long endTime);

}
