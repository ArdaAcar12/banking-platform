import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
} from 'recharts'

export interface FinancialChartProps {
  data: Array<{
    month: string
    income: number
    expense: number
  }>
}

export function FinancialChart({ data }: FinancialChartProps) {
  const formatCurrency = (value: number | undefined | null) => {
    if (value === undefined || value === null) return '0 ₺'
    return new Intl.NumberFormat('tr-TR', {
      style: 'currency',
      currency: 'TRY',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value)
  }

  return (
    <div className="rounded-xl border border-border bg-surface p-6 shadow-sm">
      <div className="flex items-center justify-between mb-6">
        <h3 className="text-sm font-bold text-text-primary">Aylik Gelir / Gider</h3>
        <div className="flex items-center gap-2 text-xs text-text-secondary">
          <span className="flex items-center gap-1">
            <span className="h-1.5 w-1.5 rounded-full bg-success" />
            Gelir
          </span>
          <span className="flex items-center gap-1">
            <span className="h-1.5 w-1.5 rounded-full bg-error" />
            Gider
          </span>
        </div>
      </div>
      <div className="h-64 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={data}>
            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E7EBF0" />
            <XAxis
              dataKey="month"
              stroke="#788397"
              tick={{ fontSize: 11 }}
              axisLine={false}
              tickMargin={10}
            />
            <YAxis
              stroke="#788397"
              tick={{ fontSize: 11 }}
              tickFormatter={(value: number | undefined | null) =>
                formatCurrency(value)
              }
              axisLine={false}
              tickMargin={10}
              domain={['dataMin', 'dataMax']}
            />
            <Tooltip
              contentStyle={{
                backgroundColor: '#FFFFFF',
                border: '1px solid #E7EBF0',
                borderRadius: '8px',
              }}
              itemStyle={{ color: '#172033', fontSize: 12 }}
              labelStyle={{ color: '#788397', fontSize: 11, fontWeight: 600 }}
            />
            <Legend verticalAlign="top" align="right" height={36} />
            <Line
              type="monotone"
              dataKey="income"
              stroke="#16B89A"
              strokeWidth={2}
              dot={false}
              name="Gelir"
            />
            <Line
              type="monotone"
              dataKey="expense"
              stroke="#DC5252"
              strokeWidth={2}
              dot={false}
              name="Gider"
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  )
}
