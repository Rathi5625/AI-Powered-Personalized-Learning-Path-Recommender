import React from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Check, ArrowRight } from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { VerifyHeader } from '../components/layout/VerifyHeader';

export const PasswordResetSuccessPage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="relative min-h-screen flex flex-col justify-between overflow-x-hidden selection:bg-[#FFB091]/30 selection:text-[#9C5B33]">
      {/* Soft Ambient Background Glows */}
      <AmbientBackground />

      {/* Top Centered Brand Pill */}
      <VerifyHeader />

      {/* Main Success Card */}
      <main className="flex-1 flex flex-col items-center justify-center px-4 sm:px-6 py-8 sm:py-12 z-10">
        <motion.div
          initial={{ opacity: 0, scale: 0.95, y: 18 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
          className="w-full max-w-[470px] bg-white/85 backdrop-blur-2xl rounded-[32px] sm:rounded-[36px] p-7 sm:p-10 border border-white/90 shadow-[0_24px_64px_rgba(26,31,54,0.06)] text-center"
        >
          {/* Circular Success Icon Container */}
          <motion.div
            initial={{ scale: 0.6, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ delay: 0.1, type: 'spring', stiffness: 320, damping: 22 }}
            className="w-16 h-16 rounded-full bg-[#EBF5EF] border border-[#D4E8DC] flex items-center justify-center mx-auto mb-6 shadow-xs"
          >
            <div className="w-9 h-9 rounded-full border-2 border-[#529E73] flex items-center justify-center">
              <Check className="w-5 h-5 text-[#529E73] stroke-[2.5]" />
            </div>
          </motion.div>

          {/* Heading */}
          <motion.h1
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.16, duration: 0.4 }}
            className="text-2xl sm:text-[26px] font-extrabold text-[#1A1F36] tracking-tight"
          >
            Password updated successfully
          </motion.h1>

          {/* Subtitle */}
          <motion.p
            initial={{ opacity: 0, y: 6 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.22, duration: 0.4 }}
            className="text-xs sm:text-sm text-gray-500 mt-2.5 mb-8 max-w-xs mx-auto leading-relaxed font-normal"
          >
            Your password has been changed. You&apos;re ready to continue learning.
          </motion.p>

          {/* Action Buttons */}
          <div className="space-y-3">
            {/* Primary Sign In Button */}
            <motion.button
              whileHover={{ scale: 1.01 }}
              whileTap={{ scale: 0.99 }}
              type="button"
              onClick={() => navigate('/login')}
              className="w-full h-12 sm:h-13 rounded-full bg-[#CC7D52] hover:bg-[#B86D44] active:bg-[#A05D36] text-[#1A1F36] font-semibold text-xs sm:text-sm flex items-center justify-center gap-2 shadow-sm shadow-[#CC7D52]/20 transition-all cursor-pointer"
            >
              <span>Sign In</span>
              <ArrowRight className="w-4 h-4 text-[#1A1F36]" />
            </motion.button>

            {/* Secondary Return to LearnAI Button */}
            <motion.button
              whileHover={{ scale: 1.01 }}
              whileTap={{ scale: 0.99 }}
              type="button"
              onClick={() => navigate('/')}
              className="w-full h-12 sm:h-13 rounded-full bg-white/90 hover:bg-white active:bg-gray-50 border border-gray-200/90 text-[#1A1F36] font-semibold text-xs sm:text-sm flex items-center justify-center shadow-xs transition-all cursor-pointer"
            >
              <span>Return to LearnAI</span>
            </motion.button>
          </div>
        </motion.div>
      </main>
    </div>
  );
};
