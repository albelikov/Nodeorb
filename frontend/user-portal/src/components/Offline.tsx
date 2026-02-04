import React from 'react';
import { Wifi, WifiOff, RefreshCw } from 'lucide-react';

const Offline: React.FC = () => {
  const handleRetry = () => {
    if (navigator.onLine) {
      window.location.reload();
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-4">
      <div className="bg-white rounded-lg shadow-lg p-8 max-w-md w-full text-center">
        <div className="mb-6">
          {navigator.onLine ? (
            <Wifi className="w-16 h-16 text-green-500 mx-auto" />
          ) : (
            <WifiOff className="w-16 h-16 text-red-500 mx-auto" />
          )}
        </div>
        
        <h1 className="text-2xl font-bold text-gray-900 mb-2">
          {navigator.onLine ? 'Connection Restored' : 'No Internet Connection'}
        </h1>
        
        <p className="text-gray-600 mb-6">
          {navigator.onLine 
            ? 'Your connection has been restored. You can continue using the application.'
            : 'Please check your internet connection and try again.'
          }
        </p>

        {!navigator.onLine && (
          <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6">
            <p className="text-yellow-800 text-sm">
              Any appeals you submit while offline will be queued and sent automatically when your connection is restored.
            </p>
          </div>
        )}

        <button
          onClick={handleRetry}
          className={`w-full py-3 px-4 rounded-md font-medium flex items-center justify-center gap-2 ${
            navigator.onLine 
              ? 'bg-green-600 text-white hover:bg-green-700' 
              : 'bg-blue-600 text-white hover:bg-blue-700'
          }`}
        >
          <RefreshCw className="w-4 h-4" />
          {navigator.onLine ? 'Continue' : 'Check Connection'}
        </button>
      </div>
    </div>
  );
};

export default Offline;