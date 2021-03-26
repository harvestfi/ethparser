package pro.belbix.ethparser.repositories.c_layer;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.entity.b_layer.ContractEventEntity;
import pro.belbix.ethparser.views.UniPriceView;

public interface UniPriceViewRepository extends JpaRepository<ContractEventEntity, Long> {

  @Query(nativeQuery = true, value = ""
      + "select  "
      + "       event.id               as id, "
      + "       event_contract.name    as name, "
      + "       event_contract.address as address, "
      + "       c_tx.id                as tx_id, "
      + "       eth_tx_hash.hash       as tx_hash, "
      + "       tx_func.name           as func_name, "
      + "       log_hash.method_name   as log_name, "
      + "       logs.logs ->> 0        as sender, "
      + "       logs.logs ->> 1        as to_adr, "
      + "       logs.logs ->> 2        as amount0In, "
      + "       logs.logs ->> 3        as amount1In, "
      + "       logs.logs ->> 4        as amount0Out, "
      + "       logs.logs ->> 5        as amount1Out "
      + "from b_contract_events event "
      + " "
      + "         join a_eth_block block on event.block = block.number "
      + "         join a_eth_address event_address on event.contract = event_address.idx "
      + "         join eth_contracts event_contract on event_contract.address = event_address.address "
      + " "
      + "         join b_contract_event_to_tx tx_map on event.id = tx_map.event_id "
      + "         join b_contract_txs c_tx on tx_map.tx_id = c_tx.id "
      + " "
      + "         join a_eth_tx eth_tx on c_tx.tx_id = eth_tx.id "
      + "         join a_eth_hash eth_tx_hash on eth_tx.hash = eth_tx_hash.idx "
      + "         join b_func_hashes tx_func on c_tx.func_hash = tx_func.method_id "
      + " "
      + "         join b_contract_logs logs on c_tx.id = logs.contract_tx_id "
      + "         join b_log_hashes log_hash on logs.topic = log_hash.method_id "
      + " "
      + "where event_address.address in :addresses "
      + "  and log_hash.method_name in :logNames "
      + "  and block.number between :startBlock and :endBlock "
      + "order by id, tx_id"
  )
  List<UniPriceView> findByAddressesAndLogNames(
      @Param("addresses") List<String> addresses,
      @Param("logNames") List<String> logNames,
      @Param("startBlock") long startBlock,
      @Param("endBlock") long endBlock,
      Pageable pageable
  );

}
