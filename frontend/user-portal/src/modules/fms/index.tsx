import React from 'react';
import { useSecurity } from '../../core/security/SecurityProvider';

const FMS: React.FC = () => {
  const { trustScore } = useSecurity();

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg p-6 shadow-sm">
        <h2 className="text-3xl font-bold text-gray-900 mb-2">Financial Management System</h2>
        <p className="text-gray-600">Manage financial operations, payments, and accounting.</p>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Payments</h3>
          <p className="text-gray-600">Process and track payments</p>
        </div>
        
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Invoicing</h3>
          <p className="text-gray-600">Create and manage invoices</p>
        </div>
        
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Accounting</h3>
          <p className="text-gray-600">Financial reporting and accounting</p>
        </div>
      </div>
    </div>
  );
};

export default FMS;