package pro.belbix.ethparser.repositories.v0;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.dto.v0.HarvestDTO;

public interface HarvestRepository extends JpaRepository<HarvestDTO, String> {

    List<HarvestDTO> findAllByNetworkOrderByBlockDate(String network);

    List<HarvestDTO> findAllByBlockDateGreaterThanAndNetworkOrderByBlockDate(long blockDate, String network);

    @Query(nativeQuery = true, value = ""
        + "select * from harvest_tx where "
        + "all_pools_owners_count is null "
        + "and network = :network "
        + "order by block_date;")
    List<HarvestDTO> fetchAllWithoutCounts(@Param("network") String network);

    @Query(nativeQuery = true, value = ""
        + "select * from harvest_tx "
        + "where lower(vault_address) = lower(:vault) "
        + "and network = :network "
        + "and block_date <= :block_date "
        + "order by block_date desc limit 1")
    HarvestDTO fetchLastByVaultAndDate(
        @Param("vault") String vaultAddress,
        @Param("network") String network,
        @Param("block_date") long blockDate
    );

    @Query(nativeQuery = true, value = "select * from harvest_tx "
        + "where lower(vault_address) = lower(:vault) "
        + "and last_usd_tvl != 0 "
        + "and network = :network "
        + "and block_date <= :block_date "
        + "order by block_date desc limit 1")
    HarvestDTO fetchLastByVaultAndDateNotZero(
        @Param("vault") String vaultAddress,
        @Param("network") String network,
        @Param("block_date") long blockDate
    );

    @Query(nativeQuery = true, value = ""
        + "select count(t.owner) "
        + "from ( "
        + "         select distinct on (owner) owner, "
        + "                                    last_value(owner_balance_usd) over w as balance "
        + "         from harvest_tx "
        + "         where lower(vault_address) = lower(:vault) "
        + "           and block_date <= :block_date "
        + "           and network = :network "
        + "             window w as (PARTITION BY owner order by block_date desc) "
        + "     ) t "
        + "where t.balance > 10"
    )
    Integer fetchActualOwnerQuantity(@Param("vault") String vaultAddress,
        @Param("network") String network,
        @Param("block_date") long blockDate);

    @Query(nativeQuery = true, value = ""
        + "select count(owner) owners from ( "
        + "         select distinct owner from ( "
        + "                  select owner from harvest_tx "
        + "                   where harvest_tx.block_date <= :block_date "
        + "                         and harvest_tx.network = :network"
        + "                  union all "
        + "                  select owner from uni_tx "
        + "                   where uni_tx.block_date <= :block_date "
        + "                         and 'eth' = :network "
        + "              ) t "
        + "     ) t2")
    Integer fetchAllUsersQuantity(
        @Param("block_date") long blockDate,
        @Param("network") String network
    );

    @Query(nativeQuery = true, value = ""
        + "select count(owner) from ( "
        + "    select t.owner, sum(t.b) balance "
        + "    from (select owner, "
        + "      cast(SUBSTRING_INDEX(cast(MAX(CONCAT(block_date, SUBSTRING_INDEX(cast(id as text), cast('_' as text), cast(-1 as integer)), '|', "
        + "                 coalesce(owner_balance_usd, 0))) as text), '|', -1) as DOUBLE PRECISION) b "
        + "          from harvest_tx "
        + "          where lower(vault_address) in :vaults "
        + "          and block_date < :block_date "
        + "          and network = :network "
        + "          group by vault, owner) t "
        + "                   where t.b > 10 "
        + "    group by t.owner "
        + "    order by balance desc "
        + ") t2")
    Integer fetchAllPoolsUsersQuantity(
        @Param("vaults") List<String> vaultAddresses,
        @Param("block_date") long blockDate,
        @Param("network") String network
    );

