package pro.belbix.ethparser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.TransactionLastEntity;

public interface TransactionLastRepository extends JpaRepository<TransactionLastEntity, String> {

}
