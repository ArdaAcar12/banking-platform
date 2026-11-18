export interface DemoTransaction {
  id: string
  description: string
  category: 'FOOD' | 'TRANSPORT' | 'UTILITIES' | 'ENTERTAINMENT' | 'SALARY' | 'TRANSFER' | 'SHOPPING'
  amount: number
  type: 'INCOME' | 'EXPENSE'
  date: string
  status: 'COMPLETED' | 'PENDING' | 'FAILED'
  merchant?: string
}

export const demoTransactions: DemoTransaction[] = [
  {
    id: 'tx-001',
    description: 'Market Alışverişi',
    category: 'FOOD',
    amount: -185.40,
    type: 'EXPENSE',
    date: '2026-11-17T14:30:00Z',
    status: 'COMPLETED',
    merchant: 'Migros',
  },
  {
    id: 'tx-002',
    description: 'Maas Odeme',
    category: 'SALARY',
    amount: 45000.00,
    type: 'INCOME',
    date: '2026-11-15T09:00:00Z',
    status: 'COMPLETED',
    merchant: 'TeknoSoft Yazilim',
  },
  {
    id: 'tx-003',
    description: 'Ulasim Ucreti',
    category: 'TRANSPORT',
    amount: -45.50,
    type: 'EXPENSE',
    date: '2026-11-14T08:15:00Z',
    status: 'COMPLETED',
    merchant: 'ISTANBULKART',
  },
  {
    id: 'tx-004',
    description: 'Elektrik Faturalari',
    category: 'UTILITIES',
    amount: -320.00,
    type: 'EXPENSE',
    date: '2026-11-10T16:45:00Z',
    status: 'COMPLETED',
    merchant: 'Enerjisa',
  },
  {
    id: 'tx-005',
    description: 'Kafe Ucreti',
    category: 'FOOD',
    amount: -68.25,
    type: 'EXPENSE',
    date: '2026-11-08T11:20:00Z',
    status: 'COMPLETED',
    merchant: 'Cafe Nero',
  },
  {
    id: 'tx-006',
    description: 'Online Alisveris',
    category: 'SHOPPING',
    amount: -245.00,
    type: 'EXPENSE',
    date: '2026-11-05T20:10:00Z',
    status: 'COMPLETED',
    merchant: 'Trendyol',
  },
  {
    id: 'tx-007',
    description: 'Arkadasa Havale',
    category: 'TRANSFER',
    amount: -500.00,
    type: 'EXPENSE',
    date: '2026-11-03T10:00:00Z',
    status: 'COMPLETED',
  },
  {
    id: 'tx-008',
    description: 'Sinema Bileti',
    category: 'ENTERTAINMENT',
    amount: -120.00,
    type: 'EXPENSE',
    date: '2026-11-01T19:30:00Z',
    status: 'COMPLETED',
    merchant: 'Cinemaximum',
  },
]

export const demoMonthlyIncome = demoTransactions
  .filter((t) => t.type === 'INCOME')
  .reduce((sum, t) => sum + t.amount, 0)

export const demoMonthlyExpense = demoTransactions
  .filter((t) => t.type === 'EXPENSE')
  .reduce((sum, t) => sum + Math.abs(t.amount), 0)

export const demoCategorySpending = [
  { name: 'Yemek', value: 253.65, color: '#16B89A' },
  { name: 'Ulasim', value: 45.50, color: '#101B33' },
  { name: 'Faturalar', value: 320.00, color: '#182A46' },
  { name: 'Alisveris', value: 245.00, color: '#E9A23B' },
  { name: 'Eglence', value: 120.00, color: '#DC5252' },
  { name: 'Havale', value: 500.00, color: '#788397' },
]

export const demoMonthlyBalanceHistory = [
  { month: 'Haziran', income: 45000, expense: 18500 },
  { month: 'Temmuz', income: 45000, expense: 21200 },
  { month: 'Agustos', income: 45000, expense: 16800 },
  { month: 'Eylul', income: 45000, expense: 19400 },
  { month: 'Ekim', income: 45000, expense: 23100 },
  { month: 'Kasim', income: 45000, expense: 17600 },
]
