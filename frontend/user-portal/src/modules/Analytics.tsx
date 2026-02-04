import React from 'react'

const Analytics: React.FC = () => {
  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-center justify-between mb-4">
          <h1 className="text-3xl font-bold text-gray-900">Analytics & Insights</h1>
          <div className="flex items-center space-x-2">
            <span className="px-3 py-1 bg-green-100 text-green-800 text-sm font-medium rounded-full">
              Live Data
            </span>
          </div>
        </div>
        
        <p className="text-gray-600 mb-6">
          Advanced analytics and business intelligence for data-driven decision making.
          Real-time dashboards, predictive analytics, and comprehensive reporting.
        </p>

        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-blue-900">Revenue Growth</h3>
                <p className="text-blue-600">Month-over-month</p>
              </div>
              <div className="w-12 h-12 bg-blue-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">+12.5%</span>
              </div>
            </div>
          </div>

          <div className="bg-green-50 border border-green-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-green-900">Customer Retention</h3>
                <p className="text-green-600">Churn rate</p>
              </div>
              <div className="w-12 h-12 bg-green-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">-3.2%</span>
              </div>
            </div>
          </div>

          <div className="bg-purple-50 border border-purple-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-purple-900">Operational Efficiency</h3>
                <p className="text-purple-600">Process optimization</p>
              </div>
              <div className="w-12 h-12 bg-purple-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">+18.7%</span>
              </div>
            </div>
          </div>

          <div className="bg-orange-50 border border-orange-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-orange-900">Cost Savings</h3>
                <p className="text-orange-600">Optimization impact</p>
              </div>
              <div className="w-12 h-12 bg-orange-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">$245K</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Key Metrics</h2>
          <div className="space-y-4">
            {[
              { id: 1, metric: 'Average Order Value', value: '$1,247', change: '+8.5%' },
              { id: 2, metric: 'Conversion Rate', value: '12.3%', change: '+2.1%' },
              { id: 3, metric: 'Customer Lifetime Value', value: '$8,950', change: '+15.2%' },
              { id: 4, metric: 'Operational Downtime', value: '1.2%', change: '-0.8%' },
            ].map((metric) => (
              <div key={metric.id} className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
                <div>
                  <h3 className="font-medium text-gray-900">{metric.metric}</h3>
                  <p className="text-sm text-gray-600">Performance indicator</p>
                </div>
                <div className="text-right">
                  <span className="font-bold text-gray-900">{metric.value}</span>
                  <div className="text-xs text-green-600">+{metric.change}</div>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Trend Analysis</h2>
          <div className="space-y-4">
            {[
              { id: 1, trend: 'Revenue Growth', direction: 'Upward', confidence: 'High', period: 'Last 6 months' },
              { id: 2, trend: 'Customer Satisfaction', direction: 'Stable', confidence: 'Medium', period: 'Last 3 months' },
              { id: 3, trend: 'Operational Costs', direction: 'Downward', confidence: 'High', period: 'Last 4 months' },
              { id: 4, trend: 'Market Share', direction: 'Growing', confidence: 'Medium', period: 'Last 8 months' },
            ].map((trend) => (
              <div key={trend.id} className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
                <div>
                  <h3 className="font-medium text-gray-900">{trend.trend}</h3>
                  <p className="text-sm text-gray-600">{trend.period}</p>
                </div>
                <div className="text-right">
                  <span className={`px-3 py-1 rounded text-sm font-medium ${
                    trend.direction === 'Upward' || trend.direction === 'Growing' ? 'bg-green-100 text-green-800' :
                    trend.direction === 'Stable' ? 'bg-blue-100 text-blue-800' : 'bg-red-100 text-red-800'
                  }`}>
                    {trend.direction}
                  </span>
                  <div className="text-xs text-gray-500 mt-1">Confidence: {trend.confidence}</div>
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
                <span className="font-medium text-gray-900">Generate Report</span>
                <p className="text-sm text-gray-600">Custom analytics</p>
              </div>
              <svg className="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v4a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
            </div>
          </button>
          
          <button className="p-6 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors text-left">
            <div className="flex items-center justify-between">
              <div>
                <span className="font-medium text-gray-900">Predictive Analysis</span>
                <p className="text-sm text-gray-600">Forecast trends</p>
              </div>
              <svg className="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v4a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
            </div>
          </button>
          
          <button className="p-6 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors text-left">
            <div className="flex items-center justify-between">
              <div>
                <span className="font-medium text-gray-900">Data Export</span>
                <p className="text-sm text-gray-600">Export to formats</p>
              </div>
              <svg className="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
              </svg>
            </div>
          </button>
        </div>
      </div>
    </div>
  )
}

export default Analytics