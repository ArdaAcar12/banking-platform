export type AccountType = 'CURRENT' | 'SAVINGS';

export type AccountStatus = 'ACTIVE' | 'FROZEN' | 'CLOSED';

export interface Account {
  id: string;
  accountNumber: string;
  iban: string;
  accountType: AccountType;
  currency: string;
  balance: number;
  availableBalance: number;
  status: AccountStatus;
  createdAt: string;
  updatedAt: string;
}

export interface AccountRequest {
  accountType: AccountType;
  currency: string;
}

export interface AccountResponse {
  id: string;
  accountNumber: string;
  iban: string;
  accountType: AccountType;
  currency: string;
  balance: number;
  availableBalance: number;
  status: AccountStatus;
  createdAt: string;
  updatedAt: string;
}

export interface AccountListResponse {
  data: AccountResponse[];
}

export interface OpenAccountRequest {
  accountType: AccountType;
  currency: string;
}
