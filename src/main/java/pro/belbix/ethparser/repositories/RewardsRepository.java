package pro.belbix.ethparser.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.dto.RewardDTO;

public interface RewardsRepository extends JpaRepository<RewardDTO, String> {

    List<RewardDTO> getAllByOrderByBlockDate();

    RewardDTO getFirstByVaultOrderByBlockDateDesc(String vault);

    @Query("select t from RewardDTO t where t.vault = :vault and t.blockDate > :date")
    List<RewardDTO> fetchRewardsByVaultAfterBlockDate(@Param("vault") String vault, @Param("date") long date);


    @Query(nativeQuery = true, value = "" +
        "select max(id)                                                              id, " +
        "       null                                                                 block, " +
        "       max(block_date)                                                      block_date, " +
        "       vault                                                                vault, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', reward)), '_', -1)     reward, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', period_finish)), '_', -1)     period_finish " +
        " " +
        "from rewards " +
        "group by vault")
    List<RewardDTO> fetchLastRewards();


    List<RewardDTO> getAllByVaultOrderByBlockDate(String vault);

}
