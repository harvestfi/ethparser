package pro.belbix.ethparser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.dto.DeployerDTO;

import java.util.List;

public interface DeployerRepository extends JpaRepository<DeployerDTO, String> {
  List<DeployerDTO> findAllByOrderByBlockDate();
}
