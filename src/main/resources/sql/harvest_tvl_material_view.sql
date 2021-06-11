drop MATERIALIZED view if exists harvest_tvl_material_view;
create MATERIALIZED view if not exists harvest_tvl_material_view
as
select tx.network                as network,
       tx.id                     as calculate_hash,
       tx.block_date             as calculate_time,
       tx.all_pools_owners_count as last_owners_count,
       tx.all_owners_count       as last_all_owners_count,
--        не понятно откуда брать
       tx.block_date             as last_tvl,
       tx.block_date             as last_price

from harvest_tx tx;

CREATE UNIQUE INDEX ON harvest_tvl_material_view (
                                                  network,
                                                  calculate_hash,
                                                  calculate_time,
                                                  last_owners_count,
                                                  last_tvl,
                                                  last_price
    );

DROP TRIGGER IF EXISTS tg_refresh_harvest_tvl_material_view on harvest_tx;
CREATE TRIGGER tg_refresh_harvest_tvl_material_view
    AFTER INSERT OR UPDATE OR DELETE
    ON harvest_tx
    FOR EACH STATEMENT
EXECUTE PROCEDURE tg_refresh_harvest_tvl_material_view();
