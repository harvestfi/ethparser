package pro.belbix.ethparser.repositories;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.dto.HarvestDTO;

public interface HarvestRepository extends JpaRepository<HarvestDTO, String> {

    List<HarvestDTO> findAllByOrderByBlockDate();

    List<HarvestDTO> findAllByBlockDateGreaterThanOrderByBlockDate(long blockDate);

    @Query(nativeQuery = true, value = ""
        + "select * from harvest_tx where all_pools_owners_count is null order by block_date;")
    List<HarvestDTO> fetchAllWithoutCounts();

    @Query(nativeQuery = true, value = ""
        + "select (coalesce(deposit.d, 0) - coalesce(withdraw.w, 0)) result from (  "
        + "                  (select SUM(amount) d  "
        + "                  from harvest_tx t  "
        + "                  where t.method_name = 'Deposit'  "
        + "                      and t.vault = :vault  "
        + "                      and t.block_date <= :block_date) deposit,  "
        + "                  (select SUM(amount) w  "
        + "                  from harvest_tx t  "
        + "                  where t.method_name = 'Withdraw'  "
        + "                      and t.vault = :vault  "
        + "                      and t.block_date <= :block_date) withdraw  "
        + "              )")
    Double fetchTVL(@Param("vault") String vault, @Param("block_date") long blockDate);

    @Query(nativeQuery = true, value = "select * from harvest_tx "
        + "where vault = :vault and block_date <= :block_date order by block_date desc limit 0,1")
    HarvestDTO fetchLastByVaultAndDate(@Param("vault") String vault, @Param("block_date") long blockDate);

    @Query(nativeQuery = true, value = "select * from harvest_tx "
        + "where vault = :vault and last_usd_tvl != 0 and block_date <= :block_date order by block_date desc limit 0,1")
    HarvestDTO fetchLastByVaultAndDateNotZero(@Param("vault") String vault, @Param("block_date") long blockDate);

    @Query(nativeQuery = true, value = ""
        + "select count(*) from ("
        + "select t.owner from harvest_tx t where t.vault = :vault and t.block_date <= :block_date group by t.owner) tt"
    )
    Integer fetchOwnerCount(@Param("vault") String vault, @Param("block_date") long blockDate);

    @Query(nativeQuery = true, value = ""
        + "select count(owner) from ( "
        + "                  select owner, "
        + "                         SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', owner_balance_usd)), '_', -1) balance "
        + "                  from harvest_tx "
        + "                  where vault in (:vault, :oldVault) and block_date <= :block_date "
        + "                  group by owner "
        + "              ) t "
        + "where balance > 10 ")
    Integer fetchActualOwnerQuantity(@Param("vault") String vault,
                                     @Param("oldVault") String oldVault,
                                     @Param("block_date") long blockDate);

    @Query(nativeQuery = true, value = ""
        + "select count(owner) owners from ( "
        + "         select distinct owner from ( "
        + "                  select owner from harvest_tx "
        + "                   where harvest_tx.block_date <= :block_date "
        + "                  union all "
        + "                  select owner from uni_tx "
        + "                   where uni_tx.block_date <= :block_date "
        + "              ) t "
        + "     ) t2")
    Integer fetchAllUsersQuantity(@Param("block_date") long blockDate);

    @Query(nativeQuery = true, value = ""
        + "select count(owner) owners "
        + "from (select owner, "
        + "                      SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', owner_balance_usd)), '_', -1) balance "
        + "               from harvest_tx "
        + "               where vault in :vaults and block_date < :block_date"
        + "               group by owner "
        + "              ) t "
        + "    where balance > 10")
    Integer fetchAllPoolsUsersQuantity(@Param("vaults") List<String> vaults, @Param("block_date") long blockDate);

    HarvestDTO findFirstByOrderByBlockDesc();

    HarvestDTO findFirstByVaultAndBlockDateLessThanEqualAndIdNotOrderByBlockDateDesc(String vault, long date,
                                                                                     String id);

    @Query("select t.lastTvl from HarvestDTO t where t.vault = :vault and t.blockDate <= :from order by t.blockDate desc")
    List<Double> fetchTvlFrom(@Param("from") long from, @Param("vault") String vault, Pageable pageable);

    @Query("select t.lastUsdTvl from HarvestDTO t where t.vault = :vault and t.blockDate <= :from  order by t.blockDate desc")
    List<Double> fetchUsdTvlFrom(@Param("from") long from, @Param("vault") String vault, Pageable pageable);

    @Query("select max(t.blockDate) - min(t.blockDate) as period from HarvestDTO t where "
        + "t.vault = :vault and t.blockDate <= :to order by t.blockDate desc")
    List<Long> fetchPeriodOfWork(@Param("vault") String vault,
                                 @Param("to") long to,
                                 Pageable pageable);

    @Query("select avg(t.lastUsdTvl) as period from HarvestDTO t where "
        + "t.vault = :vault and t.blockDate > :from and t.blockDate <= :to")
    List<Double> fetchAverageTvl(@Param("vault") String vault,
                                   @Param("from") long from,
                                 @Param("to") long to,
                                 Pageable pageable);

