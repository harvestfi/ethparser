package pro.belbix.ethparser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.LogLastEntity;

public interface LogLastRepository extends JpaRepository<LogLastEntity, String> {

}
