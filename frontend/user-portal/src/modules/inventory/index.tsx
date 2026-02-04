import React from 'react';
import { useSecurity } from '../../core/security/SecurityProvider';

const Inventory: React.FC = () => {
  const { trustScore } = useSecurity();

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg p-6 shadow-sm">
        <h2 className="text-3xl font-bold text-gray-900 mb-2">Inventory Management</h2>
        <p className="text-gray-600">Track and manage inventory across all locations.</p>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Stock Levels</h3>
          <p className="text-gray-600">Monitor inventory levels</p>
        </div>
        
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Reorder Points</h3>
          <p className="text-gray-600">Set and track reorder points</p>
        </div>
        
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Inventory Reports</h3>
          <p className="text-gray-600">Generate inventory reports</p>
        </div>
      </div>
    </div>
  );
};

export default Inventory;