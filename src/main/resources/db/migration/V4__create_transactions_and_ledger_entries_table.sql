-- Sprint 5: Finansal işlem ve ledger kayıtları tabloları
CREATE TABLE financial_transactions (
    id UUID PRIMARY KEY,
    reference_number VARCHAR(50) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    description VARCHAR(255) NOT NULL,
    initiated_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP NULL,
    CONSTRAINT fk_financial_transactions_initiated_by FOREIGN KEY (initiated_by) REFERENCES users(id)
);

-- Referans numarası benzersizdir.
CREATE UNIQUE INDEX uk_financial_transactions_reference_number ON financial_transactions(reference_number);

-- İşlem türü, durum ve oluşturma tarihi sorguları için indexler.
CREATE INDEX idx_financial_transactions_type ON financial_transactions(transaction_type);
CREATE INDEX idx_financial_transactions_status ON financial_transactions(status);
CREATE INDEX idx_financial_transactions_created_at ON financial_transactions(created_at);
CREATE INDEX idx_financial_transactions_initiated_by ON financial_transactions(initiated_by);

CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL,
    account_id UUID NOT NULL,
    direction VARCHAR(10) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_ledger_entries_transaction FOREIGN KEY (transaction_id) REFERENCES financial_transactions(id),
    CONSTRAINT fk_ledger_entries_account FOREIGN KEY (account_id) REFERENCES accounts(id)
);

-- İşlem bazlı sorgular için index.
CREATE INDEX idx_ledger_entries_transaction_id ON ledger_entries(transaction_id);

-- Hesap bazlı sorgular için index.
CREATE INDEX idx_ledger_entries_account_id ON ledger_entries(account_id);

-- Oluşturma tarihi sorguları için index.
CREATE INDEX idx_ledger_entries_created_at ON ledger_entries(created_at);
