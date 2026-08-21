import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import { LandingPage } from './pages/LandingPage';
import { LoginPage } from './pages/LoginPage';
import { SignupPage } from './pages/SignupPage';
import { ForgotPasswordPage } from './pages/ForgotPasswordPage';
import { VerifyEmailPage } from './pages/VerifyEmailPage';
import { ResetPasswordPage } from './pages/ResetPasswordPage';
import { PasswordResetSuccessPage } from './pages/PasswordResetSuccessPage';
import { OnboardingStep1Page } from './pages/OnboardingStep1Page';
import { OnboardingStep2Page } from './pages/OnboardingStep2Page';
import { OnboardingStep3Page } from './pages/OnboardingStep3Page';
import { OnboardingStep4Page } from './pages/OnboardingStep4Page';
import { OnboardingStep5Page } from './pages/OnboardingStep5Page';
import { OnboardingStep6Page } from './pages/OnboardingStep6Page';
import { OnboardingStep7Page } from './pages/OnboardingStep7Page';
import { BuildingPathPage } from './pages/BuildingPathPage';
import { MyLearningPathPage } from './pages/MyLearningPathPage';
import { DashboardPage } from './pages/DashboardPage';
import { ExploreCoursesPage } from './pages/ExploreCoursesPage';
import { CourseDetailsPage } from './pages/CourseDetailsPage';
import { SkillsPage } from './pages/SkillsPage';
import { AssessmentsPage } from './pages/AssessmentsPage';
import { AssessmentPage } from './AssessmentPage';
import { AssessmentResultsPage } from './pages/AssessmentResultsPage';
import { ProjectDetailsPage } from './pages/ProjectDetailsPage';
import { AIMentorPage } from './pages/AIMentorPage';
import { ProgressPage } from './pages/ProgressPage';
import { ProfilePage } from './pages/ProfilePage';
import { SettingsPage } from './pages/SettingsPage';
import { HelpSupportPage } from './pages/HelpSupportPage';
import { NotificationsPage } from './pages/NotificationsPage';

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Public Auth & Marketing Routes */}
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/verify-email" element={<VerifyEmailPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />
          <Route path="/password-reset-success" element={<PasswordResetSuccessPage />} />

          {/* Onboarding Routes (Protected: Requires Authenticated + Email Verified) */}
          <Route
            path="/onboarding"
            element={
              <ProtectedRoute requireOnboarding={false}>
                <OnboardingStep1Page />
              </ProtectedRoute>
            }
          />
          <Route
            path="/onboarding/step-1"
            element={
              <ProtectedRoute requireOnboarding={false}>
                <OnboardingStep1Page />
              </ProtectedRoute>
            }
          />
          <Route
            path="/onboarding/step-2"
            element={
              <ProtectedRoute requireOnboarding={false}>
                <OnboardingStep2Page />
              </ProtectedRoute>
            }
          />
          <Route
            path="/onboarding/step-3"
            element={
              <ProtectedRoute requireOnboarding={false}>
                <OnboardingStep3Page />
              </ProtectedRoute>
            }
          />
          <Route
            path="/onboarding/step-4"
            element={
              <ProtectedRoute requireOnboarding={false}>
                <OnboardingStep4Page />
              </ProtectedRoute>
            }
          />
          <Route
            path="/onboarding/step-5"
            element={
              <ProtectedRoute requireOnboarding={false}>
                <OnboardingStep5Page />
              </ProtectedRoute>
            }
          />
          <Route
            path="/onboarding/step-6"
            element={
              <ProtectedRoute requireOnboarding={false}>
                <OnboardingStep6Page />
              </ProtectedRoute>
            }
          />
          <Route
            path="/onboarding/step-7"
            element={
              <ProtectedRoute requireOnboarding={false}>
                <OnboardingStep7Page />
              </ProtectedRoute>
            }
          />
          <Route
            path="/building-path"
            element={
              <ProtectedRoute requireOnboarding={false}>
                <BuildingPathPage />
              </ProtectedRoute>
            }
          />

          {/* Application Routes (Protected: Requires Authenticated + Email Verified + Onboarded) */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute requireOnboarding={true}>
                <DashboardPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/learning-path"
            element={
              <ProtectedRoute requireOnboarding={true}>
                <MyLearningPathPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/explore-courses"
            element={
              <ProtectedRoute requireOnboarding={true}>
                <ExploreCoursesPage />
              </ProtectedRoute>
            }
          />
          <Route path="/explore" element={<Navigate to="/explore-courses" replace />} />
          <Route path="/courses" element={<Navigate to="/explore-courses" replace />} />
          <Route
            path="/course-details"
            element={
              <ProtectedRoute requireOnboarding={true}>
                <CourseDetailsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/skills"
            element={
              <ProtectedRoute requireOnboarding={true}>
                <SkillsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/assessments"
            element={
              <ProtectedRoute requireOnboarding={true}>
                <AssessmentsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/assessment"
            element={
              <ProtectedRoute requireOnboarding={true}>
                <AssessmentPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/assessment-results"
            element={
              <ProtectedRoute requireOnboarding={true}>
                <AssessmentResultsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/project-details"
            element={
              <ProtectedRoute requireOnboarding={true}>
                <ProjectDetailsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/projects"
            element={
              <ProtectedRoute requireOnboarding={true}>
                <ProjectDetailsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/ai-mentor"
            element={
              <ProtectedRoute requireOnboarding={true}>
                <AIMentorPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/progress"
            element={
              <ProtectedRoute requireOnboarding={true}>
                <ProgressPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/profile"
            element={
              <ProtectedRoute requireOnboarding={true}>
                <ProfilePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/settings"
            element={
              <ProtectedRoute requireOnboarding={true}>
                <SettingsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/help-support"
            element={
              <ProtectedRoute requireOnboarding={true}>
                <HelpSupportPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/help"
            element={
              <ProtectedRoute requireOnboarding={true}>
                <HelpSupportPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/notifications"
            element={
              <ProtectedRoute requireOnboarding={true}>
                <NotificationsPage />
              </ProtectedRoute>
            }
          />

          {/* Catch-all fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
};

export default App;
