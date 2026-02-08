import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { SERVICES } from '../config/services';
import { useSecurity } from '../core/security/SecurityProvider';

const Sidebar: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { trustScore } = useSecurity();

  const currentServiceId = location.pathname.split('/')[1] || 'marketplace';

  const getTrustScoreColor = (score: number) => {
    if (score >= 90) return 'bg-green-500';
    if (score >= 70) return 'bg-yellow-500';
    return 'bg-red-500';
  };

  return (
    <div className="w-64 bg-gray-900 text-white h-screen fixed left-0 top-0 flex flex-col">
      <div className="p-4 border-b border-gray-700">
        <h1 className="text-xl font-bold">Logistics Portal</h1>
        <div className="mt-2 flex items-center gap-2">
          <div className="w-2 h-2 rounded-full bg-green-400 animate-pulse"></div>
          <span className="text-sm text-gray-400">Online</span>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto py-4">
        <nav>
          {SERVICES.map((service) => {
            const Icon = require('lucide-react').icons[service.icon];
            const isActive = currentServiceId === service.id;
            
            return (
              <button
                key={service.id}
                onClick={() => navigate(`/${service.id}`)}
                className={`w-full flex items-center px-4 py-3 text-left hover:bg-gray-800 transition-colors ${
                  isActive ? 'bg-gray-800 border-r-2 border-blue-500' : ''
                }`}
              >
                <Icon className="w-5 h-5 mr-3" />
                <span>{service.name}</span>
              </button>
            );
          })}
        </nav>
      </div>

      <div className="p-4 border-t border-gray-700">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm text-gray-400">Trust Score</p>
            <div className="flex items-center gap-2 mt-1">
              <div className="w-16 bg-gray-700 rounded-full h-2">
                <div
                  className={`h-2 rounded-full transition-all ${getTrustScoreColor(trustScore)}`}
                  style={{ width: `${trustScore}%` }}
                ></div>
              </div>
              <span className="text-sm font-medium">{trustScore}/100</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Sidebar;