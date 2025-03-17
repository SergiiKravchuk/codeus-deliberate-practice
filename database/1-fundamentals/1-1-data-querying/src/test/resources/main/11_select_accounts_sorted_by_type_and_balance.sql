--Retrieve all accounts, ordering them first by account type ('savings' accounts before 'checking' accounts), and within each account type, sort by balance in descending order.
SELECT *
FROM Accounts
ORDER BY
    CASE account_type
        WHEN 'savings' THEN 1
        WHEN 'checking' THEN 2
        ELSE 3
    END ASC,
    balance DESC;