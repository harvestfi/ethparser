package pro.belbix.ethparser.repositories.v0;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.dto.v0.RewardDTO;

public interface RewardsRepository extends JpaRepository<RewardDTO, String> {

    List<RewardDTO> getAllByOrderByBlockDate();

    RewardDTO getFirstByVaultOrderByBlockDateDesc(String vault);

    @Query("select t from RewardDTO t where "
        + "t.blockDate > :from and t.blockDate <= :to order by t.blockDate")
    List<RewardDTO> fetchAllByRange(@Param("from") long from, @Param("to") long to);

    @Query("select t from RewardDTO t where "
        + "t.vault = :vault and t.blockDate between :startDate and :endDate")
    List<RewardDTO> fetchRewardsByVaultAfterBlockDate(@Param("vault") String vault,
                                                      @Param("startDate") long startDate,
                                                      @Param("endDate") long endDate);

    @Query(nativeQuery = true, value = "" +
        "select distinct on (vault) "
        + "       last_value(id) over w            as id, "
        + "       last_value(block) over w         as block, "
        + "       last_value(block_date) over w    as block_date, "
        + "       last_value(network) over w       as network, "
        + "       vault, "
        + "       last_value(reward) over w        as reward, "
        + "       last_value(apy) over w           as apy, "
        + "       last_value(tvl) over w           as tvl, "
        + "       last_value(farm_balance) over w  as farm_balance, "
        + "       last_value(weekly_apy) over w    as weekly_apy, "
        + "       last_value(period_finish) over w as period_finish "
        + " "
        + "from rewards "
        + "    window w as (PARTITION BY vault order by block_date desc)")
    List<RewardDTO> fetchLastRewards();

    @Query("select t from RewardDTO t where t.vault = :vault and t.blockDate between :startTime and :endTime order by t.blockDate")
    List<RewardDTO> getAllByVaultOrderByBlockDate(@Param("vault") String vault,
                                                  @Param("startTime") long startTime,
                                                  @Param("endTime") long endTime);

    @Query("select t from RewardDTO t where t.blockDate between :startTime and :endTime order by t.blockDate")
    List<RewardDTO> getAllOrderByBlockDate(
        @Param("startTime") long startTime, @Param("endTime") long endTime);
}
