package pro.belbix.ethparser.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pro.belbix.ethparser.dto.ImportantEventsDTO;

public interface ImportantEventsRepository extends JpaRepository<ImportantEventsDTO, String> {

    List<ImportantEventsDTO> findAllByOrderByBlockDate();

    ImportantEventsDTO findFirstByOrderByBlockDateDesc();

    @Query("select t.mintAmount from ImportantEventsDTO t where t.hash = :hash and t.event = :event")
    List<Double> fetchMintAmount(@Param("hash") String hash, @Param("event") String event);
}
