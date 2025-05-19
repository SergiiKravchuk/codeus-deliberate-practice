------------------------------------------------------------------------------------------------------------------------
-- TASK 5: Assign Branch Manager (with Constraints)
------------------------------------------------------------------------------------------------------------------------
-- DESCRIPTION:
-- This procedure assigns a given employee as the manager of a branch.
-- A branch can have only one manager, and a manager can be assigned to only one branch.
-- The procedure must check for and enforce these constraints before performing the assignment.
--
-- PROCEDURE NAME: assign_branch_manager
-- INPUT PARAMETERS:
--   - p_branch_id (INT): The ID of the branch.
--   - p_employee_id (INT): The ID of the employee to assign as manager.
-- OUTPUT PARAMETERS:
--   - none (uses RAISE NOTICE to confirm assignment)
-- TIPS:
--   - Validate that the branch and employee exist.
--   - Ensure the employee is not already managing another branch.
--   - Ensure the branch does not already have a manager.
--   - Update the branch’s manager_id field.
------------------------------------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE assign_branch_manager(
    IN p_branch_id INT,
    IN p_employee_id INT
)
 LANGUAGE plpgsql
 AS $$
 DECLARE
    v_branch_exists BOOLEAN;
    v_employee_exists BOOLEAN;
    v_branch_has_manager BOOLEAN;
    v_employee_is_manager BOOLEAN;
BEGIN
    -- Check branch existence
    SELECT EXISTS (SELECT 1 FROM branches WHERE id = p_branch_id) INTO v_branch_exists;
    IF NOT v_branch_exists THEN
        RAISE EXCEPTION 'Branch % does not exist.', p_branch_id;
    END IF;

    -- Check employee existence
    SELECT EXISTS (SELECT 1 FROM employees WHERE id = p_employee_id) INTO v_employee_exists;
    IF NOT v_employee_exists THEN
        RAISE EXCEPTION 'Employee % does not exist.', p_employee_id;
    END IF;

    -- Check if branch already has a manager
    SELECT manager_id IS NOT NULL FROM branches WHERE id = p_branch_id INTO v_branch_has_manager;
    IF v_branch_has_manager THEN
        RAISE EXCEPTION 'Branch % already has a manager assigned.', p_branch_id;
    END IF;

    -- Check if employee is already managing another branch
    SELECT EXISTS (
        SELECT 1 FROM branches WHERE manager_id = p_employee_id
    ) INTO v_employee_is_manager;
    IF v_employee_is_manager THEN
        RAISE EXCEPTION 'Employee % is already managing another branch.', p_employee_id;
    END IF;

    -- Assign the manager
    UPDATE branches
    SET manager_id = p_employee_id
    WHERE id = p_branch_id;

    RAISE NOTICE 'Employee % assigned as manager to branch %.', p_employee_id, p_branch_id;
END;
$$;
