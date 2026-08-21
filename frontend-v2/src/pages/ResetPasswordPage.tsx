import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Lock, RotateCcw, Eye, EyeOff, ArrowRight, Sparkles, AlertCircle } from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { ResetHeader } from '../components/layout/ResetHeader';
import { PasswordRequirements, allRequirementsMet } from '../components/auth/PasswordRequirements';
import { api } from '../api/client';

/* ─── Password Strength ─────────────────────────────────────────── */
type Strength = 'weak' | 'medium' | 'strong';

function calcStrength(pw: string): Strength {
  let score = 0;
  if (pw.length >= 8) score++;
  if (/[A-Z]/.test(pw)) score++;
  if (/[0-9]/.test(pw)) score++;
  if (/[^A-Za-z0-9]/.test(pw)) score++;
  if (score <= 1) return 'weak';
  if (score <= 3) return 'medium';
  return 'strong';
}

const strengthCfg: Record<Strength, { bars: string[]; label: string; color: string }> = {
  weak:   { bars: ['bg-[#BDBBBB]', 'bg-gray-200', 'bg-gray-200'], label: 'Weak',   color: 'text-gray-500' },
  medium: { bars: ['bg-[#8E86FF]', 'bg-[#8E86FF]', 'bg-gray-200'], label: 'Medium', color: 'text-[#8E86FF]' },
  strong: { bars: ['bg-emerald-400', 'bg-emerald-400', 'bg-emerald-400'], label: 'Strong', color: 'text-emerald-500' },
};

