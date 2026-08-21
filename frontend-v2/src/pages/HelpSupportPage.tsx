import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Sparkles, CheckCircle2 } from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { HelpSidebar } from '../components/help-support/HelpSidebar';
import { HelpTopBar } from '../components/help-support/HelpTopBar';
import { HelpHero } from '../components/help-support/HelpHero';
import { QuickSupportGrid } from '../components/help-support/QuickSupportGrid';
import { FAQSection } from '../components/help-support/FAQSection';
import { AIMentorSupportCard } from '../components/help-support/AIMentorSupportCard';
import { HumanSupportForm } from '../components/help-support/HumanSupportForm';
import { SupportStatusCard, type SupportRequestItem } from '../components/help-support/SupportStatusCard';
import api from '../api/client';

const DEFAULT_REQUESTS: SupportRequestItem[] = [
  {
    id: 'req-1',
    title: 'Unable to update profile picture',
    status: 'In Progress',
  },
  {
    id: 'req-2',
    title: 'Course video not loading',
    status: 'Resolved',
  },
];

export const HelpSupportPage: React.FC = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [showUpgradeModal, setShowUpgradeModal] = useState(false);
  const [recentRequests, setRecentRequests] = useState<SupportRequestItem[]>(DEFAULT_REQUESTS);
  const [toast, setToast] = useState<string | null>(null);

  const fetchTickets = async () => {
    try {
      const tickets = await api.getSupportTickets();
      if (tickets && tickets.length > 0) {
        const mapped: SupportRequestItem[] = tickets.map((t) => ({
          id: t.id,
          title: t.subject,
          status: t.status === 'RESOLVED' ? 'Resolved' : 'In Progress',
        }));
        setRecentRequests(mapped);
      }
    } catch (err) {
      console.error('Failed to load tickets:', err);
    }
  };

  useEffect(() => {
    fetchTickets();
  }, []);

  const showToastNotice = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  };

  const handleSupportSubmit = async (subject: string, category: string) => {
    try {
      const created = await api.createSupportTicket({
        subject,
        category: category || 'Technical Issue',
        description: `User submitted request under category ${category}: ${subject}`,
      });
      const newRequest: SupportRequestItem = {
        id: created.id,
        title: created.subject,
        status: 'In Progress',
      };
      setRecentRequests([newRequest, ...recentRequests]);
      showToastNotice('Support request submitted successfully. Ticket created!');
    } catch (err: any) {
      showToastNotice(err.message || 'Failed to submit ticket');
    }
  };

  const handleCategorySelect = (category: string) => {
    if (category === 'learning') {
      setSearchQuery('learning path');
    } else if (category === 'account') {
      setSearchQuery('career goals');
    } else if (category === 'technical') {
      showToastNotice('Please describe your technical issue below.');
    }
  };

  return (
    <div className="relative min-h-screen flex bg-[#f9f9ff] text-[#0f1b32] selection:bg-[#ffdbcb] selection:text-[#8e4d2b] overflow-x-hidden">
      {/* Background Ambient Aura */}
      <AmbientBackground />

      {/* Desktop Left Fixed Sidebar */}
      <HelpSidebar onUpgrade={() => setShowUpgradeModal(true)} />

      {/* Mobile Navigation Drawer */}
      <AnimatePresence>
        {mobileMenuOpen && (
          <div className="fixed inset-0 z-50 lg:hidden flex">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setMobileMenuOpen(false)}
              className="fixed inset-0 bg-black/30 backdrop-blur-xs"
            />
            <motion.div
              initial={{ x: -260 }}
              animate={{ x: 0 }}
              exit={{ x: -260 }}
              transition={{ type: 'spring', damping: 25, stiffness: 280 }}
              className="relative w-[260px] bg-white h-full z-10 p-5 flex flex-col justify-between shadow-2xl"
            >
              <div className="flex items-center justify-between pb-4 border-b border-gray-100">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-xl bg-[#8e4d2b] text-white flex items-center justify-center font-bold text-sm">
                    L
                  </div>
                  <span className="font-extrabold text-base text-[#0f1b32]">LearnAI</span>
                </div>
                <button
                  type="button"
                  aria-label="Close menu"
                  onClick={() => setMobileMenuOpen(false)}
                  className="p-1 text-gray-400 hover:text-gray-600"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              <div className="space-y-1 py-4 text-xs">
                <a
                  href="/dashboard"
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-medium text-gray-700 hover:bg-gray-50"
                >
                  Dashboard
                </a>
                <a
                  href="/learning-path"
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-medium text-gray-700 hover:bg-gray-50"
                >
                  My Learning Path
                </a>
                <a
                  href="/settings"
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-medium text-gray-700 hover:bg-gray-50"
                >
                  Settings
                </a>
                <a
                  href="/help-support"
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-bold bg-[#ffdbcb]/60 text-[#8e4d2b]"
                >
                  Help & Support
                </a>
              </div>

              <div className="pt-4 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => {
                    setMobileMenuOpen(false);
                    setShowUpgradeModal(true);
                  }}
                  className="w-full py-2 rounded-xl bg-[#FAF4F0] border border-[#F2DACB] text-[#8e4d2b] font-bold text-xs"
                >
                  Upgrade to Pro
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Main Workspace Frame */}
      <div className="flex-1 flex flex-col min-w-0 z-10">
        {/* Sticky Top Bar */}
        <HelpTopBar
          onToggleMobileMenu={() => setMobileMenuOpen(true)}
        />

        {/* Page Content Body */}
        <main className="flex-1 px-4 sm:px-8 py-6 sm:py-8 max-w-7xl w-full mx-auto space-y-6 sm:space-y-8">
          {/* Top Hero Section */}
          <HelpHero
            searchQuery={searchQuery}
            onSearchChange={setSearchQuery}
            onSelectPopular={(topic) => setSearchQuery(topic)}
          />

          {/* Quick Support 3-Card Grid */}
          <QuickSupportGrid onSelectCategory={handleCategorySelect} />

          {/* Two-Column Support Content Layout */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 sm:gap-8 items-start">
            {/* Left 8 Columns: FAQs */}
            <div className="lg:col-span-8 space-y-6 sm:space-y-8">
              <FAQSection searchQuery={searchQuery} />
            </div>

            {/* Right 4 Columns: AI Mentor Card, Human Form & Status */}
            <div className="lg:col-span-4 space-y-6 sm:space-y-7">
              <AIMentorSupportCard />

              <HumanSupportForm onSubmitRequest={handleSupportSubmit} />

              <SupportStatusCard recentRequests={recentRequests} />
            </div>
          </div>
        </main>
      </div>

      {/* Upgrade to Pro Modal */}
      <AnimatePresence>
        {showUpgradeModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setShowUpgradeModal(false)}
              className="fixed inset-0 bg-black/40 backdrop-blur-xs"
            />
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              className="relative w-full max-w-md bg-white rounded-3xl p-6 sm:p-7 shadow-2xl border border-gray-100 z-10 text-left space-y-4"
            >
              <div className="flex items-center justify-between pb-3 border-b border-gray-100">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-xl bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b]">
                    <Sparkles className="w-4 h-4 text-[#8e4d2b]" />
                  </div>
                  <h3 className="text-base font-bold text-[#0f1b32]">Priority Support with Pro</h3>
                </div>
                <button
                  type="button"
                  aria-label="Close modal"
                  onClick={() => setShowUpgradeModal(false)}
                  className="text-gray-400 hover:text-gray-600 p-1"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              <p className="text-xs sm:text-sm text-gray-600 leading-relaxed font-normal">
                Upgrade to LearnAI Pro to receive 24/7 dedicated support engineers, real-time AI code debugging assistance, and guaranteed 1-hour ticket response times.
              </p>

              <button
                type="button"
                onClick={() => {
                  setShowUpgradeModal(false);
                  showToastNotice('Pro upgrade will be available soon!');
                }}
                className="w-full py-3 rounded-2xl bg-[#8e4d2b] hover:bg-[#783e20] text-white text-xs font-bold transition-colors cursor-pointer shadow-sm text-center"
              >
                Upgrade to Pro
              </button>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Live Toast Notification */}
      <AnimatePresence>
        {toast && (
          <motion.div
            initial={{ opacity: 0, y: 18 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 18 }}
            className="fixed bottom-6 left-1/2 -translate-x-1/2 bg-[#0f1b32] text-white text-xs font-medium px-4 py-2.5 rounded-full shadow-lg z-50 flex items-center gap-2 whitespace-nowrap"
          >
            <CheckCircle2 className="w-3.5 h-3.5 text-[#ffdbcb]" />
            <span>{toast}</span>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};
