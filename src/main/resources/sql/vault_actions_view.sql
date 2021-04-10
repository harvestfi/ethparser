drop view if exists vault_actions_view;
create view vault_actions_view as
select evt.id                 as id,
       log_address.address    as address,
       block.number           as block_number,
       block_hash.hash        as block_hash,
       event_contract.name    as source_name,
       event_contract.network as network,
       c_tx.id                as tx_id,
       eth_tx_hash.hash       as tx_hash,
       tx_func.name           as func_name,
       logs.log_idx           as log_id,
       logs.logs ->> 0        as from_adr,
       logs.logs ->> 1        as to_adr,
       (logs.logs ->> 2) :: double precision /
       (10 ^ 18)                 as ftoken_amount,
       case
           when (logs.logs ->> 0 = '0x0000000000000000000000000000000000000000') then
               'Deposit'
           when (logs.logs ->> 1 = '0x0000000000000000000000000000000000000000') then
               'Withdraw'
           else
               'Unknown'
           end                   as op_type,
       (shared_price.value ->> 0):: double precision /
       (10 ^ 18)                 as shared_price,
       (ftoken_total_supply.value ->> 0):: double precision /
       (10 ^ 18)                 as ftoken_total_supply,
       ((shared_price.value ->> 0):: double precision /
        (10 ^ 18)) * ((ftoken_total_supply.value ->> 0):: double precision /
                      (10 ^ 18)) as tvl,
       underlying.value ->> 0    as underlying,
       underlying_contract.name  as underlying_name,
       underlying_contract.type  as underlying_type
from b_contract_events evt
         join a_eth_block block on evt.block = block.number
         join a_eth_hash block_hash on block.hash = block_hash.idx
         join a_eth_address event_address on evt.contract = event_address.idx
         join eth_contracts event_contract on event_contract.address = event_address.address

         join b_contract_event_to_tx tx_map on evt.id = tx_map.event_id
         join b_contract_txs c_tx on tx_map.tx_id = c_tx.id

         join a_eth_tx eth_tx on c_tx.tx_id = eth_tx.id
         join a_eth_hash eth_tx_hash on eth_tx.hash = eth_tx_hash.idx
         join b_func_hashes tx_func on c_tx.func_hash = tx_func.method_id

         join b_contract_logs logs on c_tx.id = logs.contract_tx_id
         join b_log_hashes log_hash on logs.topic = log_hash.method_id
         join a_eth_address log_address on log_address.idx = logs.address
         join eth_contracts log_address_contract
              on log_address_contract.address = log_address.address

         join b_contract_states shared_price on evt.id = shared_price.contract_event_id and
                                                shared_price.name = 'getPricePerFullShare'
         join b_contract_states ftoken_total_supply
              on evt.id = ftoken_total_supply.contract_event_id and
                 ftoken_total_supply.name = 'totalSupply'

         join b_contract_states underlying
              on evt.id = underlying.contract_event_id and
                 underlying.name = 'underlying'
         join eth_contracts underlying_contract
              on underlying_contract.address = underlying.value ->> 0

         join eth_vaults vault on log_address_contract.id = vault.contract
where log_hash.method_name = 'Transfer';
