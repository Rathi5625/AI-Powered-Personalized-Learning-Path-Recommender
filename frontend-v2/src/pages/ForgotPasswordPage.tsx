import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Mail, KeyRound, Shield, Sparkles } from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { ForgotPasswordHeader } from '../components/layout/ForgotPasswordHeader';
import { api } from '../api/client';

export const ForgotPasswordPage: React.FC = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [toast, setToast] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const isValidEmail = email.trim().length > 0 && /\S+@\S+\.\S+/.test(email.trim());

  const showToast = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3200);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isValidEmail || isLoading) return;

    setIsLoading(true);
    try {
      await api.forgotPassword({ email: email.trim().toLowerCase() });
      showToast('Verification code sent.');

      setTimeout(() => {
        navigate('/verify-email', {
          state: {
            email: email.trim().toLowerCase(),
            purpose: 'PASSWORD_RESET',
          },
        });
      }, 1000);
    } catch (err: unknown) {
      if (err instanceof Error) {
        showToast(err.message);
      } else {
        showToast('Unable to process request. Please try again.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="relative min-h-screen flex flex-col justify-between overflow-x-hidden selection:bg-[#FFB091]/30 selection:text-[#9C5B33]">
      {/* Soft Ambient Background Glows */}
      <AmbientBackground />

      {/* Top Floating Header */}
      <ForgotPasswordHeader />

      {/* Main Centered Card */}
      <main className="flex-1 flex flex-col items-center justify-center px-4 sm:px-6 py-8 sm:py-12 z-10">
        <motion.div
          initial={{ opacity: 0, scale: 0.96, y: 16 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
          className="w-full max-w-[510px] bg-white/85 backdrop-blur-2xl rounded-[28px] sm:rounded-[32px] p-7 sm:p-10 border border-white/90 shadow-[0_20px_60px_rgba(26,31,54,0.06)]"
        >
          {/* Top Key / Lock Icon Container with subtle peach glow */}
          <motion.div
            initial={{ scale: 0.7, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ delay: 0.1, type: 'spring', stiffness: 320, damping: 22 }}
            className="w-14 h-14 rounded-full bg-[#FAF5F0] border border-[#F2DACB]/70 shadow-[0_4px_20px_rgba(204,125,82,0.12)] flex items-center justify-center mx-auto mb-5"
          >
            <KeyRound className="w-6 h-6 text-[#CC7D52]" />
          </motion.div>

          {/* Heading and Subtitle */}
          <div className="text-center mb-6 sm:mb-8">
            <h1 className="text-2xl sm:text-[28px] font-extrabold text-[#1A1F36] tracking-tight">
              Forgot your password?
            </h1>
            <p className="text-xs sm:text-sm text-gray-500 mt-2 max-w-sm mx-auto leading-relaxed font-normal">
              No worries. Enter your email and we&apos;ll send you a verification code to reset your password.
            </p>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-5">
            {/* Email Address Field */}
            <div>
              <label
                htmlFor="forgot-email"
                className="block text-xs sm:text-sm font-semibold text-[#1A1F36] mb-1.5"
              >
                Email address
              </label>
              <div className="relative flex items-center">
                <Mail className="w-4 h-4 text-gray-400 absolute left-4 pointer-events-none" />
                <input
                  id="forgot-email"
                  type="email"
                  required
                  autoComplete="email"
                  placeholder="you@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  disabled={isLoading}
                  className="w-full h-12 sm:h-13 pl-11 pr-4 rounded-xl sm:rounded-2xl bg-white border border-gray-200 text-[#1A1F36] placeholder:text-gray-400 text-xs sm:text-sm focus:outline-none focus:border-[#CC7D52] focus:ring-3 focus:ring-[#CC7D52]/10 transition-all disabled:opacity-60"
                />
              </div>
            </div>

            {/* Send Verification Code Button */}
            <motion.button
              whileHover={isValidEmail && !isLoading ? { scale: 1.01 } : {}}
              whileTap={isValidEmail && !isLoading ? { scale: 0.99 } : {}}
              type="submit"
              disabled={!isValidEmail || isLoading}
              className="w-full h-12 sm:h-13 rounded-xl sm:rounded-2xl bg-[#CC7D52] hover:bg-[#B86D44] active:bg-[#A05D36] text-[#1A1F36] font-semibold text-xs sm:text-sm flex items-center justify-center gap-2 shadow-sm shadow-[#CC7D52]/20 transition-all cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed mt-2"
            >
              <span>{isLoading ? 'Sending Code...' : 'Send Verification Code'}</span>
            </motion.button>
          </form>

          {/* Divider */}
          <div className="relative flex py-5 sm:py-6 items-center">
            <div className="flex-grow border-t border-gray-200/80"></div>
            <span className="flex-shrink mx-3 text-xs text-gray-400 font-medium">
              or
            </span>
            <div className="flex-grow border-t border-gray-200/80"></div>
          </div>

          {/* Back to Sign In Link */}
          <p className="text-xs sm:text-sm text-gray-500 text-center">
            Remember your password?{' '}
            <Link
              to="/login"
              className="text-[#1A1F36] font-semibold hover:text-[#CC7D52] transition-colors"
            >
              Back to Sign In
            </Link>
          </p>

          {/* Security Information Footer */}
          <div className="flex items-center justify-center gap-1.5 mt-8 pt-4 border-t border-gray-100/80 text-[11px] sm:text-xs text-gray-400 select-none">
            <Shield className="w-3.5 h-3.5 text-gray-400" />
            <span>Your account information is securely protected</span>
          </div>
        </motion.div>
      </main>

      {/* Toast Notification */}
      <AnimatePresence>
        {toast && (
          <motion.div
            initial={{ opacity: 0, y: 18 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 18 }}
            className="fixed bottom-6 left-1/2 -translate-x-1/2 bg-[#1A1F36] text-white text-xs font-medium px-4 py-2.5 rounded-full shadow-lg z-50 flex items-center gap-2 whitespace-nowrap"
          >
            <Sparkles className="w-3.5 h-3.5 text-[#FFB091]" />
            <span>{toast}</span>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};
