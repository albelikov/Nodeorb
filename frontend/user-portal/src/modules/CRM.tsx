import React from 'react'

const CRM: React.FC = () => {
  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-center justify-between mb-4">
          <h1 className="text-3xl font-bold text-gray-900">Customer Relationship Management</h1>
          <div className="flex items-center space-x-2">
            <span className="px-3 py-1 bg-green-100 text-green-800 text-sm font-medium rounded-full">
              Active
            </span>
          </div>
        </div>
        
        <p className="text-gray-600 mb-6">
          Manage customer interactions, track relationships, and enhance client satisfaction.
          Complete view of customer history and engagement metrics.
        </p>

        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-blue-900">Active Clients</h3>
                <p className="text-blue-600">Current customers</p>
              </div>
              <div className="w-12 h-12 bg-blue-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">156</span>
              </div>
            </div>
          </div>

          <div className="bg-green-50 border border-green-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-green-900">Satisfaction</h3>
                <p className="text-green-600">Customer rating</p>
              </div>
              <div className="w-12 h-12 bg-green-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">4.8/5</span>
              </div>
            </div>
          </div>

          <div className="bg-purple-50 border border-purple-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-purple-900">Response Time</h3>
                <p className="text-purple-600">Average</p>
              </div>
              <div className="w-12 h-12 bg-purple-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">2.1h</span>
              </div>
            </div>
          </div>

          <div className="bg-orange-50 border border-orange-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-orange-900">Retention Rate</h3>
                <p className="text-orange-600">Customer loyalty</p>
              </div>
              <div className="w-12 h-12 bg-orange-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">94%</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Recent Interactions</h2>
          <div className="space-y-4">
            {[
              { id: 1, customer: 'Acme Corp', type: 'Support Call', status: 'Resolved', time: '15 min ago' },
              { id: 2, customer: 'Global Tech', type: 'Email', status: 'Pending', time: '30 min ago' },
              { id: 3, customer: 'Innovate Inc', type: 'Meeting', status: 'Scheduled', time: '1 hour ago' },
              { id: 4, customer: 'Summit Logistics', type: 'Quote Request', status: 'In Progress', time: '2 hours ago' },
            ].map((interaction) => (
              <div key={interaction.id} className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
                <div>
                  <h3 className="font-medium text-gray-900">{interaction.customer}</h3>
                  <p className="text-sm text-gray-600">{interaction.type}</p>
                </div>
                <div className="text-right">
                  <span className={`px-3 py-1 rounded text-sm font-medium ${
                    interaction.status === 'Resolved' ? 'bg-green-100 text-green-800' :
                    interaction.status === 'Pending' ? 'bg-yellow-100 text-yellow-800' :
                    interaction.status === 'Scheduled' ? 'bg-blue-100 text-blue-800' :
                    'bg-purple-100 text-purple-800'
                  }`}>
                    {interaction.status}
                  </span>
                  <p className="text-xs text-gray-500 mt-1">{interaction.time}</p>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Customer Segments</h2>
          <div className="space-y-4">
            {[
              { id: 1, segment: 'Enterprise', customers: 23, revenue: '$2.4M', growth: '+12%' },
              { id: 2, segment: 'Mid-Market', customers: 67, revenue: '$1.8M', growth: '+8%' },
              { id: 3, segment: 'SMB', customers: 66, revenue: '$850K', growth: '+15%' },
            ].map((segment) => (
              <div key={segment.id} className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
                <div>
                  <h3 className="font-medium text-gray-900">{segment.segment}</h3>
                  <p className="text-sm text-gray-600">{segment.customers} customers</p>
                </div>
                <div className="text-right">
                  <span className="font-bold text-gray-900">{segment.revenue}</span>
                  <div className="text-xs text-green-600">+{segment.growth}</div>
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
                <span className="font-medium text-gray-900">Add Customer</span>
                <p className="text-sm text-gray-600">New client onboarding</p>
              </div>
              <svg className="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
              </svg>
            </div>
          </button>
          
          <button className="p-6 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors text-left">
            <div className="flex items-center justify-between">
              <div>
                <span className="font-medium text-gray-900">Send Quote</span>
                <p className="text-sm text-gray-600">Customer proposal</p>
              </div>
              <svg className="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>
          </button>
          
          <button className="p-6 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors text-left">
            <div className="flex items-center justify-between">
              <div>
                <span className="font-medium text-gray-900">Customer Analytics</span>
                <p className="text-sm text-gray-600">Insights & trends</p>
              </div>
              <svg className="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v4a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
            </div>
          </button>
        </div>
      </div>
    </div>
  )
}

export default CRM