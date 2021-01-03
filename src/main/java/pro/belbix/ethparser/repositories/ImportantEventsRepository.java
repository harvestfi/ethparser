package pro.belbix.ethparser.repositories;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.dto.ImportantEventsDTO;

public interface ImportantEventsRepository extends JpaRepository<ImportantEventsDTO, String> {

    List<ImportantEventsDTO> findAllByOrderByBlockDate();

    ImportantEventsDTO findFirstByOrderByBlockDateDesc();
}
