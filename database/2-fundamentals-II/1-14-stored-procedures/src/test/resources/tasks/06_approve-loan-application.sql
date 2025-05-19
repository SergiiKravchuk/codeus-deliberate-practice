-- TASK 6: Approve Loan Application
------------------------------------------------------------------------------------------------------------------------
-- DESCRIPTION:
-- In our banking system, branch managers are responsible for approving loan applications.
-- This procedure will mark a loan as approved and record the manager who approved it.
-- It must ensure the following:
--   - The loan must exist and currently have a status of 'pending'.
--   - The branch must exist and must have a manager assigned.
--   - The provided manager must match the branch’s assigned manager.
--
-- PROCEDURE NAME: approve_loan_application
-- INPUT PARAMETERS:
--   - p_loan_id (INT): The ID of the loan to approve.
--   - p_manager_id (INT): The ID of the manager approving the loan.
-- OUTPUT:
--   - none (RAISE NOTICE on success)
--
-- TIPS:
--   - Use CREATE OR REPLACE
--   - Validate all conditions before updating the loan.
--   - Raise meaningful exceptions when validations fail.
------------------------------------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE approve_loan_application(
    IN p_loan_id INT,
    IN p_manager_id INT
)
 LANGUAGE plpgsql
 AS $$
 DECLARE
    v_branch_id INT;
    v_current_status TEXT;
    v_branch_manager INT;
BEGIN
    -- Validate loan existence and status
    SELECT branch_id, status INTO v_branch_id, v_current_status
    FROM loans
    WHERE id = p_loan_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Loan % does not exist', p_loan_id;
    ELSIF v_current_status != 'pending' THEN
        RAISE EXCEPTION 'Loan % is not pending (current status: %)', p_loan_id, v_current_status;
    END IF;

    -- Validate branch and manager match
    SELECT manager_id INTO v_branch_manager
    FROM branches
    WHERE id = v_branch_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Branch % associated with loan % does not exist', v_branch_id, p_loan_id;
    ELSIF v_branch_manager IS NULL THEN
        RAISE EXCEPTION 'Branch % has no assigned manager', v_branch_id;
    ELSIF v_branch_manager != p_manager_id THEN
        RAISE EXCEPTION 'Employee % is not the manager of branch %', p_manager_id, v_branch_id;
    END IF;

    -- Approve the loan
    UPDATE loans
    SET status = 'approved',
        approved_by = p_manager_id
    WHERE id = p_loan_id;

    RAISE NOTICE 'Loan % approved by manager %', p_loan_id, p_manager_id;
END;
$$;

-- Sample usage:
-- CALL approve_loan_application(1, 10);
