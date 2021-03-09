
create or replace function substring_index(str text, delim text, count integer DEFAULT 1, OUT substring_index text) returns text
              immutable
              cost 5
              language plpgsql
              as
              '
              BEGIN
                  IF count > 0 THEN
                      substring_index = array_to_string((string_to_array(str, delim))[:count], delim);
                  ELSE
                      DECLARE
                          _array TEXT[];
                      BEGIN
                          _array = string_to_array(str, delim);
                          substring_index = array_to_string(_array[array_length(_array, 1) + count + 1:], delim);
                      END;
                  END IF;
              END;
              ';


INSERT INTO public.block_cache (block, block_date) VALUES (10770491, 1598900533);
INSERT INTO public.block_cache (block, block_date) VALUES (11997407, 1615200972);

INSERT INTO public.deployer_tx (id, block, block_date, confirmed, from_address, gas_limit, gas_price, gas_used, idx, method_name, to_address, type, value) VALUES ('0x0d02c4a0bcf2581e4a693bbc1212ddb63589fbe69f4fc0f605f5b93d689002ab', 11920891, 1614184032, 1, '0xf00dd244228f51547f0563e60bca65a30fbf5f7f', 6000000.00, 500.00, 1906591.00, 88, 'CONTRACT_CREATION', null, 'CONTRACT_CREATION', 0.00);

INSERT INTO public.events_tx (id, block, block_date, event, hash, info, mint_amount, new_strategy, old_strategy, vault) VALUES ('0x03075b3216fabd8846491c45e60d981fac920bc17f4a22a63bdf2b470b4cee3a_54', 11808045, 1612684105, 'StrategyChanged', '0x03075b3216fabd8846491c45e60d981fac920bc17f4a22a63bdf2b470b4cee3a', '{"vaultAddress":"0x859222dd0b249d0ea960f5102dab79b294d6874a","strategyTimeLock":null}', null, '0xce2fa27ad136c6035f60e8cf2ad605d805745972', '0x15ada3630227a33751e986f3e77b0a073f77d17d', 'ONEINCH_ETH_WBTC');

INSERT INTO public.hard_work (id, all_profit, apr, block, block_date, calls_quantity, eth_price, farm_buyback, farm_buyback_eth, farm_buyback_sum, farm_price, fee, fee_eth, full_reward_usd, full_reward_usd_total, gas_used, idle_time, invested, investment_target, perc, period_of_work, pool_users, ps_apr, ps_period_of_work, ps_tvl_usd, saved_gas_fees, saved_gas_fees_sum, share_change, tvl, vault, weekly_all_profit, weekly_average_tvl, weekly_profit) VALUES ('0x005b08927f1eb926627f3c8657097a7e7e2e396b0c6c34cdbf97b8c71dc81519_271', 8460929.369376468, 4.5641989965, 11525515, 1608935736, 16, 619.454584242, 0.3175105965, 0.1720732995, 20841.6363382041, 100.7131056833, 38.8396475683, 0.06269975, 106.5915942138, 2218.3249821036, 1253995, 51374, 98.8530533139, 95, 0.010650836553387565, 0, 16, 152.9968876412289, 2131271, 24565297.023992993, 991.1273347871, 16664.8604752124, 0.0001065614, 700547, 'CRV_HUSD', 944273.579197405, 720802.7619047619, 1379.8085182564998);
INSERT INTO public.hard_work (id, all_profit, apr, block, block_date, calls_quantity, eth_price, farm_buyback, farm_buyback_eth, farm_buyback_sum, farm_price, fee, fee_eth, full_reward_usd, full_reward_usd_total, gas_used, idle_time, invested, investment_target, perc, period_of_work, pool_users, ps_apr, ps_period_of_work, ps_tvl_usd, saved_gas_fees, saved_gas_fees_sum, share_change, tvl, vault, weekly_all_profit, weekly_average_tvl, weekly_profit) VALUES ('0x094497bda8267323a0ca85aec1054cd5fcf150a7430ce2617cec9deed7b4d2df_135', 8316379.004871397, 7.2192214448, 11512832, 1608766232, 91, 589.1218708339, 17.2066761129, 9.8464694776, 20402.488242605592, 101.1369740735, 487.5642561243, 0.827611875, 5800.7705197345, 666565.6961934454, 2206965, 19787, 99.8496285373, 95, 0.008631851996356885, 0, 434, 152.59735243864213, 2131271, 24208831.123837505, 25567.8891941907, 1894554.304983131, 0.000104, 47041346, 'USDC', 1149265.3355017968, 50945709.5559322, 244702.1605154856);


