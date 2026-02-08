import React, { useState } from 'react';
import { useSecurity } from '../../core/security/SecurityProvider';
import BidForm from './BidForm';

const Marketplace: React.FC = () => {
  const { trustScore, triggerStepUpAuth } = useSecurity();
  const [bids, setBids] = useState<Array<{ amount: number; assetId: string; timestamp: Date }>>([]);

  const handleBidPlaced = async (bid: { amount: number; assetId: string }) => {
    try {
      // Mock API call to SCM
      const response = await fetch('/api/scm/bid', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          amount: bid.amount,
          assetId: bid.assetId
        })
      });

      if (!response.ok) {
        const errorData = await response.json();
        
        if (response.status === 401 || response.status === 403) {
          if (errorData.code === 'BIOMETRIC_REQUIRED') {
            // Trigger step-up authentication
            triggerStepUpAuth(async () => {
              // Retry the bid after successful authentication
              try {
                const retryResponse = await fetch('/api/scm/bid', {
                  method: 'POST',
                  headers: {
                    'Content-Type': 'application/json',
                  },
                  body: JSON.stringify({
                    amount: bid.amount,
                    assetId: bid.assetId
                  })
                });

                if (retryResponse.ok) {
                  // Success - add bid to list
                  setBids(prev => [...prev, { ...bid, timestamp: new Date() }]);
                  console.log('Bid placed successfully after authentication');
                } else {
                  console.error('Failed to place bid after authentication');
                }
              } catch (retryError) {
                console.error('Error retrying bid:', retryError);
              }
            });
            return;
          }
        }
        
        throw new Error(errorData.message || 'Failed to place bid');
      }

      // Success - add bid to list
      setBids(prev => [...prev, { ...bid, timestamp: new Date() }]);
      console.log('Bid placed successfully');
    } catch (error) {
      console.error('Error placing bid:', error);
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg p-6 shadow-sm">
        <h2 className="text-3xl font-bold text-gray-900 mb-2">Marketplace</h2>
        <p className="text-gray-600">Find and book freight services, manage orders, and track shipments.</p>
      </div>
      
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <BidForm onBidPlaced={handleBidPlaced} />
        
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Recent Bids</h3>
          {bids.length === 0 ? (
            <p className="text-gray-500 text-center py-4">No bids placed yet</p>
          ) : (
            <div className="space-y-3">
              {bids.map((bid, index) => (
                <div key={index} className="border border-gray-200 rounded-lg p-4">
                  <div className="flex justify-between items-start mb-2">
                    <div>
                      <span className="font-medium text-gray-900">Asset: {bid.assetId}</span>
                      <p className="text-sm text-gray-600">Amount: ${bid.amount.toFixed(2)}</p>
                    </div>
                    <span className="text-xs text-gray-500">
                      {bid.timestamp.toLocaleString()}
                    </span>
                  </div>
                  <div className="flex gap-2">
                    <span className="px-2 py-1 bg-green-100 text-green-800 text-xs rounded-full">
                      Placed
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Available Loads</h3>
          <p className="text-gray-600">Browse available freight loads</p>
        </div>
        
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">My Orders</h3>
          <p className="text-gray-600">Manage your freight orders</p>
        </div>
        
        <div className="bg-white rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Track Shipments</h3>
          <p className="text-gray-600">Track your shipments in real-time</p>
        </div>
      </div>
    </div>
  );
};

export default Marketplace;
