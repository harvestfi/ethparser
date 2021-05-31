-- materialized view
--запрос гавно, новый надо
drop view if exists harvest_tvl_view;
create view harvest_tvl_view as
select
    MAX(agg.calculate_hash) calculate_hash,
    MAX(agg.network) network,
    MAX(agg.calculate_time) calculate_time,
    MAX(agg.last_tvl) last_tvl,
    MAX(agg.last_owners_count) last_owners_count,
    MAX(agg.last_all_owners_count) last_all_owners_count,
    MAX(agg.last_price) last_price
from (
         select
             t.calculate_hash calculate_hash,
             t.network network,
             t.calculate_time calculate_time,
             t.last_tvl last_tvl,
             t.last_owners_count last_owners_count,
             t.last_all_owners_count last_all_owners_count,
             t.last_price last_price,
             to_char(date(to_timestamp(t.calculate_time)), 'YYYY-MM-DD HH') grp
         from harvest_tvl t
--          //посмотреть подробней
         where t.calculate_time between :startTime and :endTime
           and t.network = :network
     ) agg
group by agg.grp
order by calculate_time