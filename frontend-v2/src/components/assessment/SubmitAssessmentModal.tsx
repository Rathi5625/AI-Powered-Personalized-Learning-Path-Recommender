import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { CheckCircle2 } from 'lucide-react';

interface SubmitAssessmentModalProps {
  isOpen: boolean;
  answeredCount: number;
  totalQuestions: number;
  onGoBack: () => void;
  onConfirmSubmit: () => void;
}

export const SubmitAssessmentModal: React.FC<SubmitAssessmentModalProps> = ({
  isOpen,
  answeredCount,
  totalQuestions,
  onGoBack,
  onConfirmSubmit,
}) => {
  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          {/* Dark Translucent Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onGoBack}
            className="fixed inset-0 bg-black/40 backdrop-blur-xs"
          />

          {/* Modal Card */}
          <motion.div
            initial={{ scale: 0.95, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            exit={{ scale: 0.95, opacity: 0 }}
            className="relative w-full max-w-sm bg-white/95 backdrop-blur-2xl rounded-3xl p-6 sm:p-7 shadow-2xl border border-white/90 z-10 text-left space-y-4"
          >
            {/* Header Icon & Title */}
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-2xl bg-emerald-50 text-emerald-600 flex items-center justify-center shrink-0">
                <CheckCircle2 className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-base sm:text-lg font-extrabold text-[#0f1b32] tracking-tight">
                  Ready to submit?
                </h3>
              </div>
            </div>

            {/* Description */}
            <p className="text-xs sm:text-sm text-[#53433c] leading-relaxed font-normal">
              You have answered <strong className="font-bold text-[#0f1b32]">{answeredCount}</strong> of{' '}
              <strong className="font-bold text-[#0f1b32]">{totalQuestions}</strong> questions.
            </p>

            {/* Action Buttons */}
            <div className="flex flex-col gap-2 pt-2">
              <button
                type="button"
                onClick={onConfirmSubmit}
                className="w-full py-3 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold transition-colors cursor-pointer shadow-sm text-center"
              >
                Submit Assessment
              </button>
              <button
                type="button"
                onClick={onGoBack}
                className="w-full py-2.5 rounded-xl bg-gray-100 hover:bg-gray-200 text-gray-700 text-xs font-bold transition-colors cursor-pointer text-center"
              >
                Go back to review
              </button>
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};
