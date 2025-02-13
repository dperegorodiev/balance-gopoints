CREATE TABLE accounts (
                          id UUID PRIMARY KEY,
                          balance DECIMAL(19,2) NOT NULL,
                          created_at TIMESTAMP NOT NULL
);

CREATE TABLE transactions (
                              id UUID PRIMARY KEY,
                              account_id UUID NOT NULL,
                              to_account_id UUID,
                              type VARCHAR(50) NOT NULL,
                              amount DECIMAL(19,2) NOT NULL,
                              balance_after DECIMAL(19,2) NOT NULL,
                              created_at TIMESTAMP NOT NULL,
                              FOREIGN KEY (account_id) REFERENCES accounts(id),
                              FOREIGN KEY (to_account_id) REFERENCES accounts(id)
);

-- Индексы для оптимизации запросов
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);