import React from 'react';
import { motion } from 'framer-motion';
import { X, Laptop, Smartphone, ShieldAlert } from 'lucide-react';

interface ActiveSessionsModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSignOutOthers: () => void;
}

export const ActiveSessionsModal: React.FC<ActiveSessionsModalProps> = ({
  isOpen,
  onClose,
  onSignOutOthers,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        onClick={onClose}
        className="fixed inset-0 bg-black/40 backdrop-blur-xs"
      />
      <motion.div
        initial={{ scale: 0.95, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        exit={{ scale: 0.95, opacity: 0 }}
        className="relative w-full max-w-md bg-white rounded-3xl p-6 sm:p-7 shadow-2xl border border-gray-100 z-10 text-left space-y-5"
      >
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-gray-100">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-xl bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b]">
              <ShieldAlert className="w-4 h-4 text-[#8e4d2b]" />
            </div>
            <h3 className="text-base font-bold text-[#0f1b32]">Active Sessions</h3>
          </div>
          <button
            type="button"
            aria-label="Close modal"
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 p-1"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Sessions List */}
        <div className="space-y-3">
          {/* Current Device */}
          <div className="flex items-center justify-between p-3.5 rounded-2xl bg-[#FAF4F0]/60 border border-[#F2DACB]/60 text-xs">
            <div className="flex items-center gap-3">
              <Laptop className="w-4 h-4 text-[#8e4d2b]" />
              <div>
                <span className="font-bold text-[#0f1b32] block">Current Device</span>
                <span className="text-[11px] text-gray-500 font-medium block">
                  Windows 11 • Chrome 128 • San Francisco, US
                </span>
              </div>
            </div>
            <span className="px-2 py-0.5 rounded-full bg-emerald-50 border border-emerald-200 text-emerald-700 text-[10px] font-bold">
              Active Now
            </span>
          </div>

          {/* Mobile Device */}
          <div className="flex items-center justify-between p-3.5 rounded-2xl bg-white border border-gray-100 text-xs">
            <div className="flex items-center gap-3">
              <Smartphone className="w-4 h-4 text-gray-500" />
              <div>
                <span className="font-bold text-[#0f1b32] block">Mobile Device</span>
                <span className="text-[11px] text-gray-500 font-medium block">
                  Android 14 • Chrome Mobile • 2 hours ago
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Action Button */}
        <div className="pt-2">
          <button
            type="button"
            onClick={() => {
              onSignOutOthers();
              onClose();
            }}
            className="w-full py-2.5 px-4 rounded-xl bg-white hover:bg-gray-50 border border-gray-200 text-xs font-bold text-[#ba1a1a] transition-colors"
          >
            Sign out of all other sessions
          </button>
        </div>
      </motion.div>
    </div>
  );
};
