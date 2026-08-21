import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { X, User } from 'lucide-react';

interface EditProfileModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (data: { fullName: string; email: string; location: string }) => void;
}

export const EditProfileModal: React.FC<EditProfileModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
}) => {
  const [fullName, setFullName] = useState('Parth Rathi');
  const [email, setEmail] = useState('parth@example.com');
  const [location, setLocation] = useState('San Francisco, CA');

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSuccess({ fullName, email, location });
    onClose();
  };

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
              <User className="w-4 h-4 text-[#8e4d2b]" />
            </div>
            <h3 className="text-base font-bold text-[#0f1b32]">Edit Profile</h3>
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

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4 text-xs font-semibold">
          <div className="space-y-1">
            <label className="text-gray-500 uppercase tracking-wider text-[10px] block">
              Full Name
            </label>
            <input
              type="text"
              required
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-2xl bg-white border border-gray-200 text-xs font-medium text-[#0f1b32] focus:outline-none focus:ring-2 focus:ring-[#8e4d2b]/20 focus:border-[#8e4d2b]"
            />
          </div>

          <div className="space-y-1">
            <label className="text-gray-500 uppercase tracking-wider text-[10px] block">
              Email Address
            </label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-2xl bg-white border border-gray-200 text-xs font-medium text-[#0f1b32] focus:outline-none focus:ring-2 focus:ring-[#8e4d2b]/20 focus:border-[#8e4d2b]"
            />
          </div>

          <div className="space-y-1">
            <label className="text-gray-500 uppercase tracking-wider text-[10px] block">
              Location
            </label>
            <input
              type="text"
              required
              value={location}
              onChange={(e) => setLocation(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-2xl bg-white border border-gray-200 text-xs font-medium text-[#0f1b32] focus:outline-none focus:ring-2 focus:ring-[#8e4d2b]/20 focus:border-[#8e4d2b]"
            />
          </div>

          <div className="flex items-center gap-2 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 py-2.5 rounded-xl border border-gray-200 text-xs font-bold text-gray-600 hover:bg-gray-50 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="flex-1 py-2.5 rounded-xl bg-[#8e4d2b] hover:bg-[#783e20] text-white text-xs font-bold transition-colors shadow-2xs text-center"
            >
              Save Changes
            </button>
          </div>
        </form>
      </motion.div>
    </div>
  );
};
