import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export function ProtectedRoute() {
  const { session } = useAuth();
  if (!session) return <Navigate to="/login" replace />;
  if (!session.onboardingComplete) return <Navigate to="/onboarding" replace />;
  if (!session.careerId) return <Navigate to="/career-selection" replace />;
  return <Outlet />;
}

export function AuthRoute() {
  const { session } = useAuth();
  if (!session) return <Navigate to="/login" replace />;
  return <Outlet />;
}

export function OnboardingRoute() {
  const { session } = useAuth();
  if (!session) return <Navigate to="/signup" replace />;
  if (session.onboardingComplete && !session.careerId) {
    return <Navigate to="/career-selection" replace />;
  }
  if (session.onboardingComplete && session.careerId) {
    return <Navigate to="/dashboard" replace />;
  }
  return <Outlet />;
}

export function CareerRoute() {
  const { session } = useAuth();
  if (!session) return <Navigate to="/login" replace />;
  if (!session.onboardingComplete) return <Navigate to="/onboarding" replace />;
  return <Outlet />;
}

export function PublicOnlyRoute() {
  const { session } = useAuth();
  if (session?.onboardingComplete && session.careerId) {
    return <Navigate to="/dashboard" replace />;
  }
  return <Outlet />;
}
