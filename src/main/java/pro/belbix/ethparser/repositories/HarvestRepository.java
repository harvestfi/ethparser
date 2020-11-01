package pro.belbix.ethparser.repositories;

import java.math.BigInteger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.model.HarvestDTO;

public interface HarvestRepository extends JpaRepository<HarvestDTO, Long> {

    @Query(nativeQuery = true, value = ""
        + "select (coalesce(deposit.d, 0) - coalesce(withdraw.w, 0)) result from (  "
        + "                  (select SUM(amount) d  "
        + "                  from egv.harvest_tx t  "
        + "                  where t.method_name = 'Deposit'  "
        + "                      and t.vault = :vault) deposit,  "
        + "                  (select SUM(amount) w  "
        + "                  from egv.harvest_tx t  "
        + "                  where t.method_name = 'Withdraw'  "
        + "                    and t.vault = :vault) withdraw  "
        + "              )")
    Double fetchTVL(@Param("vault") String vault);

    HarvestDTO findFirstByOrderByBlockDesc();

}
