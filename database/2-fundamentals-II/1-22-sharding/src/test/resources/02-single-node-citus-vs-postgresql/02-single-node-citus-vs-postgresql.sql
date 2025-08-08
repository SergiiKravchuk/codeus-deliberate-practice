-- Select transactions from branch 3 that have amount over 5k
SELECT tx.id, tx.amount, tx.transaction_date
FROM transactions tx
WHERE tx.branch_id = 3
  AND tx.amount > 5000
  AND tx.transaction_date > NOW() - INTERVAL '5 days';


-- Select accounts from branch 3 that have transactions over 5k for last 5 days
SELECT acc.id, acc.customer_id, acc.account_type, tx.id, tx.amount
FROM accounts acc
         JOIN transactions tx
              ON tx.branch_id = acc.branch_id
                  AND tx.account_id = acc.id
WHERE acc.branch_id = 3
  AND tx.amount > 5000
  AND tx.transaction_date > NOW() - INTERVAL '5 days'
GROUP BY acc.id, acc.customer_id, acc.account_type, tx.id, tx.amount;


-- Select accounts from branches 2 and 3 that have transactions over 5k for last 5 days
SELECT acc.id, acc.customer_id, acc.account_type, tx.id, tx.amount
FROM accounts acc
         JOIN transactions tx
              ON tx.branch_id = acc.branch_id
                  AND tx.account_id = acc.id
WHERE acc.branch_id IN (2, 3)
  AND tx.amount > 5000
  AND tx.transaction_date > NOW() - INTERVAL '5 days'
GROUP BY acc.id, acc.customer_id, acc.account_type, tx.id, tx.amount;