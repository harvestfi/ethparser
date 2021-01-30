package pro.belbix.ethparser.repositories;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.dto.TransferDTO;

public interface TransferRepository extends JpaRepository<TransferDTO, String> {

    @Query("select t from TransferDTO t where "
        + "(lower(t.owner) = lower(:owner) or lower(t.recipient) = lower(:recipient)) "
        + "and t.name = 'FARM'"
        + "and t.blockDate > :from "
        + "and t.blockDate <= :to "
        + "order by t.blockDate asc")
    List<TransferDTO> fetchAllByOwnerAndRecipient(@Param("owner") String owner,
                                                  @Param("recipient") String recipient,
                                                  @Param("from") long from,
                                                  @Param("to") long to);

    @Query("select t from TransferDTO t where "
        + "(lower(t.owner) = lower(:owner) or lower(t.recipient) = lower(:recipient)) "
        + "and t.type in :types "
        + "and t.name = 'FARM'"
        + "and t.blockDate > :from "
        + "and t.blockDate <= :to "
        + "order by t.blockDate asc")
    List<TransferDTO> fetchAllByOwnerAndRecipientAndTypes(@Param("owner") String owner,
                                                          @Param("recipient") String recipient,
                                                          @Param("types") List<String> types,
                                                          @Param("from") long from,
                                                          @Param("to") long to);

    @Query(nativeQuery = true, value = ""
        + "select  coalesce(buys.buy, 0) - coalesce(sells.sell, 0) sum from "
        + "(select sum(value) buy from transfers "
        + "where block_date <= :before and recipient = :address) buys "
        + "    left join "
        + "(select sum(value) sell from transfers "
        + "where block_date <= :before and owner = :address) sells on 1=1")
    Double getBalanceForOwner(@Param("address") String address, @Param("before") long before);

    @Query("select t from TransferDTO t  where t.blockDate > :date order by t.blockDate")
    List<TransferDTO> fetchAllFromBlockDate(@Param("date") long date);

    @Query(nativeQuery = true, value = ""
        + "select * from transfers where method_name is null or method_name like '0x%' order by block_date")
    List<TransferDTO> fetchAllWithoutMethods();

    @Query(nativeQuery = true, value = ""
        + "select * from transfers where transfers.price is null or price = 0 order by block_date")
    List<TransferDTO> fetchAllWithoutPrice();

    @Query(nativeQuery = true, value = ""
        + "select * from transfers t where "
        + "(t.owner = :owner or t.recipient = :recipient) "
        + "and t.type in :types "
        + "and t.block_date < :blockDate "
        + "order by block_date desc")
    List<TransferDTO> fetchLastTransfer(
        @Param("owner") String owner,
        @Param("recipient") String recipient,
        @Param("types") List<String> types,
        @Param("blockDate") long blockDate,
        Pageable pageable
    );

    @Query(nativeQuery = true, value = ""
        + "select * from transfers t where "
        + "(t.profit is null) "
        + "and type in ('PS_EXIT', 'REWARD', 'LP_SELL') "
        + "order by block_date")
    List<TransferDTO> fetchAllWithoutProfits();
}
