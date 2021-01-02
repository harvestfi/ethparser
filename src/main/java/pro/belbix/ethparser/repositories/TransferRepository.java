package pro.belbix.ethparser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.dto.TransferDTO;

public interface TransferRepository extends JpaRepository<TransferDTO, String> {

}
