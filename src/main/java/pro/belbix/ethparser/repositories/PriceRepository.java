package pro.belbix.ethparser.repositories;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.dto.PriceDTO;

public interface PriceRepository extends JpaRepository<PriceDTO, String> {

    @Query("select t from PriceDTO t where "
        + "t.source = :source "
        + "and t.block <= :block "
        + "order by t.block desc")
    List<PriceDTO> fetchLastPrice(@Param("source") String source, @Param("block") long block, Pageable pageable);

    @Query(nativeQuery = true, value = "" +
        "select SUBSTRING_INDEX(MAX(CONCAT(block_date, '|', id)), '|', -1)      id, " +
        "       max(block)                                                                   block, " +
        "       max(block_date)                                                      block_date, " +
        "       source                                                                source, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', token)), '_', -1)          token, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', token_amount)), '_', -1)     token_amount, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', other_token)), '_', -1)     other_token, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', other_token_amount)), '_', -1)     other_token_amount, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', price)), '_', -1)     price, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', buy)), '_', -1)     buy " +
        "from prices " +
        "group by source")
    List<PriceDTO> fetchLastPrices();

}
