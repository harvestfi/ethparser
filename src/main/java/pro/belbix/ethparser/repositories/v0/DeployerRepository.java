package pro.belbix.ethparser.repositories.v0;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.dto.v0.DeployerDTO;

public interface DeployerRepository extends JpaRepository<DeployerDTO, String> {
  List<DeployerDTO> findAllByOrderByBlockDate();
}
