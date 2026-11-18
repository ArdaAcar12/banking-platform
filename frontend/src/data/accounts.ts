export interface DemoAccount {
  id: string
  name: string
  type: 'CHECKING' | 'SAVINGS' | 'DAILY_SPENDING'
  iban: string
  balance: number
  currency: 'TRY' | 'USD' | 'EUR'
  maskedIban: string
  lastFour: string
}

export const demoAccounts: DemoAccount[] = [
  {
    id: 'acc-001',
    name: 'Ana Hesap',
    type: 'CHECKING',
    iban: 'TR42 0006 2005 1970 1413 2099',
    balance: 24567.83,
    currency: 'TRY',
    maskedIban: 'TR**** **** **** 2099',
    lastFour: '2099',
  },
  {
    id: 'acc-002',
    name: 'Birikim Hesabı',
    type: 'SAVINGS',
    iban: 'TR89 0011 3002 4567 8901 2345',
    balance: 89342.15,
    currency: 'TRY',
    maskedIban: 'TR**** **** **** 2345',
    lastFour: '2345',
  },
  {
    id: 'acc-003',
    name: 'Günlük Harcama Hesabı',
    type: 'DAILY_SPENDING',
    iban: 'TR33 0020 4001 7890 1234 5678',
    balance: 3420.50,
    currency: 'TRY',
    maskedIban: 'TR**** **** **** 5678',
    lastFour: '5678',
  },
]

export const demoTotalBalance = demoAccounts.reduce(
  (sum, acc) => sum + acc.balance,
  0,
)

export const demoAvailableBalance = demoTotalBalance * 0.92

export const demoPendingTransactions = 3

export const demoLastTransactionDate = '17.11.2026'
