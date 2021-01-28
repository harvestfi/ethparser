package pro.belbix.ethparser.repositories;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.dto.PriceDTO;

public interface PriceRepository extends JpaRepository<PriceDTO, String> {

    @Query("select t from PriceDTO t where "
        + "t.source = :source "
        + "and t.block <= :block")
    List<PriceDTO> fetchLastPrice(@Param("source") String source, @Param("block") long block, Pageable pageable);

}
