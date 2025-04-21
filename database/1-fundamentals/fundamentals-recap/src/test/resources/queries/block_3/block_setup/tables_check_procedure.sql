CREATE OR REPLACE FUNCTION has_three_non_empty_tables()
    RETURNS BOOLEAN AS $$
DECLARE
    tbl RECORD;
    dummy INT;
    num_non_empty_tables INT := 0;
BEGIN
    FOR tbl IN
        SELECT table_name
        FROM information_schema.tables
        WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
        LOOP
            BEGIN
                EXECUTE format('SELECT 1 FROM public.%I LIMIT 1', tbl.table_name)
                    INTO dummy;

                IF dummy IS NOT NULL THEN
                    num_non_empty_tables := num_non_empty_tables + 1;
                    EXIT WHEN num_non_empty_tables >= 3;
                END IF;
            EXCEPTION
                WHEN OTHERS THEN
                    CONTINUE;
            END;
        END LOOP;

    RETURN num_non_empty_tables >= 3;
END;
$$ LANGUAGE plpgsql;
