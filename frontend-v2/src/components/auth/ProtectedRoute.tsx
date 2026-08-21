import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { Loader2 } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

interface ProtectedRouteProps {
  children?: React.ReactNode;
  requireOnboarding?: boolean;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  requireOnboarding = true,
}) => {
  const { user, isAuthenticated, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#FDFBF9]">
        <div className="flex flex-col items-center gap-3">
          <Loader2 className="w-8 h-8 animate-spin text-[#8E4D2B]" />
          <span className="text-sm font-medium text-[#7D6E66]">Loading LearnAI...</span>
        </div>
      </div>
    );
  }

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (user.emailVerified === false) {
    return (
      <Navigate
        to="/verify-email"
        replace
        state={{ email: user.email, purpose: 'EMAIL_VERIFICATION' }}
      />
    );
  }

  const isOnboarded =
    user.onboardingCompleted ??
    (Boolean(user.targetCareer) && Boolean(user.experienceLevel));

  if (requireOnboarding && !isOnboarded) {
    return <Navigate to="/onboarding" replace />;
  }

  return children ? <>{children}</> : null;
};
