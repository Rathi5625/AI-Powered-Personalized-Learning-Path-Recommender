import React, { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { MailCheck, Clock, ArrowLeft, ArrowRight, Sparkles, AlertCircle } from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { VerifyHeader } from '../components/layout/VerifyHeader';
import { OTPInput } from '../components/auth/OTPInput';
import { api, setStoredToken } from '../api/client';
import { useAuth } from '../context/AuthContext';
import type { OtpPurpose } from '../api/types';

/* ─── Helpers ─────────────────────────────────────────────────── */
const INITIAL_SECONDS = 60;

function maskEmail(email: string): string {
  if (!email || !email.includes('@')) return 'p•••••@example.com';
  const [user, domain] = email.split('@');
  if (user.length <= 1) return `${user}•••••@${domain}`;
  return `${user[0]}•••••@${domain}`;
}

function formatTime(s: number): string {
  const m = Math.floor(s / 60);
  const sec = s % 60;
  return `${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')}`;
}

/* ─── Page ────────────────────────────────────────────────────── */
export const VerifyEmailPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { refreshUser } = useAuth();

  const state = location.state as { email?: string; purpose?: OtpPurpose } | null;
  const email = state?.email || '';
  const purpose: OtpPurpose = state?.purpose || 'EMAIL_VERIFICATION';

  const maskedEmail = email ? maskEmail(email) : 'your email address';

  const [otp, setOtp] = useState<string[]>(Array(6).fill(''));
  const [seconds, setSeconds] = useState(INITIAL_SECONDS);
  const [canResend, setCanResend] = useState(false);
  const [toast, setToast] = useState<string | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [verifying, setVerifying] = useState(false);
  const [isResending, setIsResending] = useState(false);

  const allFilled = otp.every((d) => d !== '');

  /* Countdown timer for resend cooldown */
  useEffect(() => {
    if (seconds <= 0) {
      setCanResend(true);
      return;
    }
    setCanResend(false);
    const id = setTimeout(() => setSeconds((s) => s - 1), 1000);
    return () => clearTimeout(id);
  }, [seconds]);

  const showToast = useCallback((msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3500);
  }, []);

  /* Resend OTP */
  const handleResend = async () => {
    if (!canResend || isResending || !email) return;

    setIsResending(true);
    setErrorMsg(null);
    try {
      await api.resendOtp({ email, purpose });
      setOtp(Array(6).fill(''));
      setSeconds(INITIAL_SECONDS);
      showToast('A new verification code was sent to your inbox.');
    } catch (err: unknown) {
      if (err instanceof Error) {
        setErrorMsg(err.message);
        showToast(err.message);
      } else {
        showToast('Unable to resend code. Please try again.');
      }
    } finally {
      setIsResending(false);
    }
  };

  /* Verify OTP */
  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!allFilled || verifying) return;

    const rawOtp = otp.join('');
    setVerifying(true);
    setErrorMsg(null);

    try {
      if (purpose === 'PASSWORD_RESET') {
        const res = await api.verifyResetOtp({ email, otp: rawOtp });
        showToast('Code verified! Set your new password.');
        setTimeout(() => {
          navigate('/reset-password', {
            state: { resetToken: res.resetToken, email },
          });
        }, 800);
      } else {
        const authRes = await api.verifyEmailOtp({ email, otp: rawOtp });
        if (authRes.accessToken) {
          setStoredToken(authRes.accessToken);
          await refreshUser();
        }
        showToast('Email verified successfully! Welcome to LearnAI.');
        const isOnboarded = Boolean(authRes.user?.onboardingCompleted);
        setTimeout(() => {
          navigate(isOnboarded ? '/dashboard' : '/onboarding', { replace: true });
        }, 1000);
      }
    } catch (err: unknown) {
      if (err instanceof Error) {
        setErrorMsg(err.message);
        showToast(err.message);
      } else {
        setErrorMsg('Invalid verification code. Please try again.');
      }
    } finally {
      setVerifying(false);
    }
  };

  return (
    <div className="relative min-h-screen flex flex-col overflow-x-hidden selection:bg-[#FFB091]/30 selection:text-[#9C5B33]">
      {/* Ambient warm-lavender background */}
      <AmbientBackground />

      {/* Centered brand pill */}
      <VerifyHeader />

      {/* Main card */}
      <main className="flex-1 flex flex-col items-center justify-center px-4 sm:px-6 py-8 sm:py-14 z-10">
        <motion.div
          initial={{ opacity: 0, scale: 0.96, y: 18 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
          className="w-full max-w-[500px] bg-white/82 backdrop-blur-2xl rounded-[32px] sm:rounded-[36px] border border-white/90 shadow-[0_24px_64px_rgba(26,31,54,0.06)] px-7 sm:px-10 py-9 sm:py-11"
        >
          {/* Mail icon */}
          <motion.div
            initial={{ scale: 0.7, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ delay: 0.1, type: 'spring', stiffness: 320, damping: 22 }}
            className="w-12 h-12 rounded-full bg-[#F8EDE6] border border-[#F2DACB] flex items-center justify-center mx-auto mb-5"
          >
            <MailCheck className="w-5 h-5 text-[#CC7D52]" />
          </motion.div>

          {/* Heading */}
          <div className="text-center mb-6">
            <h1 className="text-2xl sm:text-[28px] font-extrabold text-[#1A1F36] tracking-tight">
              {purpose === 'PASSWORD_RESET' ? 'Enter reset code' : 'Verify your email'}
            </h1>
            <p className="text-xs sm:text-sm text-gray-500 mt-2 leading-relaxed">
              We've sent a 6-digit verification code to
            </p>
            <p className="text-xs sm:text-sm font-semibold text-[#1A1F36] mt-0.5">
              {maskedEmail}
            </p>
          </div>

          {/* Error message */}
          <AnimatePresence>
            {errorMsg && (
              <motion.div
                initial={{ opacity: 0, height: 0, y: -4 }}
                animate={{ opacity: 1, height: 'auto', y: 0 }}
                exit={{ opacity: 0, height: 0, y: -4 }}
                className="mb-4 p-3 rounded-xl bg-red-50/90 border border-red-200/80 text-red-700 text-xs flex items-center gap-2.5 overflow-hidden"
              >
                <AlertCircle className="w-4 h-4 shrink-0 text-red-500" />
                <span>{errorMsg}</span>
              </motion.div>
            )}
          </AnimatePresence>

          <form onSubmit={handleVerify}>
            {/* OTP Boxes */}
            <OTPInput value={otp} onChange={setOtp} disabled={verifying} />

            {/* Timer */}
            <div className="flex items-center justify-center gap-1.5 mt-5 text-xs text-gray-500">
              <Clock className="w-3.5 h-3.5 text-gray-400" />
              {canResend ? (
                <span className="text-emerald-600 font-medium">You can now request a new code</span>
              ) : (
                <span>
                  Resend available in{' '}
                  <span className="font-semibold text-[#1A1F36]">{formatTime(seconds)}</span>
                </span>
              )}
            </div>

            {/* Resend */}
            <p className="text-xs text-gray-500 text-center mt-2">
              Didn't receive the code?{' '}
              <button
                type="button"
                onClick={handleResend}
                disabled={!canResend || isResending}
                className={`font-semibold transition-colors ${
                  canResend
                    ? 'text-[#A06A42] hover:text-[#8D5832] cursor-pointer'
                    : 'text-gray-400 cursor-default'
                }`}
              >
                {isResending ? 'Sending...' : 'Resend code'}
              </button>
            </p>

            {/* Verify Button */}
            <motion.button
              type="submit"
              whileHover={allFilled && !verifying ? { scale: 1.01 } : {}}
              whileTap={allFilled && !verifying ? { scale: 0.99 } : {}}
              disabled={!allFilled || verifying}
              className="mt-6 w-full h-12 sm:h-14 rounded-xl sm:rounded-2xl bg-[#CC7D52] hover:bg-[#B86D44] active:bg-[#A05D36] text-white shadow-sm shadow-[#A06A42]/20 font-semibold text-xs sm:text-sm flex items-center justify-center gap-2 transition-all cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span>{verifying ? 'Verifying...' : 'Verify Code'}</span>
              {!verifying && <ArrowRight className="w-4 h-4" />}
            </motion.button>
          </form>

          {/* Back to login */}
          <div className="text-center mt-5">
            <Link
              to="/login"
              className="inline-flex items-center gap-1.5 text-xs sm:text-sm text-gray-500 hover:text-[#1A1F36] transition-colors"
            >
              <ArrowLeft className="w-3.5 h-3.5" />
              <span>Back to login</span>
            </Link>
          </div>
        </motion.div>
      </main>

      {/* Toast */}
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
