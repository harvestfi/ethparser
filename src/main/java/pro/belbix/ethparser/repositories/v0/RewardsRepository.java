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
        "select max(id)                                                              id, " +
        "       max(block)                                                                   block, " +
        "       max(block_date)                                                      block_date, " +
        "       vault                                                                vault, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', reward)), '_', -1)     reward, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', apy)), '_', -1)     apy, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', tvl)), '_', -1)     tvl, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', farm_balance)), '_', -1)     farm_balance, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', weekly_apy)), '_', -1)     weekly_apy, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', period_finish)), '_', -1)     period_finish " +
        " " +
        "from rewards " +
        "group by vault")
    List<RewardDTO> fetchLastRewards();

    @Query("select t from RewardDTO t where t.vault = :vault and t.blockDate between :startTime and :endTime order by t.blockDate")
    List<RewardDTO> getAllByVaultOrderByBlockDate(@Param("vault") String vault,
                                                  @Param("startTime") long startTime,
                                                  @Param("endTime") long endTime);
}
