import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Bell,
  CheckCircle2,
  BookOpen,
  Sparkles,
  Award,
  TrendingUp,
  FolderGit2,
  Bot,
  Settings,
  Check,
  Menu,
  X,
} from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { AIMentorSidebar } from '../components/ai-mentor/AIMentorSidebar';
import api from '../api/client';

interface NotificationItem {
  id: string;
  title: string;
  description: string;
  category: 'learning' | 'ai' | 'assessments' | 'projects' | 'system';
  time: string;
  unread: boolean;
  actionText: string;
  actionHref: string;
  icon: React.ElementType;
}

const DEFAULT_NOTIFICATIONS: NotificationItem[] = [
  {
    id: 'notif-1',
    title: 'Time to continue learning',
    description: 'You planned 45 minutes of DSA practice today.',
    category: 'learning',
    time: '10m ago',
    unread: true,
    actionText: 'Start Learning →',
    actionHref: '/learning-path',
    icon: BookOpen,
  },
  {
    id: 'notif-2',
    title: 'LearnAI found your next best topic',
    description: "You're ready to move from Arrays into Binary Search.",
    category: 'ai',
    time: '1h ago',
    unread: true,
    actionText: 'View Recommendation',
    actionHref: '/ai-mentor',
    icon: Sparkles,
  },
  {
    id: 'notif-3',
    title: 'Your DSA assessment results are ready',
    description: 'You scored 78% and your estimated DSA skill increased by 7%.',
    category: 'assessments',
    time: '3h ago',
    unread: true,
    actionText: 'View Results',
    actionHref: '/assessment-results',
    icon: Award,
  },
  {
    id: 'notif-4',
    title: 'Your Java skill improved',
    description: 'Your estimated Java skill increased from 72% to 78%.',
    category: 'learning',
    time: 'Yesterday',
    unread: false,
    actionText: 'View Skills',
    actionHref: '/skills',
    icon: TrendingUp,
  },
  {
    id: 'notif-5',
    title: "You're making progress on your project",
    description: 'Your Spring Boot E-Commerce API is 68% complete.',
    category: 'projects',
    time: 'Yesterday',
    unread: false,
    actionText: 'Continue Project',
    actionHref: '/project-details',
    icon: FolderGit2,
  },
];

type FilterCategory = 'All' | 'Unread' | 'Learning' | 'AI' | 'Assessments' | 'Projects';

