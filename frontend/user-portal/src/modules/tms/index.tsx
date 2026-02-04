import React from 'react';
import { useSecurity } from '../../core/security/SecurityProvider';

const TMS: React.FC = () => {
  const { trustScore } = useSecurity();

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg p-6 shadow-sm">
        <h2 className="text-3xl font-bold text-gray-900 mb-2">Transportation Management System</h2>
        <p className="text-gray-600">Optimize transportation operations and route planning.</p>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Route Planning</h3>
          <p className="text-gray-600">Plan and optimize delivery routes</p>
        </div>
        
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Fleet Management</h3>
          <p className="text-gray-600">Manage your transportation fleet</p>
        </div>
        
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Real-time Tracking</h3>
          <p className="text-gray-600">Track shipments in real-time</p>
        </div>
      </div>
    </div>
  );
};

export default TMS;