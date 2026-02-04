import React from 'react';
import { useSecurity } from '../../core/security/SecurityProvider';

const Documents: React.FC = () => {
  const { trustScore } = useSecurity();

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg p-6 shadow-sm">
        <h2 className="text-3xl font-bold text-gray-900 mb-2">Document Management</h2>
        <p className="text-gray-600">Manage and organize all business documents.</p>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Document Storage</h3>
          <p className="text-gray-600">Secure document storage</p>
        </div>
        
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Document Sharing</h3>
          <p className="text-gray-600">Share documents securely</p>
        </div>
        
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Document Search</h3>
          <p className="text-gray-600">Search and retrieve documents</p>
        </div>
      </div>
    </div>
  );
};

export default Documents;