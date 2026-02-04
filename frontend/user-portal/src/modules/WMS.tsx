import React from 'react'

const WMS: React.FC = () => {
  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-center justify-between mb-4">
          <h1 className="text-3xl font-bold text-gray-900">Warehouse Management System</h1>
          <div className="flex items-center space-x-2">
            <span className="px-3 py-1 bg-green-100 text-green-800 text-sm font-medium rounded-full">
              Connected
            </span>
          </div>
        </div>
        
        <p className="text-gray-600 mb-6">
          Manage warehouse operations, inventory tracking, and automated storage systems.
          Real-time visibility into warehouse activities and performance metrics.
        </p>

        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div className="bg-green-50 border border-green-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-green-900">Inventory</h3>
                <p className="text-green-600">Total items in stock</p>
              </div>
              <div className="w-12 h-12 bg-green-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">12,847</span>
              </div>
            </div>
          </div>

          <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-blue-900">Utilization</h3>
                <p className="text-blue-600">Space efficiency</p>
              </div>
              <div className="w-12 h-12 bg-blue-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">78%</span>
              </div>
            </div>
          </div>

          <div className="bg-purple-50 border border-purple-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-purple-900">Pick Rate</h3>
                <p className="text-purple-600">Orders per hour</p>
              </div>
              <div className="w-12 h-12 bg-purple-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">156</span>
              </div>
            </div>
          </div>

          <div className="bg-orange-50 border border-orange-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-orange-900">Accuracy</h3>
                <p className="text-orange-600">Order fulfillment</p>
              </div>
              <div className="w-12 h-12 bg-orange-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">99.2%</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Warehouse Zones</h2>
          <div className="space-y-4">
            {[
              { id: 1, name: 'Receiving Area', utilization: 65, status: 'Active' },
              { id: 2, name: 'Storage Zone A', utilization: 82, status: 'High' },
              { id: 3, name: 'Picking Zone B', utilization: 45, status: 'Optimal' },
              { id: 4, name: 'Shipping Area', utilization: 30, status: 'Low' },
            ].map((zone) => (
              <div key={zone.id} className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
                <div>
                  <h3 className="font-medium text-gray-900">{zone.name}</h3>
                  <p className="text-sm text-gray-600">Zone utilization: {zone.utilization}%</p>
                </div>
                <div className="flex items-center space-x-3">
                  <div className="w-24 bg-gray-200 rounded-full h-2">
                    <div 
                      className={`h-2 rounded-full ${
                        zone.utilization > 80 ? 'bg-red-500' :
                        zone.utilization > 60 ? 'bg-yellow-500' : 'bg-green-500'
                      }`}
                      style={{ width: `${zone.utilization}%` }}
                    />
                  </div>
                  <span className={`px-2 py-1 rounded text-xs font-medium ${
                    zone.status === 'High' ? 'bg-red-100 text-red-800' :
                    zone.status === 'Optimal' ? 'bg-green-100 text-green-800' :
                    'bg-blue-100 text-blue-800'
                  }`}>
                    {zone.status}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Recent Activity</h2>
          <div className="space-y-4">
            {[
              { id: 1, type: 'Inventory Update', description: 'Received 500 units of SKU-12345', time: '5 min ago' },
              { id: 2, type: 'Pick Order', description: 'Order #WMS-8899 picked and packed', time: '12 min ago' },
              { id: 3, type: 'Stock Alert', description: 'Low stock alert for SKU-67890', time: '25 min ago' },
              { id: 4, type: 'Zone Update', description: 'Zone B capacity optimized', time: '1 hour ago' },
            ].map((activity) => (
              <div key={activity.id} className="flex items-center space-x-3 p-3 bg-gray-50 rounded-lg">
                <div className={`w-2 h-2 rounded-full ${
                  activity.type === 'Inventory Update' ? 'bg-blue-500' :
                  activity.type === 'Pick Order' ? 'bg-green-500' :
                  activity.type === 'Stock Alert' ? 'bg-red-500' : 'bg-purple-500'
                }`} />
                <div className="flex-1">
                  <p className="font-medium text-gray-900">{activity.type}</p>
                  <p className="text-sm text-gray-600">{activity.description}</p>
                </div>
                <span className="text-xs text-gray-500">{activity.time}</span>
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
                <span className="font-medium text-gray-900">Inventory Lookup</span>
                <p className="text-sm text-gray-600">Search and track items</p>
              </div>
              <svg className="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
          </button>
          
          <button className="p-6 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors text-left">
            <div className="flex items-center justify-between">
              <div>
                <span className="font-medium text-gray-900">Receive Shipment</span>
                <p className="text-sm text-gray-600">Process incoming goods</p>
              </div>
              <svg className="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>
          </button>
          
          <button className="p-6 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors text-left">
            <div className="flex items-center justify-between">
              <div>
                <span className="font-medium text-gray-900">Generate Reports</span>
                <p className="text-sm text-gray-600">Export warehouse data</p>
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

export default WMS