export const NotificationsPage: React.FC = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [activeFilter, setActiveFilter] = useState<FilterCategory>('All');
  const [notifications, setNotifications] = useState<NotificationItem[]>(DEFAULT_NOTIFICATIONS);
  const [toast, setToast] = useState<string | null>(null);

  const fetchNotifications = async () => {
    try {
      const data = await api.getNotifications();
      if (data && data.length > 0) {
        const mapped: NotificationItem[] = data.map((n) => {
          let icon = BookOpen;
          let cat: NotificationItem['category'] = 'learning';
          let actionHref = n.actionUrl || '/learning-path';

          if (n.category === 'AI') {
            icon = Sparkles;
            cat = 'ai';
            actionHref = n.actionUrl || '/ai-mentor';
          } else if (n.category === 'ASSESSMENTS') {
            icon = Award;
            cat = 'assessments';
            actionHref = n.actionUrl || '/assessment-results';
          } else if (n.category === 'PROJECTS') {
            icon = FolderGit2;
            cat = 'projects';
            actionHref = n.actionUrl || '/project-details';
          }

          return {
            id: n.id,
            title: n.title,
            description: n.message,
            category: cat,
            time: 'Recently',
            unread: !n.read,
            actionText: 'View Details →',
            actionHref,
            icon,
          };
        });
        setNotifications(mapped);
      }
    } catch (err) {
      console.error('Failed to load notifications:', err);
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, []);

  const showToastNotice = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  };

  const handleMarkAllRead = async () => {
    try {
      await api.markAllNotificationsAsRead();
      setNotifications((prev) => prev.map((i) => ({ ...i, unread: false })));
      showToastNotice('All notifications marked as read.');
    } catch (err: any) {
      setNotifications((prev) => prev.map((i) => ({ ...i, unread: false })));
      showToastNotice('All notifications marked as read.');
    }
  };

  const handleItemClick = async (item: NotificationItem) => {
    if (item.unread) {
      try {
        await api.markNotificationAsRead(item.id);
      } catch (err) {
        console.error('Failed to mark read:', err);
      }
      setNotifications((prev) =>
        prev.map((n) => (n.id === item.id ? { ...n, unread: false } : n))
      );
    }
  };

  // Count metrics
  let totalCount = notifications.length;
  let unreadCount = notifications.filter((i) => i.unread).length;
  let learningCount = notifications.filter((i) => i.category === 'learning').length;
  let aiCount = notifications.filter((i) => i.category === 'ai').length;

  const filteredItems = notifications.filter((item) => {
    if (activeFilter === 'All') return true;
    if (activeFilter === 'Unread') return item.unread;
    if (activeFilter === 'Learning') return item.category === 'learning';
    if (activeFilter === 'AI') return item.category === 'ai';
    if (activeFilter === 'Assessments') return item.category === 'assessments';
    if (activeFilter === 'Projects') return item.category === 'projects';
    return true;
  });

  return (
    <div className="relative min-h-screen flex bg-[#f9f9ff] text-[#0f1b32] selection:bg-[#ffdbcb] selection:text-[#8e4d2b] overflow-x-hidden">
      <AmbientBackground />

      {/* Desktop Fixed Left Sidebar */}
      <AIMentorSidebar />

      {/* Mobile Drawer */}
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
              className="relative w-[260px] bg-white h-full z-10 p-5 flex flex-col justify-between shadow-2xl"
            >
              <div className="flex items-center justify-between pb-4 border-b border-gray-100">
                <span className="font-extrabold text-lg text-[#8e4d2b]">LearnAI</span>
                <button
                  type="button"
                  onClick={() => setMobileMenuOpen(false)}
                  className="p-1 text-gray-400"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
              <div className="space-y-1 py-4 text-xs">
                <Link to="/dashboard" className="block px-3 py-2 text-gray-700">
                  Dashboard
                </Link>
                <Link to="/ai-mentor" className="block px-3 py-2 text-gray-700">
                  AI Mentor
                </Link>
                <Link to="/notifications" className="block px-3 py-2 font-bold text-[#8e4d2b]">
                  Notifications
                </Link>
                <Link to="/settings" className="block px-3 py-2 text-gray-700">
                  Settings
                </Link>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Main Workspace */}
      <div className="flex-1 flex flex-col min-w-0 z-10">
        {/* Top Bar */}
        <header className="w-full h-16 sm:h-20 px-4 sm:px-8 flex items-center justify-between gap-4 sticky top-0 z-20 bg-[#f9f9ff]/80 backdrop-blur-xl border-b border-gray-200/60 select-none">
          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={() => setMobileMenuOpen(true)}
              className="lg:hidden p-2 rounded-xl text-[#0f1b32] hover:bg-black/5"
            >
              <Menu className="w-5 h-5" />
            </button>
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-xl bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b]">
                <Bell className="w-4 h-4" />
              </div>
              <h1 className="text-base sm:text-lg font-extrabold text-[#0f1b32] tracking-tight">
                Notifications
              </h1>
            </div>
          </div>
        </header>

        {/* Page Body */}
        <main className="flex-1 px-4 sm:px-8 py-6 sm:py-8 max-w-5xl w-full mx-auto space-y-6 text-left select-none">
          {/* Top Metric Cards */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 sm:gap-4">
            <div className="p-4 rounded-2xl bg-white/80 border border-white/90 shadow-2xs flex items-center gap-3">
              <div className="w-8 h-8 rounded-xl bg-[#FAF4F0] flex items-center justify-center text-[#8e4d2b]">
                <Bell className="w-4 h-4" />
              </div>
              <div>
                <span className="text-base font-extrabold text-[#0f1b32] block leading-none">
                  {totalCount}
                </span>
                <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider block mt-1">
                  TOTAL
                </span>
              </div>
            </div>

            <div className="p-4 rounded-2xl bg-white/80 border border-white/90 shadow-2xs flex items-center gap-3">
              <div className="w-8 h-8 rounded-xl bg-red-50 flex items-center justify-center text-red-600">
                <CheckCircle2 className="w-4 h-4" />
              </div>
              <div>
                <span className="text-base font-extrabold text-[#ba1a1a] block leading-none">
                  {unreadCount}
                </span>
                <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider block mt-1">
                  UNREAD
                </span>
              </div>
            </div>

            <div className="p-4 rounded-2xl bg-white/80 border border-white/90 shadow-2xs flex items-center gap-3">
              <div className="w-8 h-8 rounded-xl bg-blue-50 flex items-center justify-center text-blue-600">
                <BookOpen className="w-4 h-4" />
              </div>
              <div>
                <span className="text-base font-extrabold text-[#0f1b32] block leading-none">
                  {learningCount}
                </span>
                <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider block mt-1">
                  LEARNING
                </span>
              </div>
            </div>

            <div className="p-4 rounded-2xl bg-white/80 border border-white/90 shadow-2xs flex items-center gap-3">
              <div className="w-8 h-8 rounded-xl bg-[#e9edff] flex items-center justify-center text-[#615a7a]">
                <Bot className="w-4 h-4" />
              </div>
              <div>
                <span className="text-base font-extrabold text-[#0f1b32] block leading-none">
                  {aiCount}
                </span>
                <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider block mt-1">
                  AI RECS
                </span>
              </div>
            </div>
          </div>

          {/* Filter Pills Row */}
          <div className="flex items-center justify-between gap-3 flex-wrap pt-2">
            <div className="flex items-center gap-2 flex-wrap">
              {(['All', 'Unread', 'Learning', 'AI', 'Assessments', 'Projects'] as FilterCategory[]).map(
                (filter) => {
                  const isSelected = activeFilter === filter;
                  return (
                    <button
                      key={filter}
                      type="button"
                      onClick={() => setActiveFilter(filter)}
                      className={`
                        px-3.5 py-1.5 rounded-full text-xs font-bold transition-all cursor-pointer shadow-2xs
                        ${
                          isSelected
                            ? 'bg-[#FAF4F0] border border-[#F2DACB] text-[#8e4d2b]'
                            : 'bg-white/80 border border-gray-200/80 text-gray-500 hover:text-[#0f1b32]'
                        }
                      `}
                    >
                      {filter}
                    </button>
                  );
                }
              )}
            </div>

            <button
              type="button"
              onClick={handleMarkAllRead}
              className="inline-flex items-center gap-1.5 text-xs font-bold text-gray-500 hover:text-[#8e4d2b] transition-colors cursor-pointer"
            >
              <Check className="w-3.5 h-3.5" />
              <span>Mark All Read</span>
            </button>
          </div>

          {/* Categorized Notifications */}
          <div className="space-y-3 pt-2">
            {filteredItems.length === 0 ? (
              <div className="text-center py-12 bg-white/60 rounded-3xl border border-dashed border-gray-300">
                <Bell className="w-8 h-8 text-gray-400 mx-auto mb-2" />
                <p className="text-xs font-bold text-gray-500">No notifications in this filter.</p>
              </div>
            ) : (
              filteredItems.map((item) => {
                const Icon = item.icon;

                return (
                  <div
                    key={item.id}
                    onClick={() => handleItemClick(item)}
                    className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-4 sm:p-5 shadow-[0_8px_32px_rgba(23,35,58,0.04)] flex flex-col sm:flex-row sm:items-center justify-between gap-4 transition-all hover:bg-white/90 cursor-pointer"
                  >
                    <div className="flex items-start gap-3.5">
                      <div className="w-10 h-10 rounded-2xl bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b] shrink-0 mt-0.5">
                        <Icon className="w-5 h-5" />
                      </div>

                      <div className="space-y-1">
                        <div className="flex items-center gap-2">
                          <span className="text-xs sm:text-sm font-bold text-[#0f1b32]">
                            {item.title}
                          </span>
                          {item.unread && (
                            <span className="w-2 h-2 rounded-full bg-[#8e4d2b]" />
                          )}
                        </div>
                        <p className="text-xs text-[#53433c] font-normal leading-relaxed">
                          {item.description}
                        </p>

                        <div className="pt-1">
                          <Link
                            to={item.actionHref}
                            className="inline-flex items-center gap-1 text-xs font-bold text-[#8e4d2b] hover:text-[#783e20]"
                          >
                            {item.actionText}
                          </Link>
                        </div>
                      </div>
                    </div>

                    <div className="text-right shrink-0 self-start sm:self-center">
                      <span className="text-[11px] text-gray-400 font-medium">
                        {item.time}
                      </span>
                    </div>
                  </div>
                );
              })
            )}
          </div>

          {/* Bottom Control Your Notifications Banner */}
          <div className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-5 sm:p-6 shadow-[0_8px_32px_rgba(23,35,58,0.04)] flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div className="space-y-0.5">
              <h3 className="text-xs sm:text-sm font-bold text-[#0f1b32]">
                Control your notifications
              </h3>
              <p className="text-xs text-gray-500 font-normal">
                Choose which updates you want to receive.
              </p>
            </div>

            <Link
              to="/settings"
              className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-white/90 hover:bg-[#FAF4F0] border border-gray-200/80 text-xs font-bold text-[#0f1b32] transition-colors shadow-2xs"
            >
              <Settings className="w-3.5 h-3.5 text-gray-500" />
              <span>Notification Settings</span>
            </Link>
          </div>
        </main>
      </div>

      {/* Toast Notification */}
      <AnimatePresence>
        {toast && (
          <motion.div
            initial={{ opacity: 0, y: 18 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 18 }}
            className="fixed bottom-6 left-1/2 -translate-x-1/2 bg-[#0f1b32] text-white text-xs font-medium px-4 py-2.5 rounded-full shadow-lg z-50 flex items-center gap-2 whitespace-nowrap"
          >
            <Check className="w-3.5 h-3.5 text-[#ffdbcb]" />
            <span>{toast}</span>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};
