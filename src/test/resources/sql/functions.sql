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
