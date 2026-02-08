import React from 'react'

const FMS: React.FC = () => {
  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-center justify-between mb-4">
          <h1 className="text-3xl font-bold text-gray-900">Fleet Management System</h1>
          <div className="flex items-center space-x-2">
            <span className="px-3 py-1 bg-green-100 text-green-800 text-sm font-medium rounded-full">
              Active
            </span>
          </div>
        </div>
        
        <p className="text-gray-600 mb-6">
          Monitor and manage your entire fleet with real-time tracking, maintenance scheduling,
          and performance analytics. Optimize fleet utilization and reduce operational costs.
        </p>

        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-blue-900">Active Vehicles</h3>
                <p className="text-blue-600">Currently in operation</p>
              </div>
              <div className="w-12 h-12 bg-blue-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">47</span>
              </div>
            </div>
          </div>

          <div className="bg-green-50 border border-green-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-green-900">Utilization Rate</h3>
                <p className="text-green-600">Fleet efficiency</p>
              </div>
              <div className="w-12 h-12 bg-green-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">89%</span>
              </div>
            </div>
          </div>

          <div className="bg-purple-50 border border-purple-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-purple-900">Maintenance Due</h3>
                <p className="text-purple-600">Upcoming service</p>
              </div>
              <div className="w-12 h-12 bg-purple-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">3</span>
              </div>
            </div>
          </div>

          <div className="bg-orange-50 border border-orange-200 rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-orange-900">Fuel Efficiency</h3>
                <p className="text-orange-600">Average MPG</p>
              </div>
              <div className="w-12 h-12 bg-orange-500 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">7.2</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Vehicle Status</h2>
          <div className="space-y-4">
            {[
              { id: 1, vehicle: 'Truck #1234', status: 'In Transit', location: 'Chicago, IL', eta: '2h 15m' },
              { id: 2, vehicle: 'Van #5678', status: 'Available', location: 'Depot A', eta: 'Ready' },
              { id: 3, vehicle: 'Truck #9012', status: 'Maintenance', location: 'Service Center', eta: '4h' },
              { id: 4, vehicle: 'Truck #3456', status: 'Loading', location: 'Warehouse B', eta: '30m' },
            ].map((vehicle) => (
              <div key={vehicle.id} className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
                <div>
                  <h3 className="font-medium text-gray-900">{vehicle.vehicle}</h3>
                  <p className="text-sm text-gray-600">{vehicle.location}</p>
                </div>
                <div className="text-right">
                  <span className={`px-3 py-1 rounded text-sm font-medium ${
                    vehicle.status === 'In Transit' ? 'bg-blue-100 text-blue-800' :
                    vehicle.status === 'Available' ? 'bg-green-100 text-green-800' :
                    vehicle.status === 'Maintenance' ? 'bg-yellow-100 text-yellow-800' :
                    'bg-purple-100 text-purple-800'
                  }`}>
                    {vehicle.status}
                  </span>
                  <p className="text-xs text-gray-500 mt-1">ETA: {vehicle.eta}</p>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Maintenance Schedule</h2>
          <div className="space-y-4">
            {[
              { id: 1, vehicle: 'Truck #1234', service: 'Oil Change', due: '2 days', priority: 'High' },
              { id: 2, vehicle: 'Van #5678', service: 'Tire Rotation', due: '1 week', priority: 'Medium' },
              { id: 3, vehicle: 'Truck #9012', service: 'Engine Service', due: 'Today', priority: 'Critical' },
              { id: 4, vehicle: 'Truck #3456', service: 'Brake Check', due: '3 days', priority: 'Low' },
            ].map((maintenance) => (
              <div key={maintenance.id} className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
                <div>
                  <h3 className="font-medium text-gray-900">{maintenance.vehicle}</h3>
                  <p className="text-sm text-gray-600">{maintenance.service}</p>
                </div>
                <div className="text-right">
                  <span className={`px-3 py-1 rounded text-sm font-medium ${
                    maintenance.priority === 'Critical' ? 'bg-red-100 text-red-800' :
                    maintenance.priority === 'High' ? 'bg-orange-100 text-orange-800' :
                    maintenance.priority === 'Medium' ? 'bg-yellow-100 text-yellow-800' : 'bg-green-100 text-green-800'
                  }`}>
                    {maintenance.priority}
                  </span>
                  <p className="text-xs text-gray-500 mt-1">Due: {maintenance.due}</p>
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
                <span className="font-medium text-gray-900">Track Vehicle</span>
                <p className="text-sm text-gray-600">Real-time location</p>
              </div>
              <svg className="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </div>
          </button>
          
          <button className="p-6 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors text-left">
            <div className="flex items-center justify-between">
              <div>
                <span className="font-medium text-gray-900">Schedule Service</span>
                <p className="text-sm text-gray-600">Maintenance booking</p>
              </div>
              <svg className="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </div>
          </button>
          
          <button className="p-6 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors text-left">
            <div className="flex items-center justify-between">
              <div>
                <span className="font-medium text-gray-900">Fleet Analytics</span>
                <p className="text-sm text-gray-600">Performance reports</p>
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

export default FMS