/* ─── Page ──────────────────────────────────────────────────────── */
export const ResetPasswordPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const state = location.state as { resetToken?: string; email?: string } | null;
  const searchParams = new URLSearchParams(location.search);
  const resetToken = state?.resetToken || searchParams.get('token') || '';

  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showNew, setShowNew] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [toast, setToast] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  /* Derived */
  const strength = newPassword ? calcStrength(newPassword) : null;
  const sc = strength ? strengthCfg[strength] : null;
  const passwordsMatch = confirmPassword.length > 0 && newPassword === confirmPassword;
  const passwordsMismatch = confirmPassword.length > 0 && newPassword !== confirmPassword;
  const requirementsMet = allRequirementsMet(newPassword);
  const formValid = requirementsMet && passwordsMatch && newPassword.length > 0 && confirmPassword.length > 0 && !!resetToken;

  const showNotice = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3500);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formValid || isLoading) return;

    if (!resetToken) {
      setErrorMsg('Invalid or missing reset token. Please request a new password reset.');
      showNotice('Missing reset token.');
      return;
    }

    setIsLoading(true);
    setErrorMsg(null);

    try {
      await api.resetPassword({
        resetToken,
        newPassword,
        confirmPassword,
      });

      showNotice('Password reset successfully! You can now sign in.');
      setTimeout(() => {
        navigate('/password-reset-success');
      }, 1000);
    } catch (err: unknown) {
      if (err instanceof Error) {
        setErrorMsg(err.message);
        showNotice(err.message);
      } else {
        setErrorMsg('Failed to reset password. Please try again.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="relative min-h-screen flex flex-col overflow-x-hidden selection:bg-[#FFB091]/30 selection:text-[#9C5B33]">
      <AmbientBackground />
      <ResetHeader />

      <main className="flex-1 flex flex-col items-center justify-center px-4 sm:px-6 py-8 sm:py-14 z-10">
        <motion.div
          initial={{ opacity: 0, scale: 0.97, y: 16 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
          className="w-full max-w-[560px] bg-white/85 backdrop-blur-2xl rounded-[32px] sm:rounded-[36px] border border-white/90 shadow-[0_20px_60px_rgba(26,31,54,0.06)] px-7 sm:px-10 py-9 sm:py-11"
        >
          {/* Heading */}
          <div className="text-center mb-7 sm:mb-8">
            <motion.h1
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.08, duration: 0.4 }}
              className="text-2xl sm:text-[28px] font-extrabold text-[#1A1F36] tracking-tight"
            >
              Create a new password
            </motion.h1>
            <motion.p
              initial={{ opacity: 0, y: 6 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.14, duration: 0.4 }}
              className="text-xs sm:text-sm text-gray-500 mt-1.5"
            >
              Choose a strong password you haven&apos;t used before.
            </motion.p>
          </div>

          {/* Error Banner */}
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

          <form onSubmit={handleSubmit} noValidate className="space-y-4 sm:space-y-5">
            {/* New Password */}
            <div>
              <label
                htmlFor="reset-new-pw"
                className="block text-xs sm:text-sm font-semibold text-[#1A1F36] mb-1.5"
              >
                New Password
              </label>
              <div className="relative flex items-center">
                <Lock className="absolute left-4 w-4 h-4 text-gray-400 pointer-events-none" />
                <input
                  id="reset-new-pw"
                  type={showNew ? 'text' : 'password'}
                  autoComplete="new-password"
                  placeholder="Enter new password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  className="w-full h-12 sm:h-14 pl-11 pr-11 rounded-xl sm:rounded-2xl bg-white border border-gray-200 text-[#1A1F36] placeholder:text-gray-400 text-xs sm:text-sm focus:outline-none focus:border-[#A06A42] focus:ring-3 focus:ring-[#A06A42]/10 transition-all"
                />
                <button
                  type="button"
                  aria-label={showNew ? 'Hide password' : 'Show password'}
                  onClick={() => setShowNew(!showNew)}
                  className="absolute right-3 w-8 h-8 flex items-center justify-center text-gray-400 hover:text-gray-600 transition-colors cursor-pointer"
                >
                  {showNew ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>

              {/* Strength Bar */}
              <div className="mt-2 flex items-center gap-1.5">
                {sc ? (
                  <>
                    {sc.bars.map((barColor, i) => (
                      <motion.div
                        key={i}
                        initial={{ scaleX: 0 }}
                        animate={{ scaleX: 1 }}
                        transition={{ delay: i * 0.06, duration: 0.25 }}
                        className={`h-1 flex-1 rounded-full origin-left transition-colors duration-300 ${barColor}`}
                      />
                    ))}
                    <span className={`text-[10px] font-bold ml-1 ${sc.color} min-w-[36px]`}>
                      {sc.label}
                    </span>
                  </>
                ) : (
                  <>
                    <div className="h-1 flex-1 rounded-full bg-gray-200" />
                    <div className="h-1 flex-1 rounded-full bg-gray-200" />
                    <div className="h-1 flex-1 rounded-full bg-gray-200" />
                    <span className="text-[10px] font-bold ml-1 text-gray-400 min-w-[36px]">Weak</span>
                  </>
                )}
              </div>
            </div>

            {/* Confirm Password */}
            <div>
              <label
                htmlFor="reset-confirm-pw"
                className="block text-xs sm:text-sm font-semibold text-[#1A1F36] mb-1.5"
              >
                Confirm Password
              </label>
              <div className="relative flex items-center">
                <RotateCcw className="absolute left-4 w-4 h-4 text-gray-400 pointer-events-none" />
                <input
                  id="reset-confirm-pw"
                  type={showConfirm ? 'text' : 'password'}
                  autoComplete="new-password"
                  placeholder="Re-enter new password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className={`w-full h-12 sm:h-14 pl-11 pr-11 rounded-xl sm:rounded-2xl bg-white border text-[#1A1F36] placeholder:text-gray-400 text-xs sm:text-sm focus:outline-none focus:ring-3 transition-all ${
                    passwordsMismatch
                      ? 'border-red-300 focus:border-red-400 focus:ring-red-100'
                      : passwordsMatch
                      ? 'border-emerald-300 focus:border-emerald-400 focus:ring-emerald-100'
                      : 'border-gray-200 focus:border-[#A06A42] focus:ring-[#A06A42]/10'
                  }`}
                />
                <button
                  type="button"
                  aria-label={showConfirm ? 'Hide password' : 'Show password'}
                  onClick={() => setShowConfirm(!showConfirm)}
                  className="absolute right-3 w-8 h-8 flex items-center justify-center text-gray-400 hover:text-gray-600 transition-colors cursor-pointer"
                >
                  {showConfirm ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>

              <AnimatePresence>
                {passwordsMismatch && (
                  <motion.p
                    initial={{ opacity: 0, height: 0 }}
                    animate={{ opacity: 1, height: 'auto' }}
                    exit={{ opacity: 0, height: 0 }}
                    className="text-xs text-red-500 mt-1.5 overflow-hidden"
                  >
                    Passwords do not match.
                  </motion.p>
                )}
                {passwordsMatch && (
                  <motion.p
                    initial={{ opacity: 0, height: 0 }}
                    animate={{ opacity: 1, height: 'auto' }}
                    exit={{ opacity: 0, height: 0 }}
                    className="text-xs text-emerald-500 font-medium mt-1.5 overflow-hidden"
                  >
                    Passwords match.
                  </motion.p>
                )}
              </AnimatePresence>
            </div>

            {/* Password Requirements Panel */}
            <PasswordRequirements password={newPassword} />

            {/* Submit Button */}
            <motion.button
              type="submit"
              whileHover={formValid ? { scale: 1.01 } : {}}
              whileTap={formValid ? { scale: 0.99 } : {}}
              disabled={!formValid}
              className="w-full h-12 sm:h-14 rounded-xl sm:rounded-2xl bg-[#CC7D52] hover:bg-[#B86D44] active:bg-[#A05D36] text-white font-semibold text-xs sm:text-sm flex items-center justify-center gap-2 shadow-sm shadow-[#A06A42]/20 transition-all cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed mt-1"
            >
              <span>Reset Password</span>
              <ArrowRight className="w-4 h-4" />
            </motion.button>
          </form>
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