    @Query("select t from HarvestDTO t where "
        + "t.owner = :owner and t.blockDate > :from and t.blockDate <= :to order by t.blockDate asc")
    List<HarvestDTO> fetchAllByOwner(@Param("owner") String owner, @Param("from") long from, @Param("to") long to);

    @Query("select t from HarvestDTO t where "
        + "t.ownerBalance is null "
        + "or t.ownerBalanceUsd is null "
        + "order by t.blockDate asc")
    List<HarvestDTO> fetchAllWithoutOwnerBalance();

    @Query("select t from HarvestDTO t where "
        + "t.sharePrice = 0.00000000000000001"
        + "order by t.blockDate asc")
    List<HarvestDTO> fetchAllMigration();

    HarvestDTO findFirstByVaultAndBlockDateBeforeOrderByBlockDateDesc(String vault, long blockDate);

    HarvestDTO findFirstByVaultOrderByBlockDate(String vault);

    @Query(nativeQuery = true, value =
        "select * from harvest_tx t where t.block_date > :fromTs order by t.block_date")
    List<HarvestDTO> fetchAllFromBlockDate(@Param("fromTs") long fromTs);

    List<HarvestDTO> findAllByVaultOrderByBlockDate(String vault);

    @Query(nativeQuery = true, value = "" +
        "select " +
        "    id as id, " +
        "    null as amount, " +
        "    null as amount_in, " +
        "    null as block, " +
        "    block_date as block_date, " +
        "    confirmed as confirmed, " +
        "    null as hash, " +
        "    null as last_gas, " +
        "    last_tvl as last_tvl, " +
        "    last_usd_tvl as last_usd_tvl, " +
        "    null as method_name, " +
        "    null as owner, " +
        "    owner_count as owner_count, " +
        "    share_price as share_price, " +
        "    null as usd_amount, " +
        "    null as vault, " +
        "    null as prices, " +
        "    null as lp_stat, " +
        "    null as last_all_usd_tvl, " +
        "    null as owner_balance, " +
        "    owner_balance_usd as owner_balance_usd, " +
        "    all_owners_count as all_owners_count, " +
        "    all_pools_owners_count as all_pools_owners_count, " +
        "    false as migrated " +
        "from harvest_tx where vault = :vault order by block_date")
    List<HarvestDTO> fetchAllTvlForVault(@Param("vault") String vault);

    @Query(nativeQuery = true, value = "" +
        "select max(id)                                                              id, " +
        "       null                                                                 hash, " +
        "       null                                                                 block, " +
        "       true                                                                 confirmed, " +
        "       max(block_date)                                                      block_date, " +
        "       null                                                                 method_name, " +
        "       null                                                                 owner, " +
        "       null                                                                 amount, " +
        "       null                                                                 amount_in, " +
        "       vault                                                                vault, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', last_gas)), '_', -1)     last_gas, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', last_tvl)), '_', -1)     last_tvl, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', last_usd_tvl)), '_', -1) last_usd_tvl, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', owner_count)), '_', -1)  owner_count, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', share_price)), '_', -1)  share_price, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', usd_amount)), '_', -1)   usd_amount, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', prices)), '_', -1)       prices, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', lp_stat)), '_', -1)      lp_stat, " +
        "       null      owner_balance, " +
        "       null      owner_balance_usd, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', all_owners_count)), '_', -1) all_owners_count, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', last_all_usd_tvl)), '_', -1)      last_all_usd_tvl, " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', all_pools_owners_count)), '_', -1)      all_pools_owners_count, " +
        "       false      migrated " +
        " " +
        "from harvest_tx " +
        "group by vault")
    List<HarvestDTO> fetchLastTvl();

    @Query("select t from HarvestDTO t where "
        + "t.blockDate > :from "
        + "and t.blockDate <= :to "
        + "order by t.blockDate")
    List<HarvestDTO> fetchAllByPeriod(@Param("from") long from, @Param("to") long to);

    List<HarvestDTO> findAllByMethodNameAndBlockDateGreaterThanOrderByBlockDate(String methodName, long blockDate);

    @Query("select t from HarvestDTO t where "
        + "t.owner = :owner "
        + "and t.vault = :vault "
        + "and t.blockDate <= :blockDate "
        + "and t.blockDate > :lastFullWithdrawBlockDate " 
        + "order by t.blockDate")
    List<HarvestDTO> fetchPeriodForOwnerAndVault(@Param("owner") String owner,
                                                @Param("vault") String vault,
                                                @Param("blockDate") long blockDate,
                                                @Param("lastFullWithdrawBlockDate") long lastFullWithdrawBlockDate);

    @Query(nativeQuery = true, value = "select block_date from harvest_tx "
        + "where owner = :owner "
        + "and vault = :vault "
        + "and method_name = 'Withdraw' "
        + "and block_date < :blockDate "
        + "and profit > 0 "
        + "order by block_date desc limit 0,1")
    Long findLastFullWithdrawByOwnerAndVault(@Param("owner") String owner, 
                                        @Param("vault") String vault, 
                                        @Param("blockDate") long blockDate);
}
