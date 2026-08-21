import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
  GraduationCap,
  Mail,
  Lock,
  Eye,
  EyeOff,
  ArrowRight,
  Sparkles,
  AlertCircle,
  Loader2,
  CheckCircle,
} from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { LoginHeader } from '../components/layout/LoginHeader';
import { useAuth } from '../context/AuthContext';

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const { login, isAuthenticated } = useAuth();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  // If already authenticated, redirect to dashboard or home
  React.useEffect(() => {
    if (isAuthenticated) {
      navigate('/', { replace: true });
    }
  }, [isAuthenticated, navigate]);

  const showNotice = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 3500);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!email.trim()) {
      setErrorMessage('Please enter your email address.');
      return;
    }

    if (!password) {
      setErrorMessage('Please enter your password.');
      return;
    }

    setIsLoading(true);

    try {
      const authRes = await login({ email: email.trim(), password });
      const isOnboarded = Boolean(authRes.user?.onboardingCompleted);
      navigate(isOnboarded ? '/dashboard' : '/onboarding', { replace: true });
    } catch (err: unknown) {
      if (err instanceof Error) {
        if (
          err.message.toLowerCase().includes('email is not verified') ||
          err.message.includes('EMAIL_NOT_VERIFIED')
        ) {
          showNotice('Please verify your email to continue.');
          setTimeout(() => {
            navigate('/verify-email', {
              state: {
                email: email.trim().toLowerCase(),
                purpose: 'EMAIL_VERIFICATION',
              },
            });
          }, 800);
          return;
        }

        setErrorMessage(
          err.message.includes('401') || err.message.toLowerCase().includes('bad credentials')
            ? 'Invalid email or password.'
            : err.message
        );
      } else {
        setErrorMessage('Unable to connect to LearnAI. Please try again.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="relative min-h-screen flex flex-col justify-between overflow-x-hidden selection:bg-[#FFB091]/30 selection:text-[#9C5B33]">
      {/* Soft Ambient Glows */}
      <AmbientBackground />

      {/* Top Floating Pill Navigation */}
      <LoginHeader />

      {/* Main Login Card Section */}
      <main className="flex-1 flex flex-col items-center justify-center px-4 sm:px-6 py-8 sm:py-12 z-10">
        <motion.div
          initial={{ opacity: 0, scale: 0.96, y: 15 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
          className="w-full max-w-[460px] bg-white/80 backdrop-blur-2xl rounded-[32px] sm:rounded-[36px] p-7 sm:p-9 border border-white/90 shadow-[0_20px_60px_rgba(26,31,54,0.06)]"
        >
          {/* Top Graduation Icon */}
          <div className="w-12 h-12 rounded-2xl bg-[#F8EDE6] text-[#A06A42] flex items-center justify-center mx-auto mb-4 border border-[#F2DACB] shadow-xs">
            <GraduationCap className="w-6 h-6" />
          </div>

          {/* Heading */}
          <div className="text-center mb-6">
            <h1 className="text-2xl sm:text-3xl font-extrabold text-[#1A1F36] tracking-tight">
              Welcome back
            </h1>
            <p className="text-xs sm:text-sm text-gray-500 mt-1 font-normal">
              Continue your personalized learning journey.
            </p>
          </div>

          {/* Error Banner */}
          <AnimatePresence>
            {errorMessage && (
              <motion.div
                initial={{ opacity: 0, height: 0, y: -6 }}
                animate={{ opacity: 1, height: 'auto', y: 0 }}
                exit={{ opacity: 0, height: 0, y: -6 }}
                className="mb-4 p-3 rounded-xl bg-red-50/90 border border-red-200/80 text-red-700 text-xs flex items-center gap-2.5 overflow-hidden"
              >
                <AlertCircle className="w-4 h-4 shrink-0 text-red-500" />
                <span>{errorMessage}</span>
              </motion.div>
            )}
          </AnimatePresence>

          {/* Login Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Email Address Field */}
            <div>
              <label
                htmlFor="login-email"
                className="block text-xs sm:text-sm font-semibold text-[#1A1F36] mb-1.5"
              >
                Email Address
              </label>
              <div className="relative flex items-center">
                <Mail className="w-4 h-4 text-gray-400 absolute left-4 pointer-events-none" />
                <input
                  id="login-email"
                  type="email"
                  required
                  autoComplete="email"
                  placeholder="you@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  disabled={isLoading}
                  className="w-full h-12 pl-11 pr-4 rounded-xl sm:rounded-2xl bg-white border border-gray-200 text-[#1A1F36] placeholder:text-gray-400 text-xs sm:text-sm focus:outline-none focus:border-[#A06A42] focus:ring-3 focus:ring-[#A06A42]/10 transition-all disabled:opacity-60"
                />
              </div>
            </div>

            {/* Password Field */}
            <div>
              <div className="flex items-center justify-between mb-1.5">
                <label
                  htmlFor="login-password"
                  className="text-xs sm:text-sm font-semibold text-[#1A1F36]"
                >
                  Password
                </label>
                <Link
                  to="/forgot-password"
                  className="text-xs font-semibold text-[#A06A42] hover:text-[#8D5832] transition-colors cursor-pointer"
                >
                  Forgot password?
                </Link>
              </div>
              <div className="relative flex items-center">
                <Lock className="w-4 h-4 text-gray-400 absolute left-4 pointer-events-none" />
                <input
                  id="login-password"
                  type={showPassword ? 'text' : 'password'}
                  required
                  autoComplete="current-password"
                  placeholder="Enter your password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  disabled={isLoading}
                  className="w-full h-12 pl-11 pr-11 rounded-xl sm:rounded-2xl bg-white border border-gray-200 text-[#1A1F36] placeholder:text-gray-400 text-xs sm:text-sm focus:outline-none focus:border-[#A06A42] focus:ring-3 focus:ring-[#A06A42]/10 transition-all disabled:opacity-60"
                />
                <button
                  type="button"
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                  onClick={() => setShowPassword(!showPassword)}
                  className="w-8 h-8 flex items-center justify-center text-gray-400 hover:text-gray-600 absolute right-3 cursor-pointer transition-colors"
                >
                  {showPassword ? (
                    <EyeOff className="w-4 h-4" />
                  ) : (
                    <Eye className="w-4 h-4" />
                  )}
                </button>
              </div>
            </div>

            {/* Remember Me Checkbox */}
            <div className="flex items-center pt-1">
              <label className="flex items-center gap-2 cursor-pointer select-none text-xs sm:text-sm text-gray-600 font-medium">
                <input
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                  disabled={isLoading}
                  className="w-4 h-4 rounded border-gray-300 text-[#A06A42] focus:ring-[#A06A42] cursor-pointer accent-[#A06A42]"
                />
                <span>Remember me</span>
              </label>
            </div>

            {/* Sign In Primary Button */}
            <motion.button
              whileHover={{ scale: 1.01 }}
              whileTap={{ scale: 0.99 }}
              type="submit"
              disabled={isLoading}
              className="w-full h-12 rounded-xl sm:rounded-2xl bg-[#CC7D52] hover:bg-[#B86D44] active:bg-[#A05D36] text-white font-semibold text-xs sm:text-sm flex items-center justify-center gap-2 shadow-xs shadow-[#A06A42]/20 transition-all cursor-pointer mt-5 disabled:opacity-70 disabled:cursor-not-allowed"
            >
              {isLoading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin text-white" />
                  <span>Signing in...</span>
                </>
              ) : (
                <>
                  <span>Sign In</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </motion.button>
          </form>

          {/* Divider */}
          <div className="relative flex py-4 sm:py-5 items-center">
            <div className="flex-grow border-t border-gray-200/80"></div>
            <span className="flex-shrink mx-3 text-[11px] sm:text-xs text-gray-400 uppercase tracking-wider font-medium">
              or continue with
            </span>
            <div className="flex-grow border-t border-gray-200/80"></div>
          </div>

          {/* Google OAuth Button */}
          <button
            type="button"
            onClick={() => showNotice('Google sign-in is coming soon.')}
            className="w-full h-11 sm:h-12 rounded-xl sm:rounded-2xl bg-white hover:bg-gray-50/80 border border-gray-200/90 shadow-xs flex items-center justify-center gap-2.5 text-xs sm:text-sm font-semibold text-[#1A1F36] transition-all cursor-pointer"
          >
            {/* Google G Logo SVG */}
            <svg className="w-4 h-4" viewBox="0 0 24 24">
              <path
                fill="#4285F4"
                d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
              />
              <path
                fill="#34A853"
                d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
              />
              <path
                fill="#FBBC05"
                d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
              />
              <path
                fill="#EA4335"
                d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
              />
            </svg>
            <span>Continue with Google</span>
          </button>

          {/* Create Account Link */}
          <p className="text-xs sm:text-sm text-gray-500 text-center mt-5">
            Don’t have an account?{' '}
            <Link
              to="/signup"
              className="text-[#A06A42] font-semibold hover:underline"
            >
              Create an account
            </Link>
          </p>
        </motion.div>

        {/* Bottom Floating Pill Message */}
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.2 }}
          className="mt-6 bg-white/80 backdrop-blur-md rounded-full px-4 py-2 border border-white/90 shadow-xs flex items-center gap-2 text-xs text-gray-500 select-none"
        >
          <Sparkles className="w-3.5 h-3.5 text-[#FFB091]" />
          <span>Your personalized learning journey is waiting.</span>
        </motion.div>
      </main>

      {/* Non-blocking Toast Notification */}
      <AnimatePresence>
        {toastMessage && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 20 }}
            className="fixed bottom-6 left-1/2 -translate-x-1/2 bg-[#1A1F36] text-white text-xs font-medium px-4 py-2.5 rounded-full shadow-lg z-50 flex items-center gap-2"
          >
            <CheckCircle className="w-4 h-4 text-[#FFB091]" />
            <span>{toastMessage}</span>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};
