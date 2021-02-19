package pro.belbix.ethparser.repositories.v0;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.dto.v0.ImportantEventsDTO;

public interface ImportantEventsRepository extends JpaRepository<ImportantEventsDTO, String> {

    List<ImportantEventsDTO> findAllByOrderByBlockDate();

    ImportantEventsDTO findFirstByOrderByBlockDateDesc();
}
