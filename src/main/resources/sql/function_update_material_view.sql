CREATE OR REPLACE FUNCTION tg_refresh_harvest_tvl_material_view()
    RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    NOTIFY refresh_mv, 'harvest_tvl_material_view';
    RETURN NULL;
END;
$$;