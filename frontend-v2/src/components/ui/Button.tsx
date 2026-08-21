import React from 'react';
import { motion, type HTMLMotionProps } from 'framer-motion';

export interface ButtonProps extends HTMLMotionProps<'button'> {
  variant?: 'primary' | 'secondary' | 'ghost' | 'outline';
  size?: 'sm' | 'md' | 'lg';
  children: React.ReactNode;
  className?: string;
  icon?: React.ReactNode;
}

export const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  size = 'md',
  children,
  className = '',
  icon,
  ...props
}) => {
  const baseStyles =
    'inline-flex items-center justify-center font-medium rounded-full transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 cursor-pointer select-none';

  const sizeStyles = {
    sm: 'text-xs px-3.5 py-1.5 gap-1.5',
    md: 'text-sm px-5 py-2.5 gap-2',
    lg: 'text-sm sm:text-base px-7 py-3.5 gap-2.5',
  };

  const variantStyles = {
    primary:
      'bg-[#A06A42] hover:bg-[#8D5832] text-white shadow-sm shadow-[#A06A42]/20 focus-visible:ring-[#A06A42]',
    secondary:
      'bg-white/80 hover:bg-white text-[#1A1F36] border border-black/5 shadow-sm focus-visible:ring-black/20',
    ghost:
      'bg-transparent hover:bg-black/5 text-[#1A1F36] focus-visible:ring-black/20',
    outline:
      'bg-transparent hover:bg-[#A06A42]/5 text-[#A06A42] border border-[#A06A42]/30 focus-visible:ring-[#A06A42]',
  };

  return (
    <motion.button
      whileHover={{ scale: 1.02 }}
      whileTap={{ scale: 0.98 }}
      className={`${baseStyles} ${sizeStyles[size]} ${variantStyles[variant]} ${className}`}
      {...props}
    >
      {icon && <span className="shrink-0">{icon}</span>}
      <span>{children}</span>
    </motion.button>
  );
};