    @Query(nativeQuery = true, value = ""
        + " select coalesce(sum(t.deposit), 0) as difference "
        + " from ( "
        + "         select - sum(harvest_tx.owner_balance) as deposit "
        + "         from harvest_tx "
        + "         where harvest_tx.owner = :owner "
        + "           and harvest_tx.vault = :vault "
        + "           and harvest_tx.network = :network "
        + "           and harvest_tx.method_name = 'Deposit' "
        + "           and harvest_tx.block_date <= :block_date "
        + "         union all "
        + "         select sum(harvest_tx.owner_balance) as withdrow "
        + "         from harvest_tx "
        + "         where harvest_tx.owner = :owner "
        + "           and harvest_tx.vault = :vault "
        + "           and harvest_tx.network = :network "
        + "           and harvest_tx.method_name = 'Withdraw' "
        + "           and harvest_tx.block_date <= :block_date "
        + " ) t ")
    Double fetchTotalDifferenceDepositAndWithdraw(
        @Param("vault") String vault,
        @Param("owner") String owner,
        @Param("block_date") long blockDate,
        @Param("network") String network
    );

    HarvestDTO findFirstByNetworkOrderByBlockDesc(String network);

    @Query("select max(t.blockDate) - min(t.blockDate) as period from HarvestDTO t where "
        + "lower(t.vaultAddress) = lower(:vault) "
        + "and t.network = :network "
        + "and t.blockDate <= :to")
    List<Long> fetchPeriodOfWork(@Param("vault") String vaultAddress,
        @Param("to") long to,
        @Param("network") String network,
        Pageable pageable);

    @Query("select avg(t.lastUsdTvl) as period from HarvestDTO t where "
        + "lower(t.vaultAddress) = lower(:vault) "
        + "and t.blockDate between :from and :to "
        + "and t.network = :network")
    List<Double> fetchAverageTvl(
        @Param("vault") String vaultAddress,
        @Param("from") long from,
        @Param("to") long to,
        @Param("network") String network,
        Pageable pageable);

    @Query("select t from HarvestDTO t where "
        + "t.owner = :owner "
        + "and t.blockDate between :from and :to "
        + "and t.network = :network "
        + "order by t.blockDate asc")
    List<HarvestDTO> fetchAllByOwner(
        @Param("owner") String owner,
        @Param("from") long from,
        @Param("to") long to,
        @Param("network") String network
    );

    @Query("select t from HarvestDTO t where "
        + "t.owner = :owner "
        + "and t.blockDate between :from and :to "
        + "order by t.blockDate asc")
    List<HarvestDTO> fetchAllByOwnerWithoutNetwork(
        @Param("owner") String owner,
        @Param("from") long from,
        @Param("to") long to
    );

    @Query("select t from HarvestDTO t where "
        + "lower(t.vaultAddress) = lower(:vault) "
        + "and t.blockDate between :from and :to "
        + "and t.network = :network "
        + "order by t.blockDate asc")
    List<HarvestDTO> fetchAllByVaultAddressAndNetwork(
        @Param("vault") String vaultAddress,
        @Param("from") long from,
        @Param("to") long to,
        @Param("network") String network
    );

    @Query("select t from HarvestDTO t where "
        + "(t.ownerBalance is null "
        + "or t.ownerBalanceUsd is null) "
        + "and t.network = :network "
        + "order by t.blockDate asc")
    List<HarvestDTO> fetchAllWithoutOwnerBalance(
        @Param("network") String network
    );

    @Query("select t from HarvestDTO t where "
        + "t.sharePrice = 0.00000000000000001"
        + "order by t.blockDate asc")
    List<HarvestDTO> fetchAllMigration();

    @Query(nativeQuery = true, value = ""
        + "select * from harvest_tx t where "
        + "lower(t.vault_address) = lower(:vaultAddress) "
        + "and t.block_date < :blockDate "
        + "and t.network = :network "
        + "order by t.block_date desc limit 1")
    HarvestDTO findFirstByVaultAddressAndBlockDateBeforeAndNetworkOrderByBlockDateDesc(
        String vaultAddress, long blockDate, String network);

    @Query(nativeQuery = true, value =
        "select * from harvest_tx t where "
            + "t.block_date > :fromTs "
            + "and t.network = :network "
            + "order by t.block_date")
    List<HarvestDTO> fetchAllFromBlockDate(
        @Param("fromTs") long fromTs,
        @Param("network") String network
    );

