import { create } from 'zustand';

interface Appeal {
  id: string;
  serviceId: string;
  reason: string;
  timestamp: number;
}

interface OfflineQueueState {
  queue: Appeal[];
  addToQueue: (appeal: Omit<Appeal, 'id' | 'timestamp'>) => void;
  removeFromQueue: (id: string) => void;
  clearQueue: () => void;
  processQueue: () => Promise<void>;
}

export const useOfflineQueue = create<OfflineQueueState>((set, get) => ({
  queue: [],
  addToQueue: (appeal) => {
    const newAppeal: Appeal = {
      ...appeal,
      id: Math.random().toString(36).substr(2, 9),
      timestamp: Date.now()
    };
    set((state) => ({ queue: [...state.queue, newAppeal] }));
  },
  removeFromQueue: (id) => {
    set((state) => ({ queue: state.queue.filter(item => item.id !== id) }));
  },
  clearQueue: () => set({ queue: [] }),
  processQueue: async () => {
    const state = get();
    if (state.queue.length === 0) return;

    // Try to send each appeal to SCM service
    for (const appeal of state.queue) {
      try {
        // This would be implemented with actual API call
        console.log('Sending appeal:', appeal);
        // await sendAppealToSCM(appeal);
        get().removeFromQueue(appeal.id);
      } catch (error) {
        console.error('Failed to send appeal:', error);
        break; // Stop processing if network is still unavailable
      }
    }
  }
}));