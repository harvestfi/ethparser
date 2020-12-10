package pro.belbix.ethparser.repositories;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.dto.HardWorkDTO;

public interface HardWorkRepository extends JpaRepository<HardWorkDTO, String> {

    List<HardWorkDTO> findAllByOrderByBlockDate();

    @Query(""
        + "select sum(t.shareChangeUsd) from HardWorkDTO t "
        + "where t.vault = :vault "
        + "and t.blockDate <= :blockDate")
    Double getSumForVault(@Param("vault") String vault, @Param("blockDate") long blockDate);

    @Query("select sum(t.perc) from HardWorkDTO t where "
        + "t.vault = :vault and t.blockDate <= :to")
    List<Double> fetchPercentForPeriod(@Param("vault") String vault,
                                       @Param("to") long to,
                                       Pageable pageable);

    @Query("select sum(t.shareChangeUsd) from HardWorkDTO t where "
        + "t.vault = :vault and t.blockDate > :from and t.blockDate <= :to")
    List<Double> fetchProfitForPeriod(@Param("vault") String vault,
                                       @Param("from") long from,
                                       @Param("to") long to,
                                       Pageable pageable);

    @Query("select sum(t.shareChangeUsd) from HardWorkDTO t where "
        + "t.blockDate <= :to")
    List<Double> fetchAllProfitAtDate(@Param("to") long to,
                                      Pageable pageable);

    @Query("select sum(t.shareChangeUsd) from HardWorkDTO t where "
        + "t.blockDate > :from and t.blockDate <= :to")
    List<Double> fetchAllProfitForPeriod(@Param("from") long from,
                                         @Param("to") long to,
                                      Pageable pageable);
}
