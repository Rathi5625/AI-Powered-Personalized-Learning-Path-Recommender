import React from 'react';

export interface BadgeProps {
  children: React.ReactNode;
  variant?: 'peach' | 'lavender' | 'brown' | 'neutral' | 'pro';
  className?: string;
  size?: 'sm' | 'md';
}

export const Badge: React.FC<BadgeProps> = ({
  children,
  variant = 'neutral',
  size = 'md',
  className = '',
}) => {
  const sizeStyles = {
    sm: 'text-[11px] px-2.5 py-0.5',
    md: 'text-xs px-3 py-1',
  };

  const variantStyles = {
    peach: 'bg-[#FCEFEA] text-[#A06A42] border border-[#FADCD1]',
    lavender: 'bg-[#F1EFFF] text-[#6E64E8] border border-[#DDD8FE]',
    brown: 'bg-[#F7EDE6] text-[#A06A42] border border-[#F0DCD0]',
    pro: 'bg-[#F9EFE8] text-[#A06A42] font-semibold border border-[#F2DACB]',
    neutral: 'bg-black/5 text-gray-700 border border-black/5',
  };

  return (
    <span
      className={`inline-flex items-center justify-center font-medium rounded-full ${sizeStyles[size]} ${variantStyles[variant]} ${className}`}
    >
      {children}
    </span>
  );
};
