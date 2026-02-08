import React from 'react'

const Inventory: React.FC = () => {
  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-center justify-between mb-4">
          <h1 className="text-3xl font-bold text-gray-900">Inventory Management</h1>
          <div className="flex items-center space-x-2">
            <span className="px-3 py-1 bg-green-100 text-green-800 text-sm font-medium rounded-full">
              Optimized
            </span>
          </div>
        </div>
        
        <p className="text-gray-600 mb-6">
          Comprehensive inventory tracking and management system. Real-time stock levels,
          automated reordering, and demand forecasting for optimal inventory control.
        </p>

        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-blue-900">Total Items</h3>
                <p className="text-blue-600">In stock</p>
              </div>
              <div className="w-12 h-12 bg-blue-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">15,427</span>
              </div>
            </div>
          </div>

          <div className="bg-green-50 border border-green-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-green-900">Turnover Rate</h3>
                <p className="text-green-600">Inventory velocity</p>
              </div>
              <div className="w-12 h-12 bg-green-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">3.2x</span>
              </div>
            </div>
          </div>

          <div className="bg-purple-50 border border-purple-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-purple-900">Stock Accuracy</h3>
                <p className="text-purple-600">Audit precision</p>
              </div>
              <div className="w-12 h-12 bg-purple-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">99.8%</span>
              </div>
            </div>
          </div>

          <div className="bg-orange-50 border border-orange-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-orange-900">Low Stock Items</h3>
                <p className="text-orange-600">Reorder needed</p>
              </div>
              <div className="w-12 h-12 bg-orange-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">12</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Stock Levels</h2>
          <div className="space-y-4">
            {[
              { id: 1, item: 'SKU-12345', category: 'Electronics', current: 150, min: 50, max: 500 },
              { id: 2, item: 'SKU-67890', category: 'Components', current: 25, min: 30, max: 200 },
              { id: 3, item: 'SKU-54321', category: 'Tools', current: 89, min: 25, max: 300 },
              { id: 4, item: 'SKU-98765', category: 'Materials', current: 450, min: 100, max: 1000 },
            ].map((item) => (
              <div key={item.id} className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
                <div>
                  <h3 className="font-medium text-gray-900">{item.item}</h3>
                  <p className="text-sm text-gray-600">{item.category}</p>
                </div>
                <div className="text-right">
                  <div className="flex items-center space-x-4">
                    <div className="text-xs text-gray-500">
                      {item.current}/{item.max}
                    </div>
                    <div className="w-24 bg-gray-200 rounded-full h-2">
                      <div 
                        className={`h-2 rounded-full ${
                          item.current < item.min ? 'bg-red-500' :
                          item.current < item.max * 0.3 ? 'bg-yellow-500' : 'bg-green-500'
                        }`}
                        style={{ width: `${(item.current / item.max) * 100}%` }}
                      />
                    </div>
                  </div>
                  {item.current < item.min && (
                    <span className="text-xs text-red-600 mt-1">⚠️ Low stock</span>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Inventory Movement</h2>
          <div className="space-y-4">
            {[
              { id: 1, type: 'Received', item: 'SKU-12345', quantity: '+100', time: '2 min ago' },
              { id: 2, type: 'Shipped', item: 'SKU-67890', quantity: '-15', time: '15 min ago' },
              { id: 3, type: 'Adjusted', item: 'SKU-54321', quantity: '+5', time: '1 hour ago' },
              { id: 4, type: 'Transferred', item: 'SKU-98765', quantity: '-50', time: '3 hours ago' },
            ].map((movement) => (
              <div key={movement.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                <div className="flex items-center space-x-3">
                  <div className={`w-2 h-2 rounded-full ${
                    movement.type === 'Received' ? 'bg-green-500' :
                    movement.type === 'Shipped' ? 'bg-red-500' :
                    movement.type === 'Adjusted' ? 'bg-blue-500' : 'bg-purple-500'
                  }`} />
                  <div>
                    <p className="font-medium text-gray-900">{movement.type}</p>
                    <p className="text-sm text-gray-600">{movement.item}</p>
                  </div>
                </div>
                <div className="text-right">
                  <span className={`font-medium ${
                    movement.quantity.startsWith('+') ? 'text-green-600' :
                    movement.quantity.startsWith('-') ? 'text-red-600' : 'text-gray-600'
                  }`}>
                    {movement.quantity}
                  </span>
                  <p className="text-xs text-gray-500">{movement.time}</p>
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
                <span className="font-medium text-gray-900">Stock Audit</span>
                <p className="text-sm text-gray-600">Physical count</p>
              </div>
              <svg className="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>
          </button>
          
          <button className="p-6 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors text-left">
            <div className="flex items-center justify-between">
              <div>
                <span className="font-medium text-gray-900">Reorder Items</span>
                <p className="text-sm text-gray-600">Supplier orders</p>
              </div>
              <svg className="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
              </svg>
            </div>
          </button>
          
          <button className="p-6 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors text-left">
            <div className="flex items-center justify-between">
              <div>
                <span className="font-medium text-gray-900">Inventory Reports</span>
                <p className="text-sm text-gray-600">Analytics & trends</p>
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

export default Inventory