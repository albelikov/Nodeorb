import React from 'react';
import { X, Fingerprint, ShieldCheck } from 'lucide-react';
import { motion } from 'framer-motion';

interface StepUpAuthModalProps {
  isOpen: boolean;
  onSuccess: () => void;
  onCancel: () => void;
}

const StepUpAuthModal: React.FC<StepUpAuthModalProps> = ({ isOpen, onSuccess, onCancel }) => {
  if (!isOpen) return null;

  const handleAuth = async () => {
    try {
      // Trigger biometric authentication
      if (window.PublicKeyCredential) {
        const challenge = Uint8Array.from(
          Array.from({ length: 32 }, () => Math.floor(Math.random() * 256))
        );

        const credentialRequestOptions: PublicKeyCredentialRequestOptions = {
          challenge,
          allowCredentials: [],
          userVerification: 'required',
          timeout: 60000
        };

        const credential = await navigator.credentials.get({
          publicKey: credentialRequestOptions
        });

        if (credential) {
          onSuccess();
        } else {
          onCancel();
        }
      } else {
        alert('Biometric authentication is not supported in this browser');
        onCancel();
      }
    } catch (error) {
      console.error('Authentication failed:', error);
      onCancel();
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
    >
      <motion.div
        initial={{ scale: 0.9, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        exit={{ scale: 0.9, opacity: 0 }}
        transition={{ type: 'spring', damping: 25, stiffness: 300 }}
        className="bg-white rounded-lg p-6 w-full max-w-md mx-4"
      >
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-semibold flex items-center">
            <ShieldCheck className="w-5 h-5 mr-2 text-blue-600" />
            Step-Up Authentication Required
          </h2>
          <button
            onClick={onCancel}
            className="text-gray-400 hover:text-gray-600"
          >
            <X className="w-6 h-6" />
          </button>
        </div>
        
        <div className="mb-6">
          <p className="text-gray-600 mb-4">
            For security reasons, please authenticate using your biometric credentials.
          </p>
          
          <div className="bg-gray-50 rounded-lg p-4 flex items-center justify-center">
            <Fingerprint className="w-16 h-16 text-blue-600" />
          </div>
        </div>

        <div className="flex gap-3">
          <button
            onClick={onCancel}
            className="flex-1 px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
          >
            Cancel
          </button>
          <button
            onClick={handleAuth}
            className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            Authenticate
          </button>
        </div>
      </div>
    </div>
  );
};

export default StepUpAuthModal;