import React from 'react';
import { useSecurity } from '../../core/security/SecurityProvider';

const ERP: React.FC = () => {
  const { trustScore } = useSecurity();

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg p-6 shadow-sm">
        <h2 className="text-3xl font-bold text-gray-900 mb-2">ERP System</h2>
        <p className="text-gray-600">Enterprise resource planning and business management.</p>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Business Intelligence</h3>
          <p className="text-gray-600">Analytics and reporting tools</p>
        </div>
        
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Resource Planning</h3>
          <p className="text-gray-600">Plan and allocate resources</p>
        </div>
        
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Process Management</h3>
          <p className="text-gray-600">Manage business processes</p>
        </div>
      </div>
    </div>
  );
};

export default ERP;