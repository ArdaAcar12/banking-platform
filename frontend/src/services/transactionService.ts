import { apiClient } from '@/services/api/apiClient'
import type { TransactionListResponse, Transaction } from '@/types/transaction'

const TRANSACTIONS_BASE = '/api/v1/transactions'

export const transactionService = {
  listTransactions: (
    params: {
      page?: number
      size?: number
      sort?: string
      type?: string
      status?: string
      startDate?: string
      endDate?: string
    } = {}
  ): Promise<TransactionListResponse> => {
    return apiClient
      .get(TRANSACTIONS_BASE, { params })
      .then((r) => r.data)
  },

  listTransactionsByAccount: (
    accountId: string,
    params: {
      page?: number
      size?: number
      sort?: string
      type?: string
      status?: string
      startDate?: string
      endDate?: string
    } = {}
  ): Promise<TransactionListResponse> => {
    return apiClient
      .get(`${TRANSACTIONS_BASE}/accounts/${accountId}/transactions`, {
        params,
      })
      .then((r) => r.data)
  },

  getTransaction: (id: string): Promise<Transaction> => {
    return apiClient.get(`${TRANSACTIONS_BASE}/${id}`).then((r) => r.data)
  },
}

export default transactionService
