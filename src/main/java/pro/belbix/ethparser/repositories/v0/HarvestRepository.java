package pro.belbix.ethparser.repositories.v0;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.dto.v0.HarvestDTO;

public interface HarvestRepository extends JpaRepository<HarvestDTO, String> {

    List<HarvestDTO> findAllByOrderByBlockDate();

    List<HarvestDTO> findAllByBlockDateGreaterThanOrderByBlockDate(long blockDate);

    @Query(nativeQuery = true, value = ""
        + "select * from harvest_tx where all_pools_owners_count is null order by block_date;")
    List<HarvestDTO> fetchAllWithoutCounts();

    @Query(nativeQuery = true, value = ""
        + "select (coalesce( (select SUM(amount) d "
        + "                   from harvest_tx t "
        + "                   where t.method_name = 'Deposit' "
        + "                     and t.vault = :vault "
        + "                     and t.block_date <= :block_date), 0) - "
        + "        coalesce((select SUM(amount) w "
        + "                  from harvest_tx t "
        + "                  where t.method_name = 'Withdraw' "
        + "                    and t.vault = :vault "
        + "                    and t.block_date <= :block_date), 0)) result "
        + "from (select where true) t")
    Double fetchTVL(@Param("vault") String vault, @Param("block_date") long blockDate);

    @Query(nativeQuery = true, value = "select * from harvest_tx "
        + "where vault = :vault and block_date <= :block_date order by block_date desc limit 1")
    HarvestDTO fetchLastByVaultAndDate(@Param("vault") String vault, @Param("block_date") long blockDate);

    @Query(nativeQuery = true, value = "select * from harvest_tx "
        + "where vault = :vault and last_usd_tvl != 0 and block_date <= :block_date order by block_date desc limit 1")
    HarvestDTO fetchLastByVaultAndDateNotZero(@Param("vault") String vault, @Param("block_date") long blockDate);

    @Query(nativeQuery = true, value = ""
        + "select count(*) from ("
        + "select t.owner from harvest_tx t where t.vault = :vault and t.block_date <= :block_date group by t.owner) tt"
    )
    Integer fetchOwnerCount(@Param("vault") String vault, @Param("block_date") long blockDate);

