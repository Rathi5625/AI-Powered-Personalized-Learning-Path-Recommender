import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Sparkles, SlidersHorizontal, Menu, X } from 'lucide-react';
import { Button } from '../ui/Button';
import { useAuth } from '../../context/AuthContext';

export const Header: React.FC = () => {
  const navigate = useNavigate();
  const { user, isAuthenticated, logout } = useAuth();
  const [isScrolled, setIsScrolled] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      if (window.scrollY > 24) {
        setIsScrolled(true);
      } else {
        setIsScrolled(false);
      }
    };

    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const navItems = [
    { label: 'Home', href: '#', active: true },
    { label: 'How It Works', href: '#how-it-works', active: false },
    { label: 'Features', href: '#features', active: false },
    { label: 'Learning Paths', href: '#learning-paths', active: false },
    { label: 'About', href: '#about', active: false },
  ];

  return (
    <div className="fixed top-0 left-0 right-0 z-50 flex justify-center px-4 sm:px-6 pt-4 sm:pt-6 pointer-events-none">
      <motion.header
        initial={{ y: -20, opacity: 0 }}
        animate={{
          y: 0,
          opacity: 1,
          paddingTop: isScrolled ? '8px' : '12px',
          paddingBottom: isScrolled ? '8px' : '12px',
          paddingLeft: isScrolled ? '18px' : '28px',
          paddingRight: isScrolled ? '18px' : '28px',
          backgroundColor: isScrolled
            ? 'rgba(255, 255, 255, 0.92)'
            : 'rgba(255, 255, 255, 0.78)',
          boxShadow: isScrolled
            ? '0 12px 36px -4px rgba(26, 31, 54, 0.08), 0 4px 12px -2px rgba(0, 0, 0, 0.04)'
            : '0 8px 30px rgba(0, 0, 0, 0.04)',
        }}
        transition={{ duration: 0.35, ease: [0.16, 1, 0.3, 1] }}
        className="w-full max-w-5xl rounded-full backdrop-blur-xl border border-white/80 flex items-center justify-between pointer-events-auto transition-colors"
      >
        {/* Left: Brand Logo */}
        <a
          href="#"
          className="flex items-center gap-2.5 group select-none cursor-pointer"
        >
          <div className="w-8 h-8 rounded-xl bg-gradient-to-tr from-[#1A1F36] via-[#2A3152] to-[#454E78] text-white flex items-center justify-center shadow-sm group-hover:scale-105 transition-transform">
            <Sparkles className="w-4 h-4 text-[#FFB091]" />
          </div>
          <span className="font-extrabold text-lg tracking-tight text-[#1A1F36]">
            LearnAI
          </span>
        </a>

        {/* Center: Desktop Navigation Links */}
        <nav className="hidden md:flex items-center gap-1 lg:gap-2">
          {navItems.map((item) => (
            <a
              key={item.label}
              href={item.href}
              className={`transition-all duration-200 text-xs lg:text-sm font-medium ${
                item.active
                  ? 'bg-[#FCEFEA] text-[#9C5B33] font-semibold px-4 py-1.5 rounded-full shadow-xs'
                  : 'text-gray-600 hover:text-[#1A1F36] px-3 py-1.5 rounded-full hover:bg-black/[0.03]'
              }`}
            >
              {item.label}
            </a>
          ))}
        </nav>

        {/* Right: Actions */}
        <div className="flex items-center gap-2 sm:gap-3">
          {/* Subtle Settings / Dial Icon */}
          <button
            type="button"
            aria-label="Preferences"
            className="w-8 h-8 rounded-full flex items-center justify-center text-gray-500 hover:text-[#1A1F36] hover:bg-black/[0.04] transition-colors cursor-pointer"
          >
            <SlidersHorizontal className="w-4 h-4" />
          </button>

          {isAuthenticated ? (
            <>
              <span className="hidden sm:inline-flex text-xs font-semibold text-gray-700 bg-gray-100 px-3 py-1.5 rounded-full">
                {user?.name?.split(' ')[0] || 'User'}
              </span>
              <Button
                variant="primary"
                size={isScrolled ? 'sm' : 'md'}
                onClick={() => navigate(user?.onboardingCompleted ? '/dashboard' : '/onboarding')}
                className="!rounded-full font-medium"
              >
                Dashboard
              </Button>
              <button
                type="button"
                onClick={logout}
                className="hidden sm:inline-flex text-xs lg:text-sm font-semibold text-red-600 hover:text-red-700 px-3 py-1.5 transition-colors cursor-pointer"
              >
                Sign Out
              </button>
            </>
          ) : (
            <>
              {/* Sign In */}
              <Link
                to="/login"
                className="hidden sm:inline-flex text-xs lg:text-sm font-semibold text-[#1A1F36] hover:text-[#A06A42] px-3 py-1.5 transition-colors cursor-pointer"
              >
                Sign In
              </Link>

              {/* Get Started Button */}
              <Button
                variant="primary"
                size={isScrolled ? 'sm' : 'md'}
                onClick={() => navigate('/login')}
                className="!rounded-full font-medium"
              >
                Get Started
              </Button>
            </>
          )}

          {/* Mobile Hamburger Toggle */}
          <button
            type="button"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            aria-label="Toggle menu"
            className="md:hidden p-1.5 text-gray-700 hover:text-[#1A1F36] hover:bg-black/5 rounded-full transition cursor-pointer"
          >
            {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>
        </div>
      </motion.header>

      {/* Mobile Drawer Navigation */}
      <AnimatePresence>
        {mobileMenuOpen && (
          <motion.div
            initial={{ opacity: 0, y: -10, scale: 0.95 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -10, scale: 0.95 }}
            transition={{ duration: 0.2 }}
            className="md:hidden absolute top-20 left-4 right-4 bg-white/95 backdrop-blur-2xl border border-gray-100 rounded-3xl p-5 shadow-2xl z-50 pointer-events-auto flex flex-col gap-3"
          >
            {navItems.map((item) => (
              <a
                key={item.label}
                href={item.href}
                onClick={() => setMobileMenuOpen(false)}
                className={`px-4 py-2.5 rounded-2xl text-sm font-medium transition ${
                  item.active
                    ? 'bg-[#FCEFEA] text-[#9C5B33] font-semibold'
                    : 'text-gray-700 hover:bg-gray-50'
                }`}
              >
                {item.label}
              </a>
            ))}
            <div className="pt-3 border-t border-gray-100 flex flex-col gap-2">
              <button
                type="button"
                className="w-full text-center py-2.5 text-sm font-semibold text-[#1A1F36] hover:text-[#A06A42]"
              >
                Sign In
              </button>
              <Button
                variant="primary"
                size="md"
                className="w-full justify-center"
              >
                Get Started
              </Button>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};
