import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { SecurityProvider } from './core/security/SecurityProvider';
import Layout from './components/Layout';
import Offline from './components/Offline';
import { SERVICES } from './config/services';

// Create basic service components
const ServicePage: React.FC<{ serviceId: string }> = ({ serviceId }) => {
  const service = SERVICES.find(s => s.id === serviceId);
  
  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg p-6 shadow-sm">
        <h2 className="text-3xl font-bold text-gray-900 mb-2">
          {service?.name || 'Service'}
        </h2>
        <p className="text-gray-600">
          Welcome to the {service?.name || 'service'} module. This is where the main functionality for this service would be implemented.
        </p>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Quick Actions</h3>
          <p className="text-gray-600">Common actions for this service</p>
        </div>
        
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Statistics</h3>
          <p className="text-gray-600">Key metrics and data</p>
        </div>
        
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Settings</h3>
          <p className="text-gray-600">Service configuration</p>
        </div>
      </div>
    </div>
  );
};

function App() {
  return (
    <Router>
      <SecurityProvider>
        <Routes>
          <Route path="/offline" element={<Offline />} />
          <Route path="/" element={<Layout />}>
            {SERVICES.map((service) => (
              <Route
                key={service.id}
                path={`/${service.id}`}
                element={<ServicePage serviceId={service.id} />}
              />
            ))}
            <Route path="*" element={<ServicePage serviceId="marketplace" />} />
          </Route>
        </Routes>
      </SecurityProvider>
    </Router>
  );
}

export default App;