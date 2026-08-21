import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { LogOut } from 'lucide-react';

interface ExitAssessmentModalProps {
  isOpen: boolean;
  onContinue: () => void;
  onConfirmExit: () => void;
}

export const ExitAssessmentModal: React.FC<ExitAssessmentModalProps> = ({
  isOpen,
  onContinue,
  onConfirmExit,
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
            onClick={onContinue}
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
              <div className="w-10 h-10 rounded-2xl bg-red-50 text-[#ba1a1a] flex items-center justify-center shrink-0">
                <LogOut className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-base sm:text-lg font-extrabold text-[#0f1b32] tracking-tight">
                  Leave assessment?
                </h3>
              </div>
            </div>

            {/* Description */}
            <p className="text-xs sm:text-sm text-[#53433c] leading-relaxed font-normal">
              Your progress will be saved, but you will leave the current assessment.
            </p>

            {/* Action Buttons */}
            <div className="flex items-center gap-3 pt-2">
              <button
                type="button"
                onClick={onContinue}
                className="flex-1 py-2.5 sm:py-3 rounded-xl bg-gray-100 hover:bg-gray-200 text-gray-700 text-xs font-bold transition-colors cursor-pointer text-center"
              >
                Continue
              </button>
              <button
                type="button"
                onClick={onConfirmExit}
                className="flex-1 py-2.5 sm:py-3 rounded-xl bg-[#ba1a1a] hover:bg-[#9a1414] text-white text-xs font-bold transition-colors cursor-pointer shadow-sm text-center"
              >
                Exit
              </button>
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};
