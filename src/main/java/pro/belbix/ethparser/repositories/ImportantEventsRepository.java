package pro.belbix.ethparser.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.dto.ImportantEventsDTO;

public interface ImportantEventsRepository extends JpaRepository<ImportantEventsDTO, String> {

    List<ImportantEventsDTO> findAllByOrderByBlockDate();

    ImportantEventsDTO findFirstByOrderByBlockDateDesc();
}
