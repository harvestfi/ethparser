package pro.belbix.ethparser.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.dto.TransferDTO;

public interface TransferRepository extends JpaRepository<TransferDTO, String> {

    @Query("select t from TransferDTO t where "
        + "(t.owner = :owner or t.recipient = :recipient) "
        + "and t.name = 'FARM'"
        + "and t.blockDate > :from "
        + "and t.blockDate <= :to "
        + "order by t.blockDate asc")
    List<TransferDTO> fetchAllByOwnerAndRecipient(@Param("owner") String owner,
                                                  @Param("recipient") String recipient,
                                                  @Param("from") long from,
                                                  @Param("to") long to);
}
