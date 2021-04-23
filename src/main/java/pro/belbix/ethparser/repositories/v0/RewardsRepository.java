package pro.belbix.ethparser.repositories.v0;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.dto.v0.RewardDTO;

public interface RewardsRepository extends JpaRepository<RewardDTO, String> {

    RewardDTO getFirstByVaultAndNetworkOrderByBlockDateDesc(String vault, String network);

    @Query("select t from RewardDTO t where "
        + "t.blockDate between :from and :to "
        + "and t.network = :network "
        + "order by t.blockDate")
    List<RewardDTO> fetchAllByRange(
        @Param("from") long from,
        @Param("to") long to,
        @Param("network") String network
    );

    @Query("select t from RewardDTO t where "
        + "t.vault = :vault "
        + "and t.blockDate between :startDate and :endDate "
        + "and t.network = :network")
    List<RewardDTO> fetchRewardsByVaultAfterBlockDate(
        @Param("vault") String vault,
        @Param("startDate") long startDate,
        @Param("endDate") long endDate,
        @Param("network") String network
    );

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
        + "       last_value(period_finish) over w as period_finish, "
        + "       last_value(is_weekly_reward) over w as is_weekly_reward "
        + " "
        + "from rewards where network = :network "
        + "    window w as (PARTITION BY vault order by block_date desc)")
    List<RewardDTO> fetchLastRewards(@Param("network") String network);

    @Query("select t from RewardDTO t where "
        + "t.vault = :vault "
        + "and t.blockDate between :startTime and :endTime "
        + "and t.network = :network "
        + "order by t.blockDate")
    List<RewardDTO> getAllByVaultOrderByBlockDate(
        @Param("vault") String vault,
        @Param("startTime") long startTime,
        @Param("endTime") long endTime,
        @Param("network") String network
    );

    @Query("select t from RewardDTO t where "
        + "t.blockDate between :startTime and :endTime "
        + "and t.network = :network "
        + "order by t.blockDate")
    List<RewardDTO> getAllOrderByBlockDate(
        @Param("startTime") long startTime,
        @Param("endTime") long endTime,
        @Param("network") String network
    );
}
