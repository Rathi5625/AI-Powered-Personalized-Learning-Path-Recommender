import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { CheckCircle } from 'lucide-react';

const requirements = [
  { key: 'length',    label: 'At least 8 characters',   test: (p: string) => p.length >= 8 },
  { key: 'upper',     label: 'One uppercase letter',     test: (p: string) => /[A-Z]/.test(p) },
  { key: 'number',    label: 'One number',               test: (p: string) => /[0-9]/.test(p) },
  { key: 'special',   label: 'One special character',    test: (p: string) => /[^A-Za-z0-9]/.test(p) },
];

interface PasswordRequirementsProps {
  password: string;
}

export const PasswordRequirements: React.FC<PasswordRequirementsProps> = ({ password }) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: 6 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: 0.15, duration: 0.35 }}
      className="rounded-2xl bg-[#F0EEFF]/80 border border-[#E2DDFF]/80 px-4 py-3.5"
    >
      <p className="text-xs font-bold text-[#1A1F36] mb-2.5 tracking-wide">
        Password Requirements:
      </p>
      <ul className="space-y-1.5">
        {requirements.map((req) => {
          const met = password.length > 0 && req.test(password);
          return (
            <li key={req.key} className="flex items-center gap-2">
              <AnimatePresence mode="wait">
                {met ? (
                  <motion.span
                    key="check"
                    initial={{ scale: 0.5, opacity: 0 }}
                    animate={{ scale: 1, opacity: 1 }}
                    exit={{ scale: 0.5, opacity: 0 }}
                    transition={{ type: 'spring', stiffness: 400, damping: 20 }}
                  >
                    <CheckCircle className="w-3.5 h-3.5 text-emerald-500 flex-shrink-0" />
                  </motion.span>
                ) : (
                  <motion.span
                    key="circle"
                    initial={{ scale: 0.5, opacity: 0 }}
                    animate={{ scale: 1, opacity: 1 }}
                    exit={{ scale: 0.5, opacity: 0 }}
                  >
                    <span className="w-3.5 h-3.5 rounded-full border border-gray-400 flex-shrink-0 inline-block" />
                  </motion.span>
                )}
              </AnimatePresence>
              <span
                className={`text-xs transition-colors duration-200 ${
                  met ? 'text-emerald-600 font-medium' : 'text-gray-500'
                }`}
              >
                {req.label}
              </span>
            </li>
          );
        })}
      </ul>
    </motion.div>
  );
};

/* Re-export so consumers can call allRequirementsMet() */
export function allRequirementsMet(password: string): boolean {
  return requirements.every((r) => r.test(password));
}
