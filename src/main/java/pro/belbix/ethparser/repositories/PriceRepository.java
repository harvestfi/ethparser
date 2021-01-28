package pro.belbix.ethparser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.dto.PriceDTO;

public interface PriceRepository extends JpaRepository<PriceDTO, String> {

}
