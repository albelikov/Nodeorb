import { create } from 'zustand'
import { SecurityState, User, BiometricException } from '../types/security'

interface SecurityStore extends SecurityState {
  setUser: (user: User | null) => void
  setLoading: (loading: boolean) => void
  setTrustScore: (score: number) => void
  setLastAuthTime: (time: number) => void
  setStepUpRequired: (required: boolean, reason?: string) => void
  handleBiometricException: (exception: BiometricException) => void
}

export const useSecurityStore = create<SecurityStore>((set, get) => ({
  // State
  user: null,
  isAuthenticated: false,
  isLoading: false,
  trustScore: 0,
  lastAuthTime: 0,
  isStepUpRequired: false,
  stepUpReason: null,

  // Actions
  setUser: (user) => set({ user, isAuthenticated: !!user }),
  
  setLoading: (loading) => set({ isLoading: loading }),
  
  setTrustScore: (score) => set({ trustScore: score }),
  
  setLastAuthTime: (time) => set({ lastAuthTime: time }),
  
  setStepUpRequired: (required, reason) => 
    set({ 
      isStepUpRequired: required, 
      stepUpReason: reason || null 
    }),

  handleBiometricException: (exception) => {
    const { trustScore } = get()
    
    // Log the exception
    console.warn('Biometric exception detected:', exception)
    
    // Trigger step-up authentication for high-severity exceptions
    if (exception.severity === 'HIGH' || exception.severity === 'MEDIUM') {
      get().setStepUpRequired(true, exception.message)
    }
    
    // Reduce trust score based on severity
    let newTrustScore = trustScore
    switch (exception.severity) {
      case 'HIGH':
        newTrustScore = Math.max(0, trustScore - 20)
        break
      case 'MEDIUM':
        newTrustScore = Math.max(0, trustScore - 10)
        break
      case 'LOW':
        newTrustScore = Math.max(0, trustScore - 5)
        break
    }
    
    set({ trustScore: newTrustScore })
  },

  // Security actions
  login: async (username: string, password: string) => {
    set({ isLoading: true })
    try {
      // Simulate API call to Keycloak/SCM
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      })
      
      if (!response.ok) throw new Error('Authentication failed')
      
      const userData = await response.json()
      set({
        user: userData.user,
        isAuthenticated: true,
        trustScore: userData.trustScore,
        lastAuthTime: Date.now(),
        isLoading: false
      })
    } catch (error) {
      set({ isLoading: false })
      throw error
    }
  },

  logout: () => {
    set({
      user: null,
      isAuthenticated: false,
      trustScore: 0,
      lastAuthTime: 0,
      isStepUpRequired: false,
      stepUpReason: null
    })
  },

  refreshTrustScore: async () => {
    try {
      const response = await fetch('/api/security/trust-score')
      const data = await response.json()
      set({ trustScore: data.score })
    } catch (error) {
      console.error('Failed to refresh trust score:', error)
    }
  },

  triggerStepUpAuth: (reason: string) => {
    set({ isStepUpRequired: true, stepUpReason: reason })
  },

  completeStepUpAuth: () => {
    set({ 
      isStepUpRequired: false, 
      stepUpReason: null,
      lastAuthTime: Date.now()
    })
  },

  checkBiometricException: () => {
    // This would be called by components when they detect biometric issues
    // For now, we'll simulate it
    const exception: BiometricException = {
      type: 'BIOMETRIC_FAILURE',
      message: 'Biometric authentication failed',
      timestamp: Date.now(),
      severity: 'MEDIUM'
    }
    get().handleBiometricException(exception)
  }
}))