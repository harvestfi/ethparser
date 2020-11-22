package pro.belbix.ethparser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.dto.HardWorkDTO;

public interface HardWorkRepository extends JpaRepository<HardWorkDTO, String> {

    @Query(""
        + "select sum(t.shareChangeUsd) from HardWorkDTO t "
        + "where t.vault = :vault "
        + "and t.blockDate <= :blockDate")
    Double getSumForVault(@Param("vault") String vault, @Param("blockDate") long blockDate);
}
