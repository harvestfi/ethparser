package pro.belbix.ethparser.repositories.v0;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.dto.v0.PriceDTO;

public interface PriceRepository extends JpaRepository<PriceDTO, String> {

    @Query("select t from PriceDTO t where "
        + "t.sourceAddress = :source "
        + "and t.block <= :block "
        + "and t.network = :network "
        + "order by t.block desc")
    List<PriceDTO> fetchLastPrice(
        @Param("source") String sourceAddress,
        @Param("block") long block,
        @Param("network") String network,
        Pageable pageable
    );

    @Query("select t from PriceDTO t where "
        + "t.sourceAddress = :sourceAddress "
        + "and t.block <= :block "
        + "and t.network = :network "
        + "order by t.block desc")
    List<PriceDTO> fetchLastPriceByAddress(
        @Param("sourceAddress") String sourceAddress,
        @Param("block") long block,
        @Param("network") String network,
        Pageable pageable
    );

    @Query(nativeQuery = true, value = "" +
        "select * from (select source_address from prices "
        + "where network = :network "
        + "group by source_address) sources "
        + "join prices p on p.id = "
        + "                 (select id from prices where "
        + "                  source_address = sources.source_address "
        + "                  order by block desc limit 1)")
    List<PriceDTO> fetchLastPrices(
        @Param("network") String network
    );

}
