--Task: List All Employees with Their Branch Location
--Requirements:
--- Show employee name and their branch details
--- Include branch location
--- Only show active employees with assigned branches
--- Basic INNER JOIN example
--
--Expected columns: first_name, last_name, branch_name, location
SELECT
    e.first_name,
    e.last_name,
    b.branch_name,
    b.location
FROM employees e
INNER JOIN branches b ON e.branch_id = b.branch_id;