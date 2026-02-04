import React from 'react';
import { useSecurity } from '../../core/security/SecurityProvider';

const WMS: React.FC = () => {
  const { trustScore } = useSecurity();

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg p-6 shadow-sm">
        <h2 className="text-3xl font-bold text-gray-900 mb-2">Warehouse Management System</h2>
        <p className="text-gray-600">Manage warehouse operations, inventory, and logistics.</p>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Inventory Management</h3>
          <p className="text-gray-600">Track and manage warehouse inventory</p>
        </div>
        
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Receiving</h3>
          <p className="text-gray-600">Manage incoming shipments</p>
        </div>
        
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Shipping</h3>
          <p className="text-gray-600">Process outgoing shipments</p>
        </div>
      </div>
    </div>
  );
};

export default WMS;