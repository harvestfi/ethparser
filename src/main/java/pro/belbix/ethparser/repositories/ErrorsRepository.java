package pro.belbix.ethparser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.dto.v0.ErrorWeb3Dto;

public interface ErrorsRepository extends JpaRepository<ErrorWeb3Dto, String> {

}
