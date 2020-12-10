-- HOLDERS---------------
select * from (
                  select deposit.owner owner, deposit.d_amnt, withdraw.w_amnt, (deposit.d_amnt - withdraw.w_amnt) result from
                      (select owner, sum(usd_amount) d_amnt
                       from harvest_tx
                       where method_name = 'Deposit'
                         and vault = 'CRVRENWBTC'
                       group by owner) deposit
                          left join
                      (select owner, sum(usd_amount) w_amnt from harvest_tx
                       where method_name = 'Withdraw'
                         and vault = 'CRVRENWBTC'
                       group by owner) withdraw ON deposit.owner = withdraw.owner
              ) t
# where t.result < 0
#    or t.result > 0
order by t.result desc;


-- WALLET HISTORY---------------------
select
       round(amount, 0) FARM,
       type,
       round(other_amount, 0) USDC,
       round(last_price, 0) for_price,
       FROM_UNIXTIME(block_date) date,
       hash
from uni_tx
where owner = '0xc54bd1f466f2f4f36de59f4024e86885386d6f1b'
order by block_date desc;

-- WHO SELL FARM---------------------
# select buy.owner, sum(buy.amount) sum_buy, sum(sell.amount) sum_sell, sum(sell.amount - coalesce(buy.amount, 0)) sum
select sell_owner owner, sum_sell, sum_buy, sum(sum_sell - coalesce(sum_buy, 0)) sum from (
                  select *
                  from
                           (select owner sell_owner, sum(amount) sum_sell
                            from uni_tx
                            where type = 'SELL'
#                               and block_date > 1606521600
                            group by owner) sell
                               left join
                               (select owner buy_owner, sum(amount) sum_buy
                                from uni_tx
                                where type = 'BUY'
#                                   and block_date > 1606521600
                                group by owner) buy on sell.sell_owner = buy.buy_owner

              ) t group by sell_owner, sum_sell, sum_buy
order by sum desc;

-- income per day ---------------------
select avg(ps_income) from (
                               select FROM_UNIXTIME(FLOOR(MIN(block_date) / 86400) * 86400) date,
                                      round(sum((share_change_usd / 0.7) * 0.3), 0)         ps_income
                               from hard_work
                               GROUP BY FLOOR(block_date / 86400)
                               order by date desc
                           ) t;


-- last hard works ---------------------
select
    FROM_UNIXTIME(block_date) date,
       round(share_change_usd, 0) vault_income,
       round((share_change_usd / 0.7) * 0.3, 0) ps_income,
       vault
from hard_work
order by block_date desc;

-- UNIQUE USERS ------------
select count(owner) from (
                             select owner
                             from harvest_tx
                             group by owner
                         ) t;
