package pro.belbix.ethparser.repositories;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.dto.HardWorkDTO;

public interface HardWorkRepository extends JpaRepository<HardWorkDTO, String> {

    List<HardWorkDTO> findAllByOrderByBlockDate();

    HardWorkDTO findFirstByOrderByBlockDateDesc();

    @Query("select t from HardWorkDTO t where "
        + "t.blockDate > :from and t.blockDate <= :to order by t.blockDate")
    List<HardWorkDTO> fetchAllInRange(@Param("from") long from,
                                      @Param("to") long to);

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

    @Query("select sum(t.farmBuyback) from HardWorkDTO t where "
        + " t.blockDate < :to")
    List<Double> fetchAllBuybacksAtDate(@Param("to") long to,
                                        Pageable pageable);

    @Query(nativeQuery = true, value = ""
        + "select count(*) from hard_work where vault = :vault and block_date < :blockDate")
    Integer countAtBlockDate(@Param("vault") String vault, @Param("blockDate") long blockDate);

    @Query(nativeQuery = true, value = ""
        + "select sum(saved_gas_fees) from hard_work where vault = :vault and block_date < :blockDate")
    Double sumSavedGasFees(@Param("vault") String vault, @Param("blockDate") long blockDate);

    @Query("select t from HardWorkDTO t where t.vault = :vault and t.blockDate between :startTime and :endTime order by t.blockDate")
    List<HardWorkDTO> findAllByVaultOrderByBlockDate(@Param("vault") String vault, @Param("startTime") long startTime,
                                                    @Param("endTime") long endTime);

    @Query(nativeQuery = true, value = "" +
        "select " +
        "    SUBSTRING_INDEX(MAX(CONCAT(block_date, '|', id)), '|', -1) id, " +
        "    vault, " +
        "    max(block) block, " +
        "    max(block_date) block_date, " +
        "    SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', share_change)), '_', -1) share_change, " +
        "    SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', share_change_usd)), '_', -1) share_change_usd, " +
        "    SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', share_usd_total)), '_', -1)  share_usd_total, " +
        "    SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', tvl)), '_', -1)  tvl, " +
        "    SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', all_profit)), '_', -1) all_profit, " +
        "    SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', period_of_work)), '_', -1) period_of_work, " +
        "    SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', ps_period_of_work)), '_', -1) ps_period_of_work, " +
        "    SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', perc)), '_', -1) perc, " +
        "    SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', ps_tvl_usd)), '_', -1) ps_tvl_usd, " +
        "    SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', ps_apr)), '_', -1)  ps_apr, " +
        "    SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', apr)), '_', -1) apr, " +
        "    SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', weekly_profit)), '_', -1) weekly_profit, " +
        "    SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', weekly_all_profit)), '_', -1) weekly_all_profit, " +
        "    SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', farm_buyback)), '_', -1) farm_buyback, " +
        "    SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', farm_buyback_sum)), '_', -1) farm_buyback_sum, " +
        "    0.0 calls_quantity, " +
        "    SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', pool_users)), '_', -1) pool_users, " +
        "    0.0 saved_gas_fees, " +
        "    SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', saved_gas_fees_sum)), '_', -1) saved_gas_fees_sum, " +
        "    0.0 fee, " +
        "    SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', weekly_average_tvl)), '_', -1) weekly_average_tvl, " +
        "    SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', farm_buyback_eth)), '_', -1) farm_buyback_eth, " +
        "    0.0 fee_eth, " +
        "    0 gas_used, " +
        "    SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', idle_time)), '_', -1) idle_time, " +
        "    SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', invested)), '_', -1) invested, " +
        "    SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', investment_target)), '_', -1) investment_target " +
        "from hard_work " +
        "group by vault "
        + "order by vault")
    List<HardWorkDTO> fetchLatest();

    @Query(nativeQuery = true, value = "select sum(saved_gas_fees_sum) gas_saved from ( "
        + "     select "
        + "         SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', saved_gas_fees_sum)), '_', -1) saved_gas_fees_sum "
        + "     from hard_work "
        + "     group by vault "
        + " ) t")
    Double fetchLastGasSaved();

    @Query(nativeQuery = true, value = "select block_date from hard_work "
        + "where vault = :vault and block_date < :block_date order by block_date desc limit 0,1")
    Long fetchPreviousBlockDateByVaultAndDate(@Param("vault") String vault, @Param("block_date") long blockDate);
}