    @Query(nativeQuery = true, value = ""
        + "select count(t.owner) "
        + "from ( "
        + "         select distinct on (owner) owner, "
        + "                                    last_value(owner_balance_usd) over w as balance "
        + "         from harvest_tx "
        + "         where vault in (:vault, :oldVault) "
        + "           and block_date <= :block_date "
        + "             window w as (PARTITION BY vault order by block_date desc) "
        + "     ) t "
        + "where t.balance > 10"
    )
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
        + "select count(owner) from ( "
        + "    select t.owner, sum(t.b) balance "
        + "    from (select owner, "
        + "      cast(SUBSTRING_INDEX(MAX(CONCAT(block_date, SUBSTRING_INDEX(id, '_', -1), '|', "
        + "                 coalesce(owner_balance_usd, 0))), '|', -1) as DOUBLE PRECISION) b "
        + "          from harvest_tx "
        + "          where vault in :vaults and block_date < :block_date "
        + "          group by vault, owner) t "
        + "                   where t.b > 10 "
        + "    group by t.owner "
        + "    order by balance desc "
        + ") t2")
    Integer fetchAllPoolsUsersQuantity(@Param("vaults") List<String> vaults, @Param("block_date") long blockDate);

    HarvestDTO findFirstByOrderByBlockDesc();

    HarvestDTO findFirstByVaultAndBlockDateLessThanEqualAndIdNotOrderByBlockDateDesc(String vault, long date,
                                                                                     String id);

    @Query("select t.lastTvl from HarvestDTO t where t.vault = :vault and t.blockDate <= :from order by t.blockDate desc")
    List<Double> fetchTvlFrom(@Param("from") long from, @Param("vault") String vault, Pageable pageable);

    @Query("select t.lastUsdTvl from HarvestDTO t where t.vault = :vault and t.blockDate <= :from  order by t.blockDate desc")
    List<Double> fetchUsdTvlFrom(@Param("from") long from, @Param("vault") String vault, Pageable pageable);

    @Query("select max(t.blockDate) - min(t.blockDate) as period from HarvestDTO t where "
        + "t.vault = :vault and t.blockDate <= :to "
        + "group by t.blockDate "
        + "order by t.blockDate desc")
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

    @Query("select t from HarvestDTO t where t.vault = :vault and t.blockDate between :startTime and :endTime order by t.blockDate")
    List<HarvestDTO> findAllByVaultOrderByBlockDate(@Param("vault") String vault, @Param("startTime") long startTime,
                                                    @Param("endTime") long endTime);

    @Query(nativeQuery = true, value = "" +
        "select distinct on (vault) "
        + "    last_value(id) over w                     as id, "
        + "    last_value(hash) over w                   as hash, "
        + "    last_value(block) over w                  as block, "
        + "    last_value(confirmed) over w              as confirmed, "
        + "    last_value(block_date) over w             as block_date, "
        + "    last_value(method_name) over w            as method_name, "
        + "    last_value(owner) over w                  as owner, "
        + "    last_value(amount) over w                 as amount, "
        + "    last_value(amount_in) over w              as amount_in, "
        + "    vault, "
        + "    last_value(last_gas) over w               as last_gas, "
        + "    last_value(last_tvl) over w               as last_tvl, "
        + "    last_value(last_usd_tvl) over w           as last_usd_tvl, "
        + "    last_value(owner_count) over w            as owner_count, "
        + "    last_value(share_price) over w            as share_price, "
        + "    last_value(usd_amount) over w             as usd_amount, "
        + "    last_value(prices) over w                 as prices, "
        + "    last_value(lp_stat) over w                as lp_stat, "
        + "    last_value(owner_balance) over w          as owner_balance, "
        + "    last_value(owner_balance_usd) over w      as owner_balance_usd, "
        + "    last_value(all_owners_count) over w       as all_owners_count, "
        + "    last_value(last_all_usd_tvl) over w       as last_all_usd_tvl, "
        + "    last_value(underlying_price) over w       as underlying_price, "
        + "    last_value(profit) over w                 as profit, "
        + "    last_value(profit_usd) over w             as profit_usd, "
        + "    last_value(total_amount) over w           as total_amount, "
        + "    last_value(all_pools_owners_count) over w as all_pools_owners_count, "
        + "    last_value(migrated) over w               as migrated "
        + "from harvest_tx "
        + "    window w as (PARTITION BY vault order by block_date desc)")
    List<HarvestDTO> fetchLastTvl();

    @Query("select t from HarvestDTO t where "
        + "t.blockDate > :from "
        + "and t.blockDate <= :to "
        + "order by t.blockDate")
    List<HarvestDTO> fetchAllByPeriod(@Param("from") long from, @Param("to") long to);

    List<HarvestDTO> findAllByMethodNameAndBlockDateGreaterThanOrderByBlockDate(String methodName, long blockDate);

    @Query(nativeQuery = true, value = "select * from ( "
        + "                  select *, "
        + "                         coalesce((select block_date "
        + "                                   from harvest_tx "
        + "                                   where owner = :owner "
        + "                                     and vault = :vault "
        + "                                     and method_name = 'Withdraw' "
        + "                                     and block_date < :blockDate "
        + "                                     and owner_balance = 0 "
        + "                                   order by block_date desc "
        + "                                   limit 1), 0) last_withdraw_block_date "
        + "                  from harvest_tx "
        + "                  where owner = :owner "
        + "                    and vault = :vault "
        + "                    and block_date < :blockDate "
        + "                  order by block_date "
        + "              ) t where block_date > last_withdraw_block_date")
    List<HarvestDTO> fetchLatestSinceLastWithdraw(@Param("owner") String owner,
                                                  @Param("vault") String vault,
                                                  @Param("blockDate") long blockDate);

    @Query(nativeQuery = true, value = ""
        + "select t.owner, sum(t.b) balance from "
        + "(select owner, "
        + "        cast(SUBSTRING_INDEX(MAX(CONCAT(block_date, SUBSTRING_INDEX(id, '_', -1), '|', coalesce(owner_balance_usd, 0))), '|', -1) as DOUBLE PRECISION) as b "
        + " from harvest_tx "
        + " group by vault, owner) t "
        + "where t.b > 50000 " //for optimization
        + "group by t.owner "
        + "order by balance desc")
    List<UserBalance> fetchOwnerBalances();

    interface UserBalance {

        String getOwner();

        double getBalance();
    }
}
