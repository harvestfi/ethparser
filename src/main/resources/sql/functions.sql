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
                substring_index =
                        array_to_string(_array[array_length(_array, 1) + count + 1:], delim);
            END;
        END IF;
    END;
';

create or replace function get_usd_price(_address text, _block integer, OUT price double precision)
    returns double precision
    immutable
    cost 5
    language plpgsql
as
'
    declare
        contract_type integer;
    BEGIN
        if lower(_address) = ''0xe9e7cea3dedca5984780bafc599bd69add087d56''
            or lower(_address) = ''0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48'' then
            price = 1.0;
            return;
        end if;

        select type
        into contract_type
        from eth_contracts c
        where c.address = _address;

        IF contract_type = 2 THEN
            price = 1.0;
        ELSE
            price = 1.0;
        END IF;
    END;
';
