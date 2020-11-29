package pro.belbix.ethparser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.dto.RewardDTO;

public interface RewardsRepository extends JpaRepository<RewardDTO, String> {

}
