import React from 'react';
import { useSecurity } from '../../core/security/SecurityProvider';

const Support: React.FC = () => {
  const { trustScore } = useSecurity();

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg p-6 shadow-sm">
        <h2 className="text-3xl font-bold text-gray-900 mb-2">Support</h2>
        <p className="text-gray-600">Get help and support for all services.</p>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Help Center</h3>
          <p className="text-gray-600">Find answers to common questions</p>
        </div>
        
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Contact Support</h3>
          <p className="text-gray-600">Get in touch with our support team</p>
        </div>
        
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">System Status</h3>
          <p className="text-gray-600">Check system status and updates</p>
        </div>
      </div>
    </div>
  );
};

export default Support;