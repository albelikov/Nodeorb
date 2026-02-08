import React, { createContext, useContext, useEffect, ReactNode } from 'react'
import { useSecurityStore } from '../store/securityStore'
import StepUpAuthModal from '../components/StepUpAuthModal'
import { BiometricException } from '../types/security'

interface SecurityProviderProps {
  children: ReactNode
}

interface SecurityContextValue {
  // Expose store methods directly
  user: any
  isAuthenticated: boolean
  isLoading: boolean
  trustScore: number
  lastAuthTime: number
  isStepUpRequired: boolean
  stepUpReason: string | null
  login: (username: string, password: string) => Promise<void>
  logout: () => void
  refreshTrustScore: () => Promise<void>
  triggerStepUpAuth: (reason: string) => void
  completeStepUpAuth: () => void
  checkBiometricException: () => void
}

const SecurityContext = createContext<SecurityContextValue | null>(null)

export const useSecurity = () => {
  const context = useContext(SecurityContext)
  if (!context) {
    throw new Error('useSecurity must be used within a SecurityProvider')
  }
  return context
}

const SecurityProvider: React.FC<SecurityProviderProps> = ({ children }) => {
  const store = useSecurityStore()

  // Listen for BiometricException events
  useEffect(() => {
    const handleBiometricException = (event: CustomEvent<BiometricException>) => {
      store.handleBiometricException(event.detail)
    }

    // Listen for custom biometric exception events
    window.addEventListener('biometric-exception', handleBiometricException as EventListener)
    
    // Also listen for global errors that might indicate biometric issues
    const originalOnError = window.onerror
    window.onerror = (message, source, lineno, colno, error) => {
      if (message && message.toString().includes('biometric')) {
        store.handleBiometricException({
          type: 'BIOMETRIC_FAILURE',
          message: message.toString(),
          timestamp: Date.now(),
          severity: 'MEDIUM'
        })
      }
      return originalOnError?.(message, source, lineno, colno, error)
    }

    return () => {
      window.removeEventListener('biometric-exception', handleBiometricException as EventListener)
      window.onerror = originalOnError
    }
  }, [store])

  // Auto-refresh trust score periodically
  useEffect(() => {
    if (store.isAuthenticated) {
      const interval = setInterval(() => {
        store.refreshTrustScore()
      }, 5 * 60 * 1000) // Every 5 minutes

      return () => clearInterval(interval)
    }
  }, [store.isAuthenticated, store.refreshTrustScore])

  const contextValue: SecurityContextValue = {
    user: store.user,
    isAuthenticated: store.isAuthenticated,
    isLoading: store.isLoading,
    trustScore: store.trustScore,
    lastAuthTime: store.lastAuthTime,
    isStepUpRequired: store.isStepUpRequired,
    stepUpReason: store.stepUpReason,
    login: store.login,
    logout: store.logout,
    refreshTrustScore: store.refreshTrustScore,
    triggerStepUpAuth: store.triggerStepUpAuth,
    completeStepUpAuth: store.completeStepUpAuth,
    checkBiometricException: store.checkBiometricException
  }

  return (
    <SecurityContext.Provider value={contextValue}>
      {children}
      <StepUpAuthModal 
        isOpen={store.isStepUpRequired}
        onSuccess={store.completeStepUpAuth}
        onCancel={() => {
          // Handle cancel - maybe logout or just close the modal
          store.triggerStepUpAuth('') // Clear the step up requirement
        }}
      />
    </SecurityContext.Provider>
  )
}

export default SecurityProvider
