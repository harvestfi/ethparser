
DELETE FROM eth_vaults WHERE contract in (select id from eth_contracts WHERE network='matic');
DELETE FROM eth_pools WHERE contract in (select id from eth_contracts WHERE network='matic');
DELETE FROM eth_token_to_uni_pair WHERE token_id in (select id from eth_tokens WHERE contract in (select id from eth_contracts WHERE network='matic'));
DELETE FROM eth_uni_pairs WHERE contract in (select id from eth_contracts WHERE network='matic');
DELETE FROM eth_tokens WHERE contract in (select id from eth_contracts WHERE network='matic');
DELETE FROM eth_strategies WHERE contract in (select id from eth_contracts WHERE network='matic');

DELETE FROM log_last WHERE network='matic';
DELETE FROM transaction_last WHERE network='matic';
DELETE FROM transfers WHERE network='matic';
DELETE FROM strat_info WHERE network='matic';
DELETE FROM rewards WHERE network='matic';
DELETE FROM prices WHERE network='matic';
DELETE FROM harvest_tx WHERE network='matic';
DELETE FROM harvest_tvl WHERE network='matic';
DELETE FROM hard_work WHERE network='matic';
DELETE FROM events_tx WHERE network='matic';
DELETE FROM error_parse WHERE network='matic';
DELETE FROM block_cache WHERE network='matic';

DELETE FROM deployer_tx WHERE network='matic';
DELETE FROM eth_contract_source_codes WHERE network='matic';
DELETE FROM eth_contracts WHERE network='matic';
