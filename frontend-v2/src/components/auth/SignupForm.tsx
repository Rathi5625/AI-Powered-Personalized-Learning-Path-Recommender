import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Eye,
  EyeOff,
  Sparkles,
  CheckCircle,
} from 'lucide-react';
import { api } from '../../api/client';

/* ─── Password Strength ─────────────────────────────────────────── */
type Strength = 'WEAK' | 'MEDIUM' | 'STRONG';

function getStrength(pw: string): Strength {
  let score = 0;
  if (pw.length >= 8) score++;
  if (/[A-Z]/.test(pw)) score++;
  if (/[0-9]/.test(pw)) score++;
  if (/[^A-Za-z0-9]/.test(pw)) score++;
  if (score <= 1) return 'WEAK';
  if (score <= 3) return 'MEDIUM';
  return 'STRONG';
}

const strengthConfig: Record<Strength, { bars: [string, string, string]; label: string; labelColor: string }> = {
  WEAK: {
    bars: ['bg-[#CC7D52]', 'bg-gray-200', 'bg-gray-200'],
    label: 'WEAK',
    labelColor: 'text-[#CC7D52]',
  },
  MEDIUM: {
    bars: ['bg-[#8E86FF]', 'bg-[#8E86FF]', 'bg-gray-200'],
    label: 'MEDIUM',
    labelColor: 'text-[#8E86FF]',
  },
  STRONG: {
    bars: ['bg-emerald-400', 'bg-emerald-400', 'bg-emerald-400'],
    label: 'STRONG',
    labelColor: 'text-emerald-500',
  },
};

/* ─── Input Component ───────────────────────────────────────────── */
interface AuthInputProps {
  id: string;
  label: string;
  type?: string;
  placeholder: string;
  value: string;
  onChange: (v: string) => void;
  rightSlot?: React.ReactNode;
  error?: string;
  success?: boolean;
}

const AuthInput: React.FC<AuthInputProps> = ({
  id, label, type = 'text', placeholder, value, onChange, rightSlot, error, success,
}) => (
  <div>
    <label htmlFor={id} className="block text-xs sm:text-sm font-semibold text-[#1A1F36] mb-1.5">
      {label}
    </label>
    <div className="relative flex items-center">
      <input
        id={id}
        type={type}
        placeholder={placeholder}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        autoComplete={id}
        className={`w-full h-12 sm:h-14 px-4 pr-11 rounded-xl sm:rounded-2xl bg-white border text-[#1A1F36] placeholder:text-gray-400 text-xs sm:text-sm focus:outline-none focus:ring-3 transition-all ${error
            ? 'border-red-300 focus:border-red-400 focus:ring-red-100'
            : success
              ? 'border-emerald-300 focus:border-emerald-400 focus:ring-emerald-100'
              : 'border-gray-200 focus:border-[#A06A42] focus:ring-[#A06A42]/10'
          }`}
      />
      {rightSlot && (
        <div className="absolute right-3 flex items-center">{rightSlot}</div>
      )}
      {success && !rightSlot && (
        <CheckCircle className="absolute right-3 w-4 h-4 text-emerald-400 pointer-events-none" />
      )}
    </div>
    <AnimatePresence>
      {error && (
        <motion.p
          initial={{ opacity: 0, height: 0 }}
          animate={{ opacity: 1, height: 'auto' }}
          exit={{ opacity: 0, height: 0 }}
          className="text-xs text-red-500 mt-1 overflow-hidden"
        >
          {error}
        </motion.p>
      )}
    </AnimatePresence>
  </div>
);

