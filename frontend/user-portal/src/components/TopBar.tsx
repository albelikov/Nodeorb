import React from 'react';
import { useLocation } from 'react-router-dom';
import { SERVICES } from '../config/services';
import { useSecurity } from '../core/security/SecurityProvider';

const TopBar: React.FC = () => {
  const location = useLocation();
  const { trustScore, isAuthenticated } = useSecurity();

  const currentServiceId = location.pathname.split('/')[1] || 'marketplace';
  const currentService = SERVICES.find(s => s.id === currentServiceId);

  const getTrustScoreColor = (score: number) => {
    if (score >= 90) return 'bg-green-500';
    if (score >= 70) return 'bg-yellow-500';
    return 'bg-red-500';
  };

  return (
    <div className="bg-white border-b border-gray-200 px-6 py-4 shadow-sm">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <h1 className="text-2xl font-bold text-gray-900">
            {currentService?.name || 'Logistics Portal'}
          </h1>
          <div className="flex items-center gap-2 text-sm text-gray-500">
            <div className="w-2 h-2 bg-green-400 rounded-full animate-pulse"></div>
            <span>Online</span>
          </div>
        </div>

        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2">
            <span className="text-sm font-medium text-gray-700">Compliance Passport</span>
            <div className="flex items-center gap-2">
              <div className="w-24 bg-gray-200 rounded-full h-2">
                <div
                  className={`h-2 rounded-full transition-all ${getTrustScoreColor(trustScore)}`}
                  style={{ width: `${trustScore}%` }}
                ></div>
              </div>
              <span className="text-sm font-medium text-gray-900">{trustScore}/100</span>
            </div>
          </div>

          {isAuthenticated && (
            <div className="flex items-center gap-2 text-sm text-green-600">
              <div className="w-2 h-2 bg-green-500 rounded-full"></div>
              <span>Authenticated</span>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default TopBar;