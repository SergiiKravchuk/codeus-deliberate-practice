
-- Create Transactions table
CREATE TABLE transactions
(
    id                SERIAL PRIMARY KEY,
    account_id        INT                                                                           ,
    transaction_type  VARCHAR(20) CHECK (transaction_type IN ('deposit', 'withdrawal', 'transfer')) NOT NULL,
    amount            DECIMAL(15, 2)                                                                NOT NULL,
    transaction_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    target_account_id INT
);



DO $$
DECLARE
    num_transactions INT := coalesce(NULLIF(current_setting('app.num_transactions', TRUE), '')::INT, 1000000);
BEGIN
    EXECUTE format(
        'INSERT INTO transactions (account_id, transaction_type, amount, transaction_date, target_account_id)
        SELECT (random() * 199999 + 1)::INT,
               CASE WHEN random() < 0.4 THEN ''deposit''
                    WHEN random() < 0.8 THEN ''withdrawal''
                    ELSE ''transfer'' END,
               random() * 1000,
               NOW() - INTERVAL ''%s days'' * random(),
               CASE WHEN random() < 0.5 THEN (random() * 99999 + 1)::INT ELSE NULL END
        FROM generate_series(1, %s);', num_transactions, num_transactions);
END $$;

