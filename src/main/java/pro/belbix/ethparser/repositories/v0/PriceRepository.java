package pro.belbix.ethparser.repositories.v0;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.dto.v0.PriceDTO;

public interface PriceRepository extends JpaRepository<PriceDTO, String> {

    @Query("select t from PriceDTO t where "
        + "t.source = :source "
        + "and t.block <= :block "
        + "order by t.block desc")
    List<PriceDTO> fetchLastPrice(@Param("source") String source, @Param("block") long block, Pageable pageable);

    @Query("select t from PriceDTO t where "
    + "t.source = 'ORACLE' "
    + "and t.token = :token "
    + "and t.block <= :block "
    + "order by t.block desc")
    List<PriceDTO> fetchLastOraclePrice(@Param("token") String token, @Param("block") long block, Pageable pageable);

    @Query(nativeQuery = true, value = "" +
        "select distinct on (source) "
        + "    last_value(id) over w                 as id, "
        + "    last_value(block) over w              as block, "
        + "    last_value(block_date) over w         as block_date, "
        + "    source, "
        + "    last_value(token) over w              as token, "
        + "    last_value(token_amount) over w       as token_amount, "
        + "    last_value(other_token) over w        as other_token, "
        + "    last_value(other_token_amount) over w as other_token_amount, "
        + "    last_value(price) over w              as price, "
        + "    last_value(buy) over w                as buy, "
        + "    last_value(lp_token0pooled) over w    as lp_token0pooled, "
        + "    last_value(lp_token1pooled) over w    as lp_token1pooled, "
        + "    last_value(lp_total_supply) over w    as lp_total_supply "
        + "from prices "
        + "    window w as (PARTITION BY source order by block_date desc)")
    List<PriceDTO> fetchLastPrices();

}
