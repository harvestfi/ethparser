package pro.belbix.ethparser.repositories.b_layer;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.b_layer.LogHexEntity;

public interface LogHexRepository extends JpaRepository<LogHexEntity, String> {

}
