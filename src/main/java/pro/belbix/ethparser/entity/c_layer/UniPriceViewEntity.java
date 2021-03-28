package pro.belbix.ethparser.entity.c_layer;

import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import pro.belbix.ethparser.repositories.c_layer.ViewI;

@Entity
@Immutable
@Data
@Subselect("select "
    + "    event.id               as id, "
    + "    block.number           as block_number, "
    + "    block_hash.hash        as block_hash, "
    + "    event_contract.name    as name, "
    + "    event_contract.address as address, "
    + "    c_tx.id                as tx_id, "
    + "    eth_tx_hash.hash       as tx_hash, "
    + "    tx_func.name           as func_name, "
    + "    log_hash.method_name   as log_name, "
    + "    logs.logs ->> 0        as sender, "
    + "    logs.logs ->> 1        as to_adr, "
    + "    logs.logs ->> 2        as amount0In, "
    + "    logs.logs ->> 3        as amount1In, "
    + "    logs.logs ->> 4        as amount0Out, "
    + "    logs.logs ->> 5        as amount1Out "
    + "from b_contract_events event "
    + " "
    + "         join a_eth_block block on event.block = block.number "
    + "         join a_eth_hash block_hash on block.hash = block_hash.idx "
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
    + "         join b_log_hashes log_hash on logs.topic = log_hash.method_id")
public class UniPriceViewEntity implements ViewI {

  @Id
  private Long id;
  private Long blockNumber;
  private String blockHash;
  private String name;
  private String address;
  private Long txId;
  private String txHash;
  private String funcName;
  private String logName;
  private String sender;
  private String toAdr;
  private String amount0In;
  private String amount1In;
  private String amount0Out;
  private String amount1Out;

}
