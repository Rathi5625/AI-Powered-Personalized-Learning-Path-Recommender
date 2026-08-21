import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { api, getStoredToken, setStoredToken, clearStoredToken } from '../api/client';
import type { AuthenticatedUserResponse, LoginRequest, AuthResponse } from '../api/types';

interface AuthContextType {
  user: AuthenticatedUserResponse | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginRequest) => Promise<AuthResponse>;
  logout: () => void;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setTokenState] = useState<string | null>(getStoredToken());
  const [user, setUser] = useState<AuthenticatedUserResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const refreshUser = useCallback(async () => {
    const currentToken = getStoredToken();
    if (!currentToken) {
      setUser(null);
      setTokenState(null);
      setIsLoading(false);
      return;
    }

    try {
      const me = await api.getMe();
      setUser(me);
      setTokenState(currentToken);
    } catch {
      clearStoredToken();
      setUser(null);
      setTokenState(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    refreshUser();

    const handleUnauthorized = () => {
      clearStoredToken();
      setUser(null);
      setTokenState(null);
    };

    window.addEventListener('auth:unauthorized', handleUnauthorized);
    return () => window.removeEventListener('auth:unauthorized', handleUnauthorized);
  }, [refreshUser]);

  const login = async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await api.login(credentials);
    if (response.accessToken) {
      setStoredToken(response.accessToken);
      setTokenState(response.accessToken);
      
      // Fetch full authenticated user profile
      try {
        const me = await api.getMe();
        setUser(me);
      } catch {
        // Fallback to basic user from login response if getMe fails
        setUser({
          id: response.user.id,
          name: response.user.name,
          email: response.user.email,
          role: response.user.role,
          targetCareer: null,
          experienceLevel: null,
          dailyLearningHours: null,
          learningStyle: null,
          preferredContentType: null,
        });
      }
    }
    return response;
  };

  const logout = () => {
    clearStoredToken();
    setUser(null);
    setTokenState(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        isLoading,
        login,
        logout,
        refreshUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
