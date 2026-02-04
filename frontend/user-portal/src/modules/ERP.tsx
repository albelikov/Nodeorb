import React from 'react'

const ERP: React.FC = () => {
  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-center justify-between mb-4">
          <h1 className="text-3xl font-bold text-gray-900">ERP System</h1>
          <div className="flex items-center space-x-2">
            <span className="px-3 py-1 bg-green-100 text-green-800 text-sm font-medium rounded-full">
              Connected
            </span>
          </div>
        </div>
        
        <p className="text-gray-600 mb-6">
          Enterprise Resource Planning system for comprehensive business management.
          Financials, inventory, HR, and supply chain integration in one platform.
        </p>

        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-blue-900">Revenue</h3>
                <p className="text-blue-600">This month</p>
              </div>
              <div className="w-12 h-12 bg-blue-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">$2.4M</span>
              </div>
            </div>
          </div>

          <div className="bg-green-50 border border-green-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-green-900">Orders</h3>
                <p className="text-green-600">This month</p>
              </div>
              <div className="w-12 h-12 bg-green-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">1,234</span>
              </div>
            </div>
          </div>

          <div className="bg-purple-50 border border-purple-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-purple-900">Inventory</h3>
                <p className="text-purple-600">Total value</p>
              </div>
              <div className="w-12 h-12 bg-purple-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">$8.7M</span>
              </div>
            </div>
          </div>

          <div className="bg-orange-50 border border-orange-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-orange-900">Employees</h3>
                <p className="text-orange-600">Active users</p>
              </div>
              <div className="w-12 h-12 bg-orange-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">247</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Financial Overview</h2>
          <div className="space-y-4">
            {[
              { id: 1, account: 'Accounts Receivable', amount: '$1.2M', status: 'Current' },
              { id: 2, account: 'Accounts Payable', amount: '$850K', status: 'Current' },
              { id: 3, account: 'Cash Flow', amount: '$350K', status: 'Positive' },
              { id: 4, account: 'Operating Expenses', amount: '$1.8M', status: 'Budgeted' },
            ].map((account) => (
              <div key={account.id} className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
                <div>
                  <h3 className="font-medium text-gray-900">{account.account}</h3>
                  <p className="text-sm text-gray-600">Financial account</p>
                </div>
                <div className="text-right">
                  <span className="font-bold text-gray-900">{account.amount}</span>
                  <div className={`text-xs ${
                    account.status === 'Positive' ? 'text-green-600' :
                    account.status === 'Current' ? 'text-blue-600' : 'text-gray-600'
                  }`}>
                    {account.status}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Recent Transactions</h2>
          <div className="space-y-4">
            {[
              { id: 1, type: 'Invoice', description: 'Invoice #INV-2024-001', amount: '+$45,000', date: '2 min ago' },
              { id: 2, type: 'Payment', description: 'Payment received', amount: '+$23,500', date: '15 min ago' },
              { id: 3, type: 'Expense', description: 'Office supplies', amount: '-$1,200', date: '1 hour ago' },
              { id: 4, type: 'Transfer', description: 'Inter-account transfer', amount: '$0', date: '3 hours ago' },
            ].map((transaction) => (
              <div key={transaction.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                <div className="flex items-center space-x-3">
                  <div className={`w-2 h-2 rounded-full ${
                    transaction.type === 'Invoice' || transaction.type === 'Payment' ? 'bg-green-500' :
                    transaction.type === 'Expense' ? 'bg-red-500' : 'bg-blue-500'
                  }`} />
                  <div>
                    <p className="font-medium text-gray-900">{transaction.type}</p>
                    <p className="text-sm text-gray-600">{transaction.description}</p>
                  </div>
                </div>
                <div className="text-right">
                  <span className={`font-medium ${
                    transaction.amount.startsWith('+') ? 'text-green-600' :
                    transaction.amount.startsWith('-') ? 'text-red-600' : 'text-gray-600'
                  }`}>
                    {transaction.amount}
                  </span>
                  <p className="text-xs text-gray-500">{transaction.date}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">Quick Actions</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <button className="p-6 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors text-left">
            <div className="flex items-center justify-between">
              <div>
                <span className="font-medium text-gray-900">Generate Reports</span>
                <p className="text-sm text-gray-600">Financial statements</p>
              </div>
              <svg className="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v4a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
            </div>
          </button>
          
          <button className="p-6 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors text-left">
            <div className="flex items-center justify-between">
              <div>
                <span className="font-medium text-gray-900">Manage Users</span>
                <p className="text-sm text-gray-600">Employee access</p>
              </div>
              <svg className="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z" />
              </svg>
            </div>
          </button>
          
          <button className="p-6 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors text-left">
            <div className="flex items-center justify-between">
              <div>
                <span className="font-medium text-gray-900">Inventory Audit</span>
                <p className="text-sm text-gray-600">Stock verification</p>
              </div>
              <svg className="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>
          </button>
        </div>
      </div>
    </div>
  )
}

export default ERP