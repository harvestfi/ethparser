ALTER SEQUENCE a_eth_log_id_seq RESTART WITH 999999;
ALTER SEQUENCE a_eth_tx_id_seq RESTART WITH 999999;
ALTER SEQUENCE eth_contracts_id_seq RESTART WITH 999999;
ALTER SEQUENCE eth_pools_id_seq RESTART WITH 999999;
ALTER SEQUENCE eth_token_to_uni_pair_id_seq RESTART WITH 999999;
ALTER SEQUENCE eth_tokens_id_seq RESTART WITH 999999;
ALTER SEQUENCE eth_uni_pairs_id_seq RESTART WITH 999999;
ALTER SEQUENCE eth_vaults_id_seq RESTART WITH 999999;


INSERT INTO public.block_cache (block, block_date, network) VALUES (10770491, 1598900533, 'eth');
INSERT INTO public.block_cache (block, block_date, network) VALUES (11997407, 1615200972, 'eth');
INSERT INTO public.block_cache (block, block_date, network) VALUES (12032570, 1615669977, 'eth');
INSERT INTO public.block_cache (block, block_date, network) VALUES (12063886, 1616087033, 'eth');


INSERT INTO public.deployer_tx (id, block, block_date, confirmed, from_address, gas_limit, gas_price, gas_used, idx, method_name, to_address, type, value, network) VALUES ('0x0d02c4a0bcf2581e4a693bbc1212ddb63589fbe69f4fc0f605f5b93d689002ab', 11920891, 1614184032, 1, '0xf00dd244228f51547f0563e60bca65a30fbf5f7f', 6000000.00, 500.00, 1906591.00, 88, 'CONTRACT_CREATION', null, 'CONTRACT_CREATION', 0.00, 'eth');

INSERT INTO public.events_tx (id, block, block_date, event, hash, info, mint_amount, new_strategy, old_strategy, vault, network) VALUES ('0x03075b3216fabd8846491c45e60d981fac920bc17f4a22a63bdf2b470b4cee3a_54', 11808045, 1612684105, 'StrategyChanged', '0x03075b3216fabd8846491c45e60d981fac920bc17f4a22a63bdf2b470b4cee3a', '{"vaultAddress":"0x859222dd0b249d0ea960f5102dab79b294d6874a","strategyTimeLock":null}', null, '0xce2fa27ad136c6035f60e8cf2ad605d805745972', '0x15ada3630227a33751e986f3e77b0a073f77d17d', 'ONEINCH_ETH_WBTC', 'eth');

INSERT INTO public.harvest_tvl (calculate_hash, calculate_time, last_all_owners_count, last_owners_count, last_price, last_tvl, network) VALUES ('0x00018f953339fbb7c708698f4a69c030ff4464114beeda7bfc83c0c548747add_338', 1603472475, 10094, 1853, 202.5659866011, 1090496226.6724818, 'eth');

INSERT INTO public.prices (id, block, block_date, buy, lp_token0pooled, lp_token1pooled, lp_total_supply, other_token, other_token_amount, price, source, token, token_amount, network) VALUES ('0x00011fb0780aa16e7f8ce785f2a038b31c25b344bbab3def44fb3f99766b790b_312', 11918804, 1614156184, 0, 13424385.204148998, 124121.509756646, 1126412.0303373176, 'ETH', 11.4869787796, 0.0092190913, 'SUSHI_LP_SUSHI_ETH', 'SUSHI', 1245.9990323311, 'eth');

INSERT INTO public.rewards (id, apy, block, block_date, farm_balance, period_finish, reward, tvl, vault, weekly_apy, network) VALUES ('0x00e2884a39f809b8c609b48185c66447fa12af01da3d6e5dd514665c035e79ff_44', 3229.0658293262, 11732675, 1611681468, 0, 1612286268, 376.3074822001, 702650, 'SUSHI_MIS_USDT', 2003.6098570761, 'eth');
INSERT INTO public.rewards (id, apy, block, block_date, farm_balance, period_finish, reward, tvl, vault, weekly_apy, network) VALUES ('0x551e84bc0ad7eae164b9ec06cba6aa5a833c981175973223c9380f5947fae26c_8', 5.2523265871, 11688056, 1611089943, 0, 1611694743, 151.2405093, 17979908, 'USDC', 5.349910738, 'eth');


INSERT INTO public.transfers (id, balance_owner, balance_recipient, block, block_date, method_name, name, owner, price, profit, profit_usd, recipient, type, value, network) VALUES ('0x000149920094108332674ef72fa5a424d936396c33e0e7829a0daf83a86a3413_244', 0, 47526.1499968688, 11104687, 1603352177, 'swapExactTokensForTokens', 'FARM', '0xce2a3294f800b1bf9a907db3c7e377cf9486a456', 265.8621683771, 4.5081921118, 1198.5577303033, '0x514906fc121c7878424a5c928cad1852cc545892', 'LP_SELL', 14.6055082658, 'eth');

INSERT INTO public.uni_tx (id, amount, block, block_date, coin, confirmed, hash, last_gas, last_price, lp, method_name, other_amount, other_coin, owner, owner_balance, owner_balance_usd, owner_count, ps_income_usd, ps_week_apy, type) VALUES ('0x000674a8364b101413df8af2f761f7bc240c4194e53d8b56fd19462263bb3f08_34', 5.0353566756, 10969556.00, 1601547208, 'FARM', true, '0x000674a8364b101413df8af2f761f7bc240c4194e53d8b56fd19462263bb3f08', null, 85.8172917711, null, null, 432.120673, 'USDC', '0xaa4ba008dc503733c2999089f9cf09ed10755f5e', -81.4642944538, -6991.0451260717, 1477, null, null, 'SELL');

