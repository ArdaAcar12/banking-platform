import { apiClient } from '@/services/api/apiClient'
import type {
  AccountListResponse,
  AccountRequest,
  AccountResponse,
} from '@/types/account'

const ACCOUNTS_BASE = '/api/v1/accounts'

export const accountService = {
  listAccounts: (): Promise<AccountListResponse> =>
    apiClient.get(`${ACCOUNTS_BASE}`).then((r) => r.data),

  getAccount: (id: string): Promise<AccountResponse> =>
    apiClient.get(`${ACCOUNTS_BASE}/${id}`).then((r) => r.data),

  openAccount: (data: AccountRequest): Promise<AccountResponse> =>
    apiClient.post(`${ACCOUNTS_BASE}`, data).then((r) => r.data),
}

export default accountService
