package pro.belbix.ethparser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.model.TransactionDTO;

public interface TransactionsRepository extends JpaRepository<TransactionDTO, Long> {

}
