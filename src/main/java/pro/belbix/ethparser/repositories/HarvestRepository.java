package pro.belbix.ethparser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.dto.HarvestDTO;

public interface HarvestRepository extends JpaRepository<HarvestDTO, String> {

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

    @Query(nativeQuery = true, value = ""
        + "select count(*) from ("
        + "select t.owner from harvest_tx t where t.vault = :vault and t.block_date <= :block_date group by t.owner) tt"
    )
    Integer fetchOwnerCount(@Param("vault") String vault, @Param("block_date") long blockDate);
    
    @Query(nativeQuery = true, value = "" 
        + "select count(t.owner) from ( "
        + "select deposit.owner owner, deposit.d_amnt, withdraw.w_amnt, (deposit.d_amnt - withdraw.w_amnt) result from "
        + "    (select owner, sum(amount) d_amnt "
        + "     from harvest_tx "
        + "     where method_name = 'Deposit' "
        + "       and (vault = :vault or vault = :oldVault /*previous version*/) "
        + "       and block_date <= :block_date "
        + "     group by owner) deposit "
        + "        left join "
        + "    (select owner, sum(amount) w_amnt from harvest_tx "
        + "     where method_name = 'Withdraw' "
        + "       and (vault = :vault or vault = :oldVault /*previous version*/) "
        + "       and block_date <= :block_date "
        + "     group by owner) withdraw ON deposit.owner = withdraw.owner "
        + "    ) t "
        + "where t.result is null or t.result > 0")
    Integer fetchActualOwnerCount(@Param("vault") String vault,
                                  @Param("oldVault") String oldVault,
                                  @Param("block_date") long blockDate);

    HarvestDTO findFirstByOrderByBlockDesc();

    HarvestDTO findFirstByVaultAndBlockDateLessThanEqualAndIdNotOrderByBlockDateDesc(String vault, long date,
                                                                                     String id);

}