INSERT INTO public.harvest_tvl (calculate_hash, calculate_time, last_all_owners_count, last_owners_count, last_price, last_tvl) VALUES ('0x00018f953339fbb7c708698f4a69c030ff4464114beeda7bfc83c0c548747add_338', 1603472475, 10094, 1853, 202.5659866011, 1090496226.6724818);

INSERT INTO public.harvest_tx (id, all_owners_count, all_pools_owners_count, amount, amount_in, block, block_date, confirmed, hash, last_all_usd_tvl, last_gas, last_tvl, last_usd_tvl, lp_stat, method_name, migrated, owner, owner_balance, owner_balance_usd, owner_count, prices, profit, profit_usd, share_price, total_amount, underlying_price, usd_amount, vault) VALUES ('0x023269168a27cebe1b191d3410859cc89ddcd311befb61ff547560c52f36def5_230', 14976, 1078, 1930559.358881, null, 11599044, 1609909859, 1, '0x023269168a27cebe1b191d3410859cc89ddcd311befb61ff547560c52f36def5', 404721180.1705768, 64.000001459, 16016937.99809832, 16016938, null, 'Deposit', false, '0xab5531dbbbc3188882cf589b2c89776a4e587ee9', 1644281.332313, 1644281.332313, 277, '{"btc":35460.0535165254,"eth":1122.329184785108,"dpi":161.20508359401418,"grain":0.0502301282531576}', null, null, 0.851712, null, null, 1644280, 'USDC');

INSERT INTO public.income (id, amount, amount_sum, amount_sum_usd, amount_usd, perc, ps_tvl, ps_tvl_usd, timestamp, week_perc) VALUES ('0x006243bad9ae1f6a6264da0c1dcf3249d3001b567faa9fdec1d2e63ad724c0aa_280', 5.4943178091, 5433.8931391715, 656315.3479700004, 885.349948, 0.0052103424, 105450.2264377116, 16408582.965265011, 1602771011, 0.7976684758);

INSERT INTO public.prices (id, block, block_date, buy, lp_token0pooled, lp_token1pooled, lp_total_supply, other_token, other_token_amount, price, source, token, token_amount) VALUES ('0x00011fb0780aa16e7f8ce785f2a038b31c25b344bbab3def44fb3f99766b790b_312', 11918804, 1614156184, 0, 13424385.204148998, 124121.509756646, 1126412.0303373176, 'ETH', 11.4869787796, 0.0092190913, 'SUSHI_LP_SUSHI_ETH', 'SUSHI', 1245.9990323311);

INSERT INTO public.rewards (id, apy, block, block_date, farm_balance, period_finish, reward, tvl, vault, weekly_apy) VALUES ('0x00e2884a39f809b8c609b48185c66447fa12af01da3d6e5dd514665c035e79ff_44', 3229.0658293262, 11732675, 1611681468, 0, 1612286268, 376.3074822001, 702650, 'SUSHI_MIS_USDT', 2003.6098570761);
INSERT INTO public.rewards (id, apy, block, block_date, farm_balance, period_finish, reward, tvl, vault, weekly_apy) VALUES ('0x551e84bc0ad7eae164b9ec06cba6aa5a833c981175973223c9380f5947fae26c_8', 5.2523265871, 11688056, 1611089943, 0, 1611694743, 151.2405093, 17979908, 'USDC', 5.349910738);


INSERT INTO public.transfers (id, balance_owner, balance_recipient, block, block_date, method_name, name, owner, price, profit, profit_usd, recipient, type, value) VALUES ('0x000149920094108332674ef72fa5a424d936396c33e0e7829a0daf83a86a3413_244', 0, 47526.1499968688, 11104687, 1603352177, 'swapExactTokensForTokens', 'FARM', '0xce2a3294f800b1bf9a907db3c7e377cf9486a456', 265.8621683771, 4.5081921118, 1198.5577303033, '0x514906fc121c7878424a5c928cad1852cc545892', 'LP_SELL', 14.6055082658);

INSERT INTO public.uni_tx (id, amount, block, block_date, coin, confirmed, hash, last_gas, last_price, lp, method_name, other_amount, other_coin, owner, owner_balance, owner_balance_usd, owner_count, ps_income_usd, ps_week_apy, type) VALUES ('0x000674a8364b101413df8af2f761f7bc240c4194e53d8b56fd19462263bb3f08_34', 5.0353566756, 10969556.00, 1601547208, 'FARM', true, '0x000674a8364b101413df8af2f761f7bc240c4194e53d8b56fd19462263bb3f08', null, 85.8172917711, null, null, 432.120673, 'USDC', '0xaa4ba008dc503733c2999089f9cf09ed10755f5e', -81.4642944538, -6991.0451260717, 1477, null, null, 'SELL');

