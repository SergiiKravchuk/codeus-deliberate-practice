-- ====================================================================================================================
-- TASK 5: Calculate Interest for a Given Balance
-- File: 05_calculate_interest_for_balance.sql
-- ====================================================================================================================
-- Objective: Create a function that takes a balance amount and calculates interest
--            based on a tiered structure. This function performs calculations only.
--
-- Function Signature to implement:
--   Name: calculate_interest_for_balance
--   Input Parameter: p_balance NUMERIC
--   Return Type: NUMERIC
--
-- Logic:
-- 1. Declare NUMERIC variables `v_rate` and `v_interest`.
-- 2. Input Validation: If `p_balance` is `NULL` or less than `0`, return `0.00`.
-- 3. Tiered interest logic (IF/ELSIF/ELSE or CASE):
--    - Balance < 1000.00: Rate = 0.005 (0.5%)
--    - 1000.00 <= Balance < 5000.00: Rate = 0.010 (1.0%)
--    - 5000.00 <= Balance < 20000.00: Rate = 0.015 (1.5%)
--    - Balance >= 20000.00: Rate = 0.020 (2.0%)
-- 4. Calculate `v_interest = p_balance * v_rate`.
-- 5. Return `v_interest`.
--
-- Example Usage (after creation, for understanding):
--   SELECT calculate_interest_for_balance(500);
-- ====================================================================================================================

-- TODO: Implement the complete function definition for 'calculate_interest_for_balance' below.

CREATE OR REPLACE FUNCTION calculate_interest_for_balance(p_balance NUMERIC)
RETURNS NUMERIC AS $$
DECLARE
    v_rate NUMERIC;
    v_interest NUMERIC;
BEGIN
    IF p_balance IS NULL OR p_balance < 0 THEN
        RETURN 0.00;
    END IF;

    IF p_balance < 1000.00 THEN
        v_rate := 0.005; -- 0.5%
    ELSIF p_balance >= 1000.00 AND p_balance < 5000.00 THEN
        v_rate := 0.010; -- 1.0%
    ELSIF p_balance >= 5000.00 AND p_balance < 20000.00 THEN
        v_rate := 0.015; -- 1.5%
    ELSE
        v_rate := 0.020; -- 2.0%
    END IF;

    v_interest := p_balance * v_rate;

    RETURN v_interest;
END;
$$ LANGUAGE plpgsql;
