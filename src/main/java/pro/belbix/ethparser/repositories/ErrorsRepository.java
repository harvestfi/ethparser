package pro.belbix.ethparser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.ErrorEntity;

public interface ErrorsRepository extends JpaRepository<ErrorEntity, String> {

}
