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

}
