import React from 'react';
import { useSecurity } from '../../core/security/SecurityProvider';

const Fleet: React.FC = () => {
  const { trustScore } = useSecurity();

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg p-6 shadow-sm">
        <h2 className="text-3xl font-bold text-gray-900 mb-2">Fleet Management</h2>
        <p className="text-gray-600">Manage and monitor your vehicle fleet.</p>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Vehicle Tracking</h3>
          <p className="text-gray-600">Track vehicle locations in real-time</p>
        </div>
        
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Maintenance</h3>
          <p className="text-gray-600">Schedule and track maintenance</p>
        </div>
        
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Driver Management</h3>
          <p className="text-gray-600">Manage driver information and schedules</p>
        </div>
      </div>
    </div>
  );
};

export default Fleet;