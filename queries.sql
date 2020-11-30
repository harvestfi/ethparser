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
