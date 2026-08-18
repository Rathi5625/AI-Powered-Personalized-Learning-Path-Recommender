import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AppLayout } from './components/layout/AppLayout';
import {
  CareerRoute,
  OnboardingRoute,
  ProtectedRoute,
  PublicOnlyRoute,
} from './components/layout/ProtectedRoute';
import { AuthProvider } from './context/AuthContext';
import { LoginPage, OnboardingPage, SignupPage } from './pages/AuthPages';
import { AdaptiveLearningPage } from './pages/AdaptiveLearningPage';
import { CareerSelectionPage } from './pages/CareerSelectionPage';
import { DashboardPage } from './pages/DashboardPage';
import { LandingPage } from './pages/LandingPage';
import { LearningPathPage } from './pages/LearningPathPage';
import { ProfilePage } from './pages/ProfilePage';
import { ProgressPage } from './pages/ProgressPage';
import { RecommendationsPage } from './pages/RecommendationsPage';
import { SkillGapPage } from './pages/SkillGapPage';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<LandingPage />} />

          <Route element={<PublicOnlyRoute />}>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/signup" element={<SignupPage />} />
          </Route>

          <Route element={<OnboardingRoute />}>
            <Route path="/onboarding" element={<OnboardingPage />} />
          </Route>

          <Route element={<CareerRoute />}>
            <Route path="/career-selection" element={<CareerSelectionPage />} />
          </Route>

          <Route element={<ProtectedRoute />}>
            <Route element={<AppLayout />}>
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/skill-gap" element={<SkillGapPage />} />
              <Route path="/recommendations" element={<RecommendationsPage />} />
              <Route path="/learning-path" element={<LearningPathPage />} />
              <Route path="/progress" element={<ProgressPage />} />
              <Route path="/adaptive-learning" element={<AdaptiveLearningPage />} />
              <Route path="/profile" element={<ProfilePage />} />
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
