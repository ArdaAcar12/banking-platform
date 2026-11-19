-- Sprint 4: Hesaplar tablosu
CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    account_number VARCHAR(34) NOT NULL,
    iban VARCHAR(34) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    available_balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Hesap numarası ve IBAN benzersizdir.
CREATE UNIQUE INDEX uk_accounts_account_number ON accounts(account_number);
CREATE UNIQUE INDEX uk_accounts_iban ON accounts(iban);

-- Kullanıcı bazlı sorgular için index.
CREATE INDEX idx_accounts_user_id ON accounts(user_id);

-- Hesap durumu ve tür sorguları için index.
CREATE INDEX idx_accounts_status ON accounts(status);
CREATE INDEX idx_accounts_account_type ON accounts(account_type);
