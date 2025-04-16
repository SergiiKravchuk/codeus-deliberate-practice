-- Migration script to transfer data from customers_denormalized_order to customers_normalized_order
-- Adds reliability_score based on specified distribution:
-- 10% vip, 20% most reliable, 30% reliable, 20% probably reliable, 20% not reliable

-- First, ensure the destination table is empty
TRUNCATE TABLE customers_normalized_order RESTART IDENTITY;

-- Get the total count of customers
DO $$
    DECLARE
        total_count INT;
        vip_threshold INT;
        most_reliable_threshold INT;
        reliable_threshold INT;
        probably_reliable_threshold INT;
    BEGIN
        -- Get the total count of customers
        SELECT COUNT(*) INTO total_count FROM customers_denormalized_order;

        -- Calculate the threshold values for each category
        vip_threshold := FLOOR(total_count * 0.1);
        most_reliable_threshold := FLOOR(total_count * 0.3); -- 10% + 20%
        reliable_threshold := FLOOR(total_count * 0.6);      -- 10% + 20% + 30%
        probably_reliable_threshold := FLOOR(total_count * 0.8); -- 10% + 20% + 30% + 20%

        -- Insert all records with appropriate reliability scores
        INSERT INTO customers_normalized_order (customer_name, customer_contact, reliability_score)
        SELECT
            c.customer_name,
            c.customer_contact,
            CASE
                WHEN ntile <= vip_threshold THEN 'vip'
                WHEN ntile <= most_reliable_threshold THEN 'most reliable'
                WHEN ntile <= reliable_threshold THEN 'reliable'
                WHEN ntile <= probably_reliable_threshold THEN 'probably reliable'
                ELSE 'not reliable'
                END AS reliability_score
        FROM (
                 SELECT
                     customer_name,
                     customer_contact,
                     ROW_NUMBER() OVER () AS ntile
                 FROM
                     customers_denormalized_order
             ) c;

        -- Inform about the operation completion
        RAISE NOTICE 'Migration completed. % records transferred with reliability scores.', total_count;
        RAISE NOTICE 'Distribution: % vip, % most reliable, % reliable, % probably reliable, % not reliable',
            vip_threshold,
            most_reliable_threshold - vip_threshold,
            reliable_threshold - most_reliable_threshold,
            probably_reliable_threshold - reliable_threshold,
            total_count - probably_reliable_threshold;
    END $$;

-- Verify the migration
SELECT
    reliability_score,
    COUNT(*) AS count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM customers_normalized_order), 1) AS percentage
FROM
    customers_normalized_order
GROUP BY
    reliability_score
ORDER BY
    CASE reliability_score
        WHEN 'vip' THEN 1
        WHEN 'most reliable' THEN 2
        WHEN 'reliable' THEN 3
        WHEN 'probably reliable' THEN 4
        WHEN 'not reliable' THEN 5
        END;