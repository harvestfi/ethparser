drop view if exists uni_prices_view;
create view uni_prices_view as
select evt.id                     as id,
       log_address.address        as address,
       block.number               as block_number,
       block_hash.hash            as block_hash,
       event_contract.name        as source_name,
       c_tx.id                    as tx_id,
       eth_tx_hash.hash           as tx_hash,
       tx_func.name               as func_name,
       logs.logs ->> 0            as sender,
       logs.logs ->> 1            as to_adr,
       logs.logs ->> 2            as amount0In,
       logs.logs ->> 3            as amount1In,
       logs.logs ->> 4            as amount0Out,
       logs.logs ->> 5            as amount1Out,
       (lp_total_supply.value ->> 0) :: double precision /
       (10 ^ uni_pair.decimals)   as lp_total_supply,
       (lp_reserves.value ->> 0) :: double precision /
       (10 ^ token0_token.decimals)    as lp_token_0_pooled,
       (lp_reserves.value ->> 1) :: double precision /
       (10 ^ token1_token.decimals)    as lp_token_1_pooled,

       case
           when (uni_pair.token0_id = key_token_contract.id) then
               token0_contract.name
           else
               token1_contract.name
           end                    as key_token_name,

       case
           when (uni_pair.token0_id = key_token_contract.id) then
               token0_contract.address
           else
               token1_contract.address
           end                    as key_token_address,

       case
           when (uni_pair.token0_id = key_token_contract.id) then
               token1_contract.name
           else
               token0_contract.name
           end                    as other_token_name,

       case
           when (uni_pair.token0_id = key_token_contract.id) then
               token1_contract.address
           else
               token0_contract.address
           end                    as other_token_address,

       case
           when ((uni_pair.token0_id = key_token_contract.id and logs.logs ->> 5 = '0')
               or (uni_pair.token0_id != key_token_contract.id and logs.logs ->> 4 = '0')) then
               1
           when ((uni_pair.token0_id = key_token_contract.id and logs.logs ->> 4 = '0')
               or (uni_pair.token0_id != key_token_contract.id and logs.logs ->> 5 = '0')) then
               0
           end                    as is_buy,
       case
           /** key token is first */
           when (uni_pair.token0_id = key_token_contract.id) then
               case
                   /** is buy */
                   when ((uni_pair.token0_id = key_token_contract.id and logs.logs ->> 5 = '0')
                       or (uni_pair.token0_id != key_token_contract.id and logs.logs ->> 4 = '0'))
                       then
                           (logs.logs ->> 4) :: double precision / (10 ^ token0_token.decimals)
                   /** is sell */
                   when ((uni_pair.token0_id = key_token_contract.id and logs.logs ->> 4 = '0')
                       or (uni_pair.token0_id != key_token_contract.id and logs.logs ->> 5 = '0'))
                       then
                           (logs.logs ->> 2) :: double precision / (10 ^ token0_token.decimals)
                   end
           /** key token is second */
           else
               case
                   /** is buy */
                   when ((uni_pair.token0_id = key_token_contract.id and logs.logs ->> 5 = '0')
                       or (uni_pair.token0_id != key_token_contract.id and logs.logs ->> 4 = '0'))
                       then
                           (logs.logs ->> 5) :: double precision / (10 ^ token1_token.decimals)
                   /** is sell */
                   when ((uni_pair.token0_id = key_token_contract.id and logs.logs ->> 4 = '0')
                       or (uni_pair.token0_id != key_token_contract.id and logs.logs ->> 5 = '0'))
                       then
                           (logs.logs ->> 3) :: double precision / (10 ^ token1_token.decimals)
                   end
           end                    as key_token_amount,
       case
           /** key token is first */
           when (uni_pair.token0_id = key_token_contract.id) then
               case
                   /** is buy */
                   when ((uni_pair.token0_id = key_token_contract.id and logs.logs ->> 5 = '0')
                       or (uni_pair.token0_id != key_token_contract.id and logs.logs ->> 4 = '0'))
                       then
                           (logs.logs ->> 3) :: double precision / (10 ^ token1_token.decimals)
                   /** is sell */
                   when ((uni_pair.token0_id = key_token_contract.id and logs.logs ->> 4 = '0')
                       or (uni_pair.token0_id != key_token_contract.id and logs.logs ->> 5 = '0'))
                       then
                           (logs.logs ->> 5) :: double precision / (10 ^ token1_token.decimals)
                   end
           /** key token is second */
           else
               case
                   /** is buy */
                   when ((uni_pair.token0_id = key_token_contract.id and logs.logs ->> 5 = '0')
                       or (uni_pair.token0_id != key_token_contract.id and logs.logs ->> 4 = '0'))
                       then
                           (logs.logs ->> 2) :: double precision / (10 ^ token0_token.decimals)
                   /** is sell */
                   when ((uni_pair.token0_id = key_token_contract.id and logs.logs ->> 4 = '0')
                       or (uni_pair.token0_id != key_token_contract.id and logs.logs ->> 5 = '0'))
                       then
                           (logs.logs ->> 4) :: double precision / (10 ^ token0_token.decimals)
                   end
           end                    as other_token_amount

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
         join eth_contracts log_address_contract on log_address_contract.address = log_address.address

         join eth_uni_pairs uni_pair on log_address_contract.id = uni_pair.contract

         join eth_tokens key_token on uni_pair.key_token = key_token.id
         join eth_contracts key_token_contract on key_token.contract = key_token_contract.id

         join eth_contracts token0_contract on token0_contract.id = uni_pair.token0_id
         join eth_tokens token0_token on token0_token.contract = token0_contract.id

         join eth_contracts token1_contract on token1_contract.id = uni_pair.token1_id
         join eth_tokens token1_token on token1_token.contract = token1_contract.id

         join b_contract_states lp_total_supply
              on lp_total_supply.contract_event_id = evt.id and lp_total_supply.name = 'totalSupply'

         join b_contract_states lp_reserves
              on lp_reserves.contract_event_id = evt.id and lp_reserves.name = 'getReserves'
where log_hash.method_name = 'Swap';
