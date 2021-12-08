package pro.belbix.ethparser.repositories.v0;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.dto.v0.BancorDTO;

public interface BancorRepository extends JpaRepository<BancorDTO, String> {

}
