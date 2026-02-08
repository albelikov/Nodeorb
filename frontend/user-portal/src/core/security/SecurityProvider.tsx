import React, { createContext, useContext, useState, useEffect } from 'react';
import { useAuthStore } from '../../store/useAuthStore';
import StepUpAuthModal from '../../components/StepUpAuthModal';

interface SecurityContextType {
  isAuthenticated: boolean;
  trustScore: number;
  sessionId: string | null;
  authenticate: () => Promise<boolean>;
  logout: () => void;
  triggerStepUpAuth: (callback?: () => void) => void;
}

const SecurityContext = createContext<SecurityContextType | undefined>(undefined);

export const useSecurity = () => {
  const context = useContext(SecurityContext);
  if (!context) {
    throw new Error('useSecurity must be used within a SecurityProvider');
  }
  return context;
};

interface SecurityProviderProps {
  children: React.ReactNode;
}

export const SecurityProvider: React.FC<SecurityProviderProps> = ({ children }) => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [pendingAuth, setPendingAuth] = useState<(() => void) | null>(null);

  const { isAuthenticated, trustScore, sessionId, setAuthenticated, setTrustScore, setSessionId } = useAuthStore();

  const authenticate = async (): Promise<boolean> => {
    try {
      // Check if WebAuthn is supported
      if (!window.PublicKeyCredential) {
        console.error('WebAuthn is not supported in this browser');
        return false;
      }

      // Generate challenge from server (mock for now)
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
      }) as PublicKeyCredential;

      if (credential) {
        // Verify with server (mock for now)
        setAuthenticated(true);
        setTrustScore(100);
        setSessionId(Math.random().toString(36).substr(2, 9));
        return true;
      }

      return false;
    } catch (error) {
      console.error('Authentication failed:', error);
      return false;
    }
  };

  const logout = () => {
    setAuthenticated(false);
    setTrustScore(0);
    setSessionId(null);
  };

  const handleBiometricRequired = (callback?: () => void) => {
    setPendingAuth(() => callback || (() => {}));
    setIsModalOpen(true);
  };

  const handleAuthSuccess = () => {
    setIsModalOpen(false);
    if (pendingAuth) {
      pendingAuth();
      setPendingAuth(null);
    }
  };

  const handleAuthCancel = () => {
    setIsModalOpen(false);
    setPendingAuth(null);
  };

  // Listen for network status changes
  useEffect(() => {
    const handleOnline = () => {
      // Process offline queue when network is restored
      // This would be implemented with actual queue processing
      console.log('Network restored, processing offline queue');
    };

    window.addEventListener('online', handleOnline);
    return () => window.removeEventListener('online', handleOnline);
  }, []);

  const contextValue: SecurityContextType = {
    isAuthenticated,
    trustScore,
    sessionId,
    authenticate,
    logout,
    triggerStepUpAuth: handleBiometricRequired
  };

  return (
    <SecurityContext.Provider value={contextValue}>
      {children}
      <StepUpAuthModal
        isOpen={isModalOpen}
        onSuccess={handleAuthSuccess}
        onCancel={handleAuthCancel}
      />
    </SecurityContext.Provider>
  );
};