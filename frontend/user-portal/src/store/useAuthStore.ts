import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AuthState {
  trustScore: number;
  isAuthenticated: boolean;
  sessionId: string | null;
  setTrustScore: (score: number) => void;
  setAuthenticated: (authenticated: boolean) => void;
  setSessionId: (sessionId: string | null) => void;
  reset: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      trustScore: 0,
      isAuthenticated: false,
      sessionId: null,
      setTrustScore: (score) => set({ trustScore: score }),
      setAuthenticated: (authenticated) => set({ isAuthenticated: authenticated }),
      setSessionId: (sessionId) => set({ sessionId }),
      reset: () => set({ trustScore: 0, isAuthenticated: false, sessionId: null })
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        trustScore: state.trustScore,
        isAuthenticated: state.isAuthenticated,
        sessionId: state.sessionId
      })
    }
  )
);