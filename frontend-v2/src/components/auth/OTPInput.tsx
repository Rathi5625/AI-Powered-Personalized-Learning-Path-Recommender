import React, { useRef, useEffect, ClipboardEvent, KeyboardEvent } from 'react';
import { motion } from 'framer-motion';

interface OTPInputProps {
  value: string[];           // array of 6 single-char strings ('0'–'9' or '')
  onChange: (next: string[]) => void;
  disabled?: boolean;
}

export const OTPInput: React.FC<OTPInputProps> = ({ value, onChange, disabled = false }) => {
  const refs = useRef<Array<HTMLInputElement | null>>([]);

  /* Focus first empty or last box on mount */
  useEffect(() => {
    const firstEmpty = value.findIndex((v) => v === '');
    const idx = firstEmpty === -1 ? 5 : firstEmpty;
    refs.current[idx]?.focus();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const focusBox = (idx: number) => {
    if (idx >= 0 && idx < 6) refs.current[idx]?.focus();
  };

  const handleChange = (idx: number, raw: string) => {
    const digit = raw.replace(/\D/g, '').slice(-1);
    if (!digit) return;
    const next = [...value];
    next[idx] = digit;
    onChange(next);
    focusBox(idx + 1);
  };

  const handleKeyDown = (idx: number, e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Backspace') {
      e.preventDefault();
      const next = [...value];
      if (next[idx]) {
        next[idx] = '';
        onChange(next);
      } else {
        focusBox(idx - 1);
        if (idx > 0) {
          next[idx - 1] = '';
          onChange(next);
        }
      }
    } else if (e.key === 'ArrowLeft') {
      e.preventDefault();
      focusBox(idx - 1);
    } else if (e.key === 'ArrowRight') {
      e.preventDefault();
      focusBox(idx + 1);
    }
  };

  const handlePaste = (e: ClipboardEvent<HTMLInputElement>) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6);
    if (!pasted) return;
    const next = Array(6).fill('');
    pasted.split('').forEach((ch, i) => { next[i] = ch; });
    onChange(next);
    const nextFocus = Math.min(pasted.length, 5);
    setTimeout(() => focusBox(nextFocus), 0);
  };

  return (
    <div className="flex items-center gap-2 sm:gap-3 justify-center" role="group" aria-label="6-digit verification code">
      {value.map((digit, idx) => {
      const filled = digit !== '';
        return (
          <motion.div
            key={idx}
            initial={{ opacity: 0, scale: 0.85 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ delay: 0.06 + idx * 0.05, type: 'spring', stiffness: 300, damping: 24 }}
          >
            <input
              ref={(el) => { refs.current[idx] = el; }}
              type="text"
              inputMode="numeric"
              maxLength={1}
              value={digit}
              disabled={disabled}
              aria-label={`Digit ${idx + 1}`}
              onChange={(e) => handleChange(idx, e.target.value)}
              onKeyDown={(e) => handleKeyDown(idx, e)}
              onPaste={handlePaste}
              onFocus={(e) => e.target.select()}
              className={`
                w-11 h-12 sm:w-13 sm:h-14 text-center text-base sm:text-lg font-bold rounded-xl sm:rounded-2xl border-2 transition-all duration-200 outline-none bg-white/70
                ${disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-text'}
                ${filled
                  ? 'border-[#CC7D52] text-[#1A1F36] bg-white shadow-sm shadow-[#CC7D52]/10'
                  : 'border-[#E8E4F0] text-[#1A1F36] bg-[#F5F3FA]/80'}
                focus:border-[#CC7D52] focus:bg-white focus:shadow-md focus:shadow-[#CC7D52]/15
              `}
              style={{ width: 46, height: 52 }}
            />
          </motion.div>
        );
      })}
    </div>
  );
};
