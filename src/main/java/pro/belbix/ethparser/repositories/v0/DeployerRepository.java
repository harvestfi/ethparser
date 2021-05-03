package pro.belbix.ethparser.repositories.v0;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.dto.v0.DeployerDTO;

public interface DeployerRepository extends JpaRepository<DeployerDTO, String> {

  DeployerDTO findFirstByNetworkOrderByBlockDesc(String network);

}
