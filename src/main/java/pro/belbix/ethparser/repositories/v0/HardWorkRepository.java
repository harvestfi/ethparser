package pro.belbix.ethparser.repositories.v0;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.dto.v0.HardWorkDTO;

public interface HardWorkRepository extends JpaRepository<HardWorkDTO, String> {

    @Query("select t from HardWorkDTO t where "
        + "t.fullRewardUsd >= :minAmount "
        + "and t.network = :network")
    Page<HardWorkDTO> fetchPages(
        @Param("minAmount") double minAmount,
        @Param("network") String network,
        Pageable pageable);

    @Query("select t from HardWorkDTO t where "
        + "lower(t.vaultAddress) = lower(:vault) "
        + "and t.fullRewardUsd >= :minAmount "
        + "and t.network = :network")
    Page<HardWorkDTO> fetchPagesByVault(
        @Param("vault") String vaultAddress,
        @Param("network") String network,
        @Param("minAmount") double minAmount,
        Pageable pageable);

    HardWorkDTO findFirstByNetworkOrderByBlockDateDesc(String network);

    @Query("select t from HardWorkDTO t where "
        + "t.blockDate between :from and :to "
        + "and t.network = :network "
        + "order by t.blockDate")
    List<HardWorkDTO> fetchAllInRange(@Param("from") long from,
        @Param("to") long to,
        @Param("network") String network
    );

    @Query(""
        + "select sum(t.fullRewardUsd) from HardWorkDTO t "
        + "where t.vaultAddress = :vault "
        + "and t.blockDate <= :blockDate "
        + "and t.network = :network")
    Double getSumForVault(
        @Param("vault") String vaultAddress,
        @Param("blockDate") long blockDate,
        @Param("network") String network
    );

    @Query("select sum(t.perc) from HardWorkDTO t where "
        + "t.vaultAddress = :vault "
        + "and t.blockDate <= :to "
        + "and t.network = :network")
    List<Double> fetchPercentForPeriod(
        @Param("vault") String vaultAddress,
        @Param("to") long to,
        @Param("network") String network,
        Pageable pageable);

    @Query("select sum(t.fullRewardUsd) from HardWorkDTO t where "
        + "t.vaultAddress = :vault "
        + "and t.blockDate between :from and :to "
        + "and t.network = :network")
    List<Double> fetchProfitForPeriod(
        @Param("vault") String vaultAddress,
        @Param("from") long from,
        @Param("to") long to,
        @Param("network") String network,
        Pageable pageable);

    @Query("select sum(t.fullRewardUsd) from HardWorkDTO t where "
        + "t.blockDate <= :to "
        + "and t.network = :network")
    List<Double> fetchAllProfitAtDate(
        @Param("to") long to,
        @Param("network") String network,
        Pageable pageable);

    @Query("select sum(t.fullRewardUsd) from HardWorkDTO t where "
        + "t.blockDate between :from and :to "
        + "and t.network = :network")
    List<Double> fetchAllProfitForPeriod(
        @Param("from") long from,
        @Param("to") long to,
        @Param("network") String network,
        Pageable pageable);

    @Query("select sum(t.farmBuyback) from HardWorkDTO t where "
        + " t.blockDate < :to "
        + "and t.network = :network")
    List<Double> fetchAllBuybacksAtDate(
        @Param("to") long to,
        @Param("network") String network,
        Pageable pageable);

    @Query("select count(t) from HardWorkDTO t where "
        + "t.vaultAddress = :vault "
        + "and t.blockDate <= :blockDate "
        + "and t.network = :network")
    Integer countAtBlockDate(
        @Param("vault") String vaultAddress,
        @Param("network") String network,
        @Param("blockDate") long blockDate);

    @Query("select sum(t.savedGasFees) from HardWorkDTO t where "
        + "t.vaultAddress = :vault "
        + "and t.network = :network "
        + "and t.blockDate < :blockDate")
    Double sumSavedGasFees(
        @Param("vault") String vaultAddress,
        @Param("network") String network,
        @Param("blockDate") long blockDate
    );

    @Query("select t from HardWorkDTO t where "
        + "t.vaultAddress = :vault "
        + "and t.network = :network "
        + "and t.blockDate between :startTime and :endTime "
        + "order by t.blockDate")
    List<HardWorkDTO> findAllByVaultOrderByBlockDate(
        @Param("vault") String vaultAddress,
        @Param("network") String network,
        @Param("startTime") long startTime,
        @Param("endTime") long endTime);

    @Query(nativeQuery = true, value = "" +
        "select distinct on (vault_address) * from hard_work "
        + "where network = :network "
        + "order by vault_address, block_date desc")
    List<HardWorkDTO> fetchLatest(@Param("network") String network);

    @Query(nativeQuery = true, value = ""
        + "select sum(t.saved_gas_fees_sum) gas_saved "
        + "from ( "
        + "         select distinct on (vault) "
        + "                last_value(saved_gas_fees_sum) over w as saved_gas_fees_sum "
        + "         from hard_work where network = :network "
        + "             window w as (PARTITION BY vault order by block_date desc) "
        + "     ) t")
    Double fetchLastGasSaved(@Param("network") String network);

    @Query(nativeQuery = true, value = "select block_date from hard_work "
        + "where vault_address = :vault "
        + "and block_date < :block_date "
        + "and network = :network "
        + "order by block_date desc limit 1")
    Long fetchPreviousBlockDateByVaultAndDate(
        @Param("vault") String vaultAddress,
        @Param("network") String network,
        @Param("block_date") long blockDate
    );

    @Query("select t from HardWorkDTO t where "
        + "t.vaultAddress is null or t.vaultAddress = ''")
    List<HardWorkDTO> fetchAllWithoutAddresses();
}
