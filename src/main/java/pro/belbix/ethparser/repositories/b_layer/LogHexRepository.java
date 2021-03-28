package pro.belbix.ethparser.repositories.b_layer;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.b_layer.LogHashEntity;

public interface LogHexRepository extends JpaRepository<LogHashEntity, String> {

}
