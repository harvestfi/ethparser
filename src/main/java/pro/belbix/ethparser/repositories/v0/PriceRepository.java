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
    List<PriceDTO> fetchLastPriceBySourceAddress(
        @Param("source") String sourceAddress,
        @Param("block") long block,
        @Param("network") String network,
        Pageable pageable
    );

    @Query("select t from PriceDTO t where "
        + "t.tokenAddress = :address "
        + "and t.block <= :block "
        + "and t.network = :network "
        + "order by t.block desc")
    List<PriceDTO> fetchLastPriceByTokenAddress(
        @Param("address") String tokenAddress,
        @Param("block") long block,
        @Param("network") String network,
        Pageable pageable
    );

    @Query(nativeQuery = true, value = "" +
        "select distinct on (source_address) * from prices "
        + "where network = :network "
        + "order by source_address, block desc")
    List<PriceDTO> fetchLastPrices(
        @Param("network") String network
    );

    @Query(nativeQuery = true, value = ""
        + "select source_address from prices "
        + "where network = :network "
        + "group by source_address")
    List<String> fetchAllSourceAddresses(@Param("network") String network);

}
