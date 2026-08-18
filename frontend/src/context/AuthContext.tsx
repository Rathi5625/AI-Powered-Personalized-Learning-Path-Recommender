import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';
import { api } from '../api/client';
import type { AppSession, Career, OnboardingData, User, UserCreateRequest } from '../api/types';

const SESSION_KEY = 'learningpath_session';
const REGISTRY_KEY = 'learningpath_user_registry';

interface UserRegistryEntry {
  email: string;
  userId: string;
}

interface AuthContextValue {
  session: AppSession | null;
  loading: boolean;
  signup: (payload: UserCreateRequest) => Promise<User>;
  login: (email: string) => Promise<void>;
  logout: () => void;
  refreshUser: () => Promise<void>;
  setCareer: (career: Career) => void;
  completeOnboarding: (data: OnboardingData) => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function readSession(): AppSession | null {
  const raw = localStorage.getItem(SESSION_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AppSession;
  } catch {
    return null;
  }
}


function writeSession(session: AppSession | null) {
  if (session) {
    localStorage.setItem(SESSION_KEY, JSON.stringify(session));
  } else {
    localStorage.removeItem(SESSION_KEY);
  }
}

function readRegistry(): UserRegistryEntry[] {
  const raw = localStorage.getItem(REGISTRY_KEY);
  if (!raw) return [];
  try {
    return JSON.parse(raw) as UserRegistryEntry[];
  } catch {
    return [];
  }
}

function addToRegistry(email: string, userId: string) {
  const registry = readRegistry().filter((entry) => entry.email !== email.toLowerCase());
  registry.push({ email: email.toLowerCase(), userId });
  localStorage.setItem(REGISTRY_KEY, JSON.stringify(registry));
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AppSession | null>(() => readSession());
  const [loading, setLoading] = useState(false);

  const persist = useCallback((next: AppSession | null) => {
    setSession(next);
    writeSession(next);
  }, []);

  const signup = useCallback(
    async (payload: UserCreateRequest) => {
      setLoading(true);
      try {
        const user = await api.createUser(payload);
        addToRegistry(user.email, user.id);
        const nextSession: AppSession = {
          user,
          onboardingComplete: false,
        };
        persist(nextSession);
        return user;
      } finally {
        setLoading(false);
      }
    },
    [persist],
  );

  const login = useCallback(
    async (email: string) => {
      setLoading(true);
      try {
        const entry = readRegistry().find((item) => item.email === email.toLowerCase());
        if (!entry) {
          throw new Error('No account found for this email. Please sign up first.');
        }
        const user = await api.getUser(entry.userId);
        const existing = readSession();
        persist({
          user,
          careerId: existing?.user.id === user.id ? existing.careerId : undefined,
          careerName: existing?.user.id === user.id ? existing.careerName : undefined,
          onboardingComplete: Boolean(user.experienceLevel && user.learningStyle),
        });
      } finally {
        setLoading(false);
      }
    },
    [persist],
  );

  const logout = useCallback(() => persist(null), [persist]);

  const refreshUser = useCallback(async () => {
    if (!session?.user.id) return;
    const user = await api.getUser(session.user.id);
    persist({ ...session, user });
  }, [persist, session]);

  const setCareer = useCallback(
    (career: Career) => {
      if (!session) return;
      persist({
        ...session,
        careerId: career.id,
        careerName: career.name,
      });
    },
    [persist, session],
  );

  const completeOnboarding = useCallback(
    async (data: OnboardingData) => {
      if (!session) return;
      const updated = await api.updateUser(session.user.id, {
        name: session.user.name,
        careerGoal: session.careerName ?? session.user.careerGoal,
        ...data,
      });
      persist({
        ...session,
        user: updated,
        onboardingComplete: true,
      });
    },
    [persist, session],
  );

  const value = useMemo(
    () => ({
      session,
      loading,
      signup,
      login,
      logout,
      refreshUser,
      setCareer,
      completeOnboarding,
    }),
    [session, loading, signup, login, logout, refreshUser, setCareer, completeOnboarding],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}

export function useRequireAuth() {
  const { session } = useAuth();
  return session;
}
