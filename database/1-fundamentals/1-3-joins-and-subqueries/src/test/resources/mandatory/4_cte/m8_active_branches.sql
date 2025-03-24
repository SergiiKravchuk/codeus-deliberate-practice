--Task: List Active Branches Using CTE
--Requirements:
--- Use CTE to identify active branches (having at least one account)
--- Show branch name and status
--- Basic CTE example
--- Mark branches as 'Active' or 'Inactive'
--
--Expected columns: branch_name, status
WITH active_branches AS (
    SELECT DISTINCT branch_id
    FROM accounts
)
SELECT
    b.branch_name,
    CASE
        WHEN ab.branch_id IS NOT NULL THEN 'Active'
        ELSE 'Inactive'
    END as status
FROM branches b
LEFT JOIN active_branches ab ON b.branch_id = ab.branch_id
ORDER BY b.branch_name;