/* ─── Main SignupForm ────────────────────────────────────────────── */
export const SignupForm: React.FC = () => {
  const navigate = useNavigate();
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [termsAccepted, setTermsAccepted] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [toast, setToast] = useState<string | null>(null);
  const [submitted, setSubmitted] = useState(false);

  /* Derived */
  const strength = password ? getStrength(password) : null;
  const sc = strength ? strengthConfig[strength] : null;
  const passwordsMatch =
    confirmPassword.length > 0 && password === confirmPassword;
  const passwordsMismatch =
    confirmPassword.length > 0 && password !== confirmPassword;

  /* Validation errors on submit */
  const [errors, setErrors] = useState<Record<string, string>>({});

  const showToast = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3200);
  };

  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitted(true);
    const errs: Record<string, string> = {};

    if (!fullName.trim()) errs.fullName = 'Full name is required.';
    if (!email.trim() || !/\S+@\S+\.\S+/.test(email)) errs.email = 'Enter a valid email address.';
    if (!password) errs.password = 'Password is required.';
    else if (password.length < 6) errs.password = 'Password must be at least 6 characters.';
    if (!confirmPassword) errs.confirmPassword = 'Please confirm your password.';
    else if (password !== confirmPassword) errs.confirmPassword = 'Passwords do not match.';
    if (!termsAccepted) errs.terms = 'You must agree to the Terms of Service.';

    setErrors(errs);

    if (Object.keys(errs).length === 0) {
      setIsLoading(true);
      try {
        await api.signup({
          name: fullName.trim(),
          email: email.trim().toLowerCase(),
          password: password,
        });

        showToast('Verification code sent to your email.');
        setTimeout(() => {
          navigate('/verify-email', {
            state: {
              email: email.trim().toLowerCase(),
              purpose: 'EMAIL_VERIFICATION',
            },
          });
        }, 1000);
      } catch (err: unknown) {
        if (err instanceof Error) {
          setErrors({ form: err.message });
          showToast(err.message);
        } else {
          setErrors({ form: 'An unexpected error occurred. Please try again.' });
        }
      } finally {
        setIsLoading(false);
      }
    }
  };

  const EyeBtn = ({
    show, toggle, label,
  }: { show: boolean; toggle: () => void; label: string }) => (
    <button
      type="button"
      aria-label={label}
      onClick={toggle}
      className="w-8 h-8 flex items-center justify-center text-gray-400 hover:text-gray-600 transition-colors cursor-pointer"
    >
      {show ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
    </button>
  );

  return (
    <div>
      {/* Icon + Heading */}
      <div className="text-center mb-6">
        <div className="w-11 h-11 rounded-2xl bg-[#ECEAFF] border border-[#D8D5FF] flex items-center justify-center mx-auto mb-3.5">
          <Sparkles className="w-5 h-5 text-[#8E86FF]" />
        </div>
        <h1 className="text-2xl sm:text-[26px] font-extrabold text-[#1A1F36] tracking-tight">
          Create your account
        </h1>
        <p className="text-xs sm:text-sm text-gray-500 mt-1">
          Start your personalized learning journey with LearnAI.
        </p>
      </div>

      <form onSubmit={handleSubmit} noValidate className="space-y-3.5">
        {/* Full Name */}
        <AuthInput
          id="signup-fullname"
          label="Full Name"
          placeholder="Jane Doe"
          value={fullName}
          onChange={setFullName}
          error={submitted ? errors.fullName : undefined}
          success={submitted && !errors.fullName && fullName.length > 0}
        />

        {/* Email */}
        <AuthInput
          id="signup-email"
          label="Email Address"
          type="email"
          placeholder="jane@example.com"
          value={email}
          onChange={setEmail}
          error={submitted ? errors.email : undefined}
          success={submitted && !errors.email && email.length > 0}
        />

        {/* Password */}
        <div>
          <label htmlFor="signup-password" className="block text-xs sm:text-sm font-semibold text-[#1A1F36] mb-1.5">
            Password
          </label>
          <div className="relative flex items-center">
            <input
              id="signup-password"
              type={showPassword ? 'text' : 'password'}
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="new-password"
              className={`w-full h-12 sm:h-14 px-4 pr-11 rounded-xl sm:rounded-2xl bg-white border text-[#1A1F36] placeholder:text-gray-400 text-xs sm:text-sm focus:outline-none focus:ring-3 transition-all ${submitted && errors.password
                  ? 'border-red-300 focus:border-red-400 focus:ring-red-100'
                  : 'border-gray-200 focus:border-[#A06A42] focus:ring-[#A06A42]/10'
                }`}
            />
            <EyeBtn
              show={showPassword}
              toggle={() => setShowPassword(!showPassword)}
              label={showPassword ? 'Hide password' : 'Show password'}
            />
          </div>

          {/* Password Strength Bar */}
          {password && sc && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className="mt-2 flex items-center gap-1.5"
            >
              {sc.bars.map((barColor, i) => (
                <motion.div
                  key={i}
                  initial={{ scaleX: 0 }}
                  animate={{ scaleX: 1 }}
                  transition={{ delay: i * 0.05 }}
                  className={`h-1 flex-1 rounded-full origin-left ${barColor} transition-colors duration-300`}
                />
              ))}
              <span className={`text-[10px] font-bold ml-1 ${sc.labelColor}`}>{sc.label}</span>
            </motion.div>
          )}

          <AnimatePresence>
            {submitted && errors.password && (
              <motion.p
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: 'auto' }}
                exit={{ opacity: 0, height: 0 }}
                className="text-xs text-red-500 mt-1"
              >
                {errors.password}
              </motion.p>
            )}
          </AnimatePresence>
        </div>

        {/* Confirm Password */}
        <AuthInput
          id="signup-confirm"
          label="Confirm Password"
          type={showConfirm ? 'text' : 'password'}
          placeholder="••••••••"
          value={confirmPassword}
          onChange={setConfirmPassword}
          error={submitted ? errors.confirmPassword : passwordsMismatch ? 'Passwords do not match.' : undefined}
          success={passwordsMatch}
          rightSlot={
            <EyeBtn
              show={showConfirm}
              toggle={() => setShowConfirm(!showConfirm)}
              label={showConfirm ? 'Hide password' : 'Show password'}
            />
          }
        />

        {/* Terms */}
        <div>
          <label className="flex items-start gap-2.5 cursor-pointer select-none">
            <input
              type="checkbox"
              checked={termsAccepted}
              onChange={(e) => setTermsAccepted(e.target.checked)}
              className="mt-0.5 w-4 h-4 rounded border-gray-300 accent-[#A06A42] cursor-pointer"
            />
            <span className="text-xs sm:text-sm text-gray-600">
              I agree to the{' '}
              <button
                type="button"
                onClick={() => showToast('Terms of Service will be available soon.')}
                className="text-[#A06A42] font-semibold hover:underline cursor-pointer"
              >
                Terms of Service
              </button>{' '}
              and{' '}
              <button
                type="button"
                onClick={() => showToast('Privacy Policy will be available soon.')}
                className="text-[#A06A42] font-semibold hover:underline cursor-pointer"
              >
                Privacy Policy
              </button>
              .
            </span>
          </label>
          <AnimatePresence>
            {submitted && errors.terms && (
              <motion.p
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: 'auto' }}
                exit={{ opacity: 0, height: 0 }}
                className="text-xs text-red-500 mt-1 ml-6"
              >
                {errors.terms}
              </motion.p>
            )}
          </AnimatePresence>
        </div>

        {/* Submit */}
        <motion.button
          type="submit"
          whileHover={!isLoading && termsAccepted ? { scale: 1.01 } : {}}
          whileTap={!isLoading && termsAccepted ? { scale: 0.99 } : {}}
          disabled={!termsAccepted || isLoading}
          className="w-full h-12 sm:h-14 rounded-xl sm:rounded-2xl bg-[#CC7D52] hover:bg-[#B86D44] active:bg-[#A05D36] text-white font-semibold text-sm flex items-center justify-center shadow-sm shadow-[#A06A42]/20 transition-all cursor-pointer mt-1 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {isLoading ? 'Creating Account...' : 'Create Account'}
        </motion.button>
      </form>

      {/* Divider */}
      <div className="relative flex py-4 sm:py-5 items-center">
        <div className="flex-grow border-t border-gray-200/80" />
        <span className="flex-shrink mx-3 text-[11px] sm:text-xs text-gray-400 uppercase tracking-wider font-medium">
          or continue with
        </span>
        <div className="flex-grow border-t border-gray-200/80" />
      </div>

      {/* Google */}
      <button
        type="button"
        onClick={() => showToast('Google sign-in will be available soon.')}
        className="w-full h-11 sm:h-12 rounded-xl sm:rounded-2xl bg-white hover:bg-gray-50 border border-gray-200/90 shadow-xs flex items-center justify-center gap-2.5 text-xs sm:text-sm font-semibold text-[#1A1F36] transition-all cursor-pointer"
      >
        <svg className="w-4 h-4" viewBox="0 0 24 24">
          <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
          <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
          <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" />
          <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z" />
        </svg>
        <span>Continue with Google</span>
      </button>

      {/* Sign In */}
      <p className="text-xs sm:text-sm text-gray-500 text-center mt-4">
        Already have an account?{' '}
        <Link to="/login" className="text-[#A06A42] font-semibold hover:underline">
          Sign in
        </Link>
      </p>

      {/* Toast */}
      <AnimatePresence>
        {toast && (
          <motion.div
            initial={{ opacity: 0, y: 18 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 18 }}
            className="fixed bottom-6 left-1/2 -translate-x-1/2 bg-[#1A1F36] text-white text-xs font-medium px-4 py-2.5 rounded-full shadow-lg z-50 flex items-center gap-2"
          >
            <Sparkles className="w-3.5 h-3.5 text-[#FFB091]" />
            <span>{toast}</span>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};
