export type TransactionType = 'TRANSFER' | 'DEPOSIT' | 'WITHDRAWAL' | 'FEE';

export type TransactionStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'REVERSED';

export interface Transaction {
  id: string;
  referenceNumber: string;
  transactionType: TransactionType;
  status: TransactionStatus;
  amount: number;
  currency: string;
  description: string;
  createdAt: string;
  completedAt: string | null;
}

export interface TransactionListResponse {
  data: Transaction[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface TransactionFilters {
  type?: TransactionType;
  status?: TransactionStatus;
  startDate?: string;
  endDate?: string;
}
