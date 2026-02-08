import React from 'react'

const TMS: React.FC = () => {
  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-center justify-between mb-4">
          <h1 className="text-3xl font-bold text-gray-900">Transportation Management System</h1>
          <div className="flex items-center space-x-2">
            <span className="px-3 py-1 bg-green-100 text-green-800 text-sm font-medium rounded-full">
              Operational
            </span>
          </div>
        </div>
        
        <p className="text-gray-600 mb-6">
          Optimize transportation operations, route planning, and carrier management.
          Real-time tracking and performance analytics for all transport activities.
        </p>

        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-blue-900">Active Routes</h3>
                <p className="text-blue-600">Currently in transit</p>
              </div>
              <div className="w-12 h-12 bg-blue-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">24</span>
              </div>
            </div>
          </div>

          <div className="bg-green-50 border border-green-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-green-900">On-Time Rate</h3>
                <p className="text-green-600">Delivery performance</p>
              </div>
              <div className="w-12 h-12 bg-green-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">94.2%</span>
              </div>
            </div>
          </div>

          <div className="bg-purple-50 border border-purple-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-purple-900">Cost Savings</h3>
                <p className="text-purple-600">Optimized routing</p>
              </div>
              <div className="w-12 h-12 bg-purple-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">$12.5K</span>
              </div>
            </div>
          </div>

          <div className="bg-orange-50 border border-orange-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-orange-900">Capacity</h3>
                <p className="text-orange-600">Available resources</p>
              </div>
              <div className="w-12 h-12 bg-orange-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">87%</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Route Overview</h2>
          <div className="space-y-4">
            {[
              { id: 1, route: 'NYC → Chicago', status: 'In Transit', eta: '2h 15m', distance: '790 mi' },
              { id: 2, route: 'LA → Phoenix', status: 'Loading', eta: '4h 30m', distance: '370 mi' },
              { id: 3, route: 'Miami → Atlanta', status: 'Delivered', eta: 'On Time', distance: '660 mi' },
              { id: 4, route: 'Seattle → Portland', status: 'En Route', eta: '3h 45m', distance: '175 mi' },
            ].map((route) => (
              <div key={route.id} className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
                <div>
                  <h3 className="font-medium text-gray-900">{route.route}</h3>
                  <p className="text-sm text-gray-600">{route.distance} • ETA: {route.eta}</p>
                </div>
                <span className={`px-3 py-1 rounded text-sm font-medium ${
                  route.status === 'In Transit' ? 'bg-blue-100 text-blue-800' :
                  route.status === 'Loading' ? 'bg-yellow-100 text-yellow-800' :
                  route.status === 'Delivered' ? 'bg-green-100 text-green-800' :
                  'bg-purple-100 text-purple-800'
                }`}>
                  {route.status}
                </span>
              </div>
            ))}
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Performance Metrics</h2>
          <div className="space-y-4">
            {[
              { id: 1, metric: 'Average Transit Time', value: '2.3 days', trend: 'improving' },
              { id: 2, metric: 'Fuel Efficiency', value: '6.8 mpg', trend: 'stable' },
              { id: 3, metric: 'Load Optimization', value: '92%', trend: 'improving' },
              { id: 4, metric: 'Driver Utilization', value: '85%', trend: 'stable' },
            ].map((metric) => (
              <div key={metric.id} className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
                <div>
                  <h3 className="font-medium text-gray-900">{metric.metric}</h3>
                  <p className="text-sm text-gray-600">Current performance</p>
                </div>
                <div className="text-right">
                  <span className="font-bold text-gray-900">{metric.value}</span>
                  <div className={`text-xs ${
                    metric.trend === 'improving' ? 'text-green-600' :
                    metric.trend === 'declining' ? 'text-red-600' : 'text-gray-500'
                  }`}>
                    {metric.trend === 'improving' ? '↗' : metric.trend === 'declining' ? '↘' : '→'} {metric.trend}
                  </div>
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
                <span className="font-medium text-gray-900">Route Optimization</span>
                <p className="text-sm text-gray-600">Optimize delivery routes</p>
              </div>
              <svg className="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
              </svg>
            </div>
          </button>
          
          <button className="p-6 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors text-left">
            <div className="flex items-center justify-between">
              <div>
                <span className="font-medium text-gray-900">Carrier Management</span>
                <p className="text-sm text-gray-600">Manage transport partners</p>
              </div>
              <svg className="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
            </div>
          </button>
          
          <button className="p-6 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors text-left">
            <div className="flex items-center justify-between">
              <div>
                <span className="font-medium text-gray-900">Track Shipment</span>
                <p className="text-sm text-gray-600">Monitor delivery status</p>
              </div>
              <svg className="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
          </button>
        </div>
      </div>
    </div>
  )
}

export default TMS