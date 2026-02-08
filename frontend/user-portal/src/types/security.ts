export interface User {
  id: string
  username: string
  email: string
  roles: string[]
  trustScore: number
  lastLogin: string
  isActive: boolean
}

export interface SecurityContext {
  user: User | null
  isAuthenticated: boolean
  isLoading: boolean
  trustScore: number
  lastAuthTime: number
  isStepUpRequired: boolean
  stepUpReason: string | null
}

export interface SecurityActions {
  login: (username: string, password: string) => Promise<void>
  logout: () => void
  refreshTrustScore: () => Promise<void>
  triggerStepUpAuth: (reason: string) => void
  completeStepUpAuth: () => void
  checkBiometricException: () => void
}

export type SecurityState = SecurityContext & SecurityActions

export interface BiometricException {
  type: 'BIOMETRIC_FAILURE' | 'MULTIPLE_ATTEMPTS' | 'SUSPICIOUS_ACTIVITY'
  message: string
  timestamp: number
  severity: 'LOW' | 'MEDIUM' | 'HIGH'
}

export interface TrustScoreUpdate {
  score: number
  factors: string[]
  timestamp: number
}