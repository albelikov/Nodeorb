import React, { useState } from 'react';
import { DollarSign, Package, AlertCircle } from 'lucide-react';
import axios from 'axios';

interface BidFormProps {
  onBidPlaced: (bid: { amount: number; assetId: string }) => void;
}

const BidForm: React.FC<BidFormProps> = ({ onBidPlaced }) => {
  const [amount, setAmount] = useState('');
  const [assetId, setAssetId] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    try {
      // Mock API endpoint - would be replaced with actual SCM endpoint
      const response = await axios.post('/api/scm/bid', {
        amount: parseFloat(amount),
        assetId
      });

      onBidPlaced({ amount: parseFloat(amount), assetId });
      setAmount('');
      setAssetId('');
    } catch (error: any) {
      if (error.response?.status === 401 || error.response?.status === 403) {
        const errorCode = error.response?.data?.code;
        if (errorCode === 'BIOMETRIC_REQUIRED') {
          // This will be handled by the parent component
          throw error;
        }
      }
      setError(error.response?.data?.message || 'Failed to place bid');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-sm p-6">
      <div className="flex items-center gap-3 mb-4">
        <DollarSign className="w-6 h-6 text-blue-600" />
        <h3 className="text-lg font-semibold text-gray-900">Place Your Bid</h3>
      </div>

      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-md">
          <div className="flex items-center gap-2 text-red-700">
            <AlertCircle className="w-4 h-4" />
            <span>{error}</span>
          </div>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Asset ID
          </label>
          <div className="relative">
            <Package className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
            <input
              type="text"
              value={assetId}
              onChange={(e) => setAssetId(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Enter asset ID"
              required
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Bid Amount ($)
          </label>
          <div className="relative">
            <DollarSign className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
            <input
              type="number"
              step="0.01"
              min="0"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="0.00"
              required
            />
          </div>
        </div>

        <button
          type="submit"
          disabled={isLoading || !amount || !assetId}
          className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {isLoading ? 'Placing Bid...' : 'Place Bid'}
        </button>
      </form>
    </div>
  );
};

export default BidForm;