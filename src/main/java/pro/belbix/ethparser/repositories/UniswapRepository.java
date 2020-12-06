package pro.belbix.ethparser.repositories;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.dto.UniswapDTO;

public interface UniswapRepository extends JpaRepository<UniswapDTO, String> {

    @Query(nativeQuery = true, value = ""
        + "select count(*) from ("
        + "select t.owner from uni_tx t where coin = 'FARM' group by t.owner) tt"
    )
    Integer fetchOwnerCount();

    UniswapDTO findFirstByCoinOrderByBlockDesc(String coin);

    UniswapDTO findFirstByBlockDateAndCoinBeforeOrderByBlockDesc(long blockDate, String coin);

    @Query("select sum(t.amount) from UniswapDTO t "
        + "where t.coin = 'FARM' and t.owner = :owner and t.blockDate <= :from")
    List<Double> fetchAmountSum(@Param("from") long from, @Param("owner") String owner, Pageable pageable);

    @Query("select sum(t.otherAmount) from UniswapDTO t "
        + "where t.coin = 'FARM' and t.owner = :owner and t.blockDate <= :from")
    List<Double> fetchAmountSumUsd(@Param("from") long from, @Param("owner") String owner, Pageable pageable);

    List<UniswapDTO> findAllByOwnerAndCoinOrderByBlockDate(String owner, String coin);

}
