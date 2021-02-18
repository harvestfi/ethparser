package pro.belbix.ethparser.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.dto.DeployerDTO;

public interface DeployerRepository extends JpaRepository<DeployerDTO, String> {
  List<DeployerDTO> findAllByOrderByBlockDate();
}
