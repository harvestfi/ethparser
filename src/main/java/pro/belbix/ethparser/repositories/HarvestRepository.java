package pro.belbix.ethparser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.model.HarvestDTO;

public interface HarvestRepository extends JpaRepository<HarvestDTO, Long> {

}