    @Query("select t from HarvestDTO t where "
        + "lower(t.vaultAddress) = lower(:vault) "
        + "and t.blockDate between :startTime and :endTime "
        + "and t.network = :network "
        + "order by t.blockDate")
    List<HarvestDTO> findAllByVaultOrderByBlockDate(
        @Param("vault") String vaultAddress,
        @Param("startTime") long startTime,
        @Param("endTime") long endTime,
        @Param("network") String network
    );

    @Query(nativeQuery = true, value = "" +
        "select distinct on (vault_address) * from harvest_tx "
        + "where network = :network "
        + "order by vault_address, block_date desc")
    List<HarvestDTO> fetchLatest(@Param("network") String network);

    @Query("select t from HarvestDTO t where "
        + "t.blockDate > :from "
        + "and t.blockDate <= :to "
        + "and t.network = :network "
        + "order by t.blockDate")
    List<HarvestDTO> fetchAllByPeriod(
        @Param("from") long from,
        @Param("to") long to,
        @Param("network") String network
    );

    List<HarvestDTO> findAllByMethodNameAndBlockDateGreaterThanAndNetworkOrderByBlockDate(
        String methodName, long blockDate, String network
    );

    @Query(nativeQuery = true, value = "select * from ( "
        + "                  select *, "
        + "                         coalesce((select block_date "
        + "                                   from harvest_tx "
        + "                                   where owner = :owner "
        + "                                     and lower(vault_address) = lower(:vault) "
        + "                                     and method_name = 'Withdraw' "
        + "                                     and block_date < :blockDate "
        + "                                     and owner_balance = 0 "
        + "                                   order by block_date desc "
        + "                                   limit 1), 0) last_withdraw_block_date "
        + "                  from harvest_tx "
        + "                  where owner = :owner "
        + "                    and lower(vault_address) = lower(:vault) "
        + "                    and block_date < :blockDate "
        + "                    and network = :network "
        + "                  order by block_date "
        + "              ) t where block_date > last_withdraw_block_date")
    List<HarvestDTO> fetchLatestSinceLastWithdraw(
        @Param("owner") String owner,
        @Param("vault") String vaultAddress,
        @Param("blockDate") long blockDate,
        @Param("network") String network
    );

    @Query("select t from HarvestDTO t where "
        + "t.vaultAddress is null or t.vaultAddress = '' "
        + "or t.lpStat not like '%coin1Address%'")
    List<HarvestDTO> fetchAllWithoutAddresses();

    @Query("select t from HarvestDTO t where "
        + "t.usdAmount >= :minAmount "
        + "and t.network = :network")
    Page<HarvestDTO> fetchPages(
        @Param("minAmount") long minAmount,
        @Param("network") String network,
        Pageable pageable);

    @Query("select t from HarvestDTO t where "
        + "lower(t.vaultAddress) = lower(:vault) "
        + "and t.usdAmount >= :minAmount "
        + "and t.network = :network")
    Page<HarvestDTO> fetchPagesByVault(
        @Param("vault") String vaultAddress,
        @Param("network") String network,
        @Param("minAmount") long minAmount,
        Pageable pageable);

    @Query(nativeQuery = true, value = ""
        + "select t.owner, sum(t.b) balance from "
        + "(select owner, "
        + "        cast(SUBSTRING_INDEX(MAX(CONCAT(block_date, SUBSTRING_INDEX(id, '_', -1), '|', coalesce(owner_balance_usd, 0))), '|', -1) as DOUBLE PRECISION) as b "
        + " from harvest_tx where network = :network "
        + "group by vault_address, owner) t "
        + "where t.b > 50000 " //for optimization
        + "group by t.owner "
        + "order by balance desc")
    List<UserBalance> fetchOwnerBalances(@Param("network") String network);

    @Query("select h.owner from HarvestDTO h "
        + "where h.network = :network "
        + "group by h.owner")
    List<String> fetchUniqueAddressByNetwork(@Param("network") String network);

    List<HarvestDTO> findAllByOwner(String owner);

    interface UserBalance {

        String getOwner();

        double getBalance();
    }
}
