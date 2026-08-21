import React, { useState, useRef, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Sparkles, CheckCircle2, Bot, MoreVertical, Trash2 } from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { AIMentorSidebar } from '../components/ai-mentor/AIMentorSidebar';
import { AIMentorTopBar } from '../components/ai-mentor/AIMentorTopBar';
import { MentorMessage, type ChatMessageItem } from '../components/ai-mentor/MentorMessage';
import { MentorComposer } from '../components/ai-mentor/MentorComposer';
import { MentorContextPanel } from '../components/ai-mentor/MentorContextPanel';
import { MentorRecommendationCard } from '../components/ai-mentor/MentorRecommendationCard';
import { TodaysPlanCard } from '../components/ai-mentor/TodaysPlanCard';
import { ConversationHistory } from '../components/ai-mentor/ConversationHistory';
import { MentorPersonalizationCard } from '../components/ai-mentor/MentorPersonalizationCard';
import api from '../api/client';
import { DashboardAggregated } from '../api/types';

const INITIAL_WELCOME: ChatMessageItem[] = [
  {
    id: 'msg-1',
    role: 'mentor',
    type: 'welcome',
  },
];

export const AIMentorPage: React.FC = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [messages, setMessages] = useState<ChatMessageItem[]>(INITIAL_WELCOME);
  const [isThinking, setIsThinking] = useState(false);
  const [showUpgradeModal, setShowUpgradeModal] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const [toast, setToast] = useState<string | null>(null);

  const [dashboardData, setDashboardData] = useState<DashboardAggregated | null>(null);

  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isThinking]);

  // Load chat history and learner context on mount
  useEffect(() => {
    const loadData = async () => {
      try {
        const [history, dash] = await Promise.all([
          api.getMentorHistory().catch(() => []),
          api.getDashboardData().catch(() => null),
        ]);

        if (dash) setDashboardData(dash);

        if (history && history.length > 0) {
          const mapped: ChatMessageItem[] = history.map((msg) => ({
            id: msg.id,
            role: msg.role,
            type: 'text',
            content: msg.content,
            timestamp: msg.createdAt,
          }));
          setMessages([INITIAL_WELCOME[0], ...mapped]);
        }
      } catch (err) {
        console.error('Failed to load mentor initial data:', err);
      }
    };
    loadData();
  }, []);

  const showToastNotice = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  };

  const handleClearConversation = async () => {
    try {
      await api.clearMentorHistory();
    } catch (err) {
      console.error('Failed to clear history on backend:', err);
    }
    setMessages(INITIAL_WELCOME);
    setMenuOpen(false);
    showToastNotice('Conversation cleared.');
  };

  const handleSendMessage = async (text: string) => {
    if (!text.trim()) return;

    const userMsgId = `user-${Date.now()}`;
    const newMessages: ChatMessageItem[] = [
      ...messages,
      { id: userMsgId, role: 'user', type: 'text', content: text },
    ];
    setMessages(newMessages);
    setIsThinking(true);

    try {
      const response = await api.sendMentorMessage(text);
      const mentorReply = response.reply || "I've analyzed your learning goal and recommend focusing on your next core topic.";

      setMessages((prev) => [
        ...prev,
        {
          id: response.messageId || `mentor-${Date.now()}`,
          role: 'mentor',
          type: 'text',
          content: mentorReply,
        },
      ]);
    } catch (err: any) {
      console.error('Failed to get mentor response:', err);
      setMessages((prev) => [
        ...prev,
        {
          id: `mentor-fallback-${Date.now()}`,
          role: 'mentor',
          type: 'text',
          content: `Based on your target of **${dashboardData?.targetCareer || 'Software Engineer'}**, I recommend continuing with your core curriculum modules and practicing daily coding challenges.`,
        },
      ]);
    } finally {
      setIsThinking(false);
    }
  };

  const handleSelectPrompt = (prompt: string) => {
    handleSendMessage(prompt);
  };

  const handleVisualExample = () => {
    handleSendMessage('Can you show me a step-by-step visual example of Binary Search?');
  };

  const handleExplainFurther = () => {
    handleSendMessage('Can you explain time and space complexity trade-offs in depth?');
  };

  const handleLoadPastTopic = (topic: string) => {
    handleSendMessage(`Let's focus on ${topic}. What should I work on next?`);
  };

  return (
    <div className="relative min-h-screen flex bg-[#f9f9ff] text-[#0f1b32] selection:bg-[#ffdbcb] selection:text-[#8e4d2b] overflow-x-hidden">
      {/* Soft atmospheric background lighting */}
      <AmbientBackground />

      {/* Desktop Fixed Left Sidebar */}
      <AIMentorSidebar onUpgrade={() => setShowUpgradeModal(true)} />

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
              transition={{ type: 'spring', damping: 25, stiffness: 280 }}
              className="relative w-[260px] bg-white h-full z-10 p-5 flex flex-col justify-between shadow-2xl"
            >
              <div className="flex items-center justify-between pb-4 border-b border-gray-100">
                <div className="flex items-center gap-2">
                  <span className="font-extrabold text-lg text-[#8e4d2b]">LearnAI</span>
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
                  href="/ai-mentor"
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-bold bg-[#ffdbcb]/60 text-[#8e4d2b]"
                >
                  AI Mentor
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
        {/* Sticky App Header */}
        <AIMentorTopBar
          onToggleMobileMenu={() => setMobileMenuOpen(true)}
          searchQuery={searchQuery}
          onSearchChange={setSearchQuery}
        />

        {/* Content Container */}
        <main className="flex-1 px-4 sm:px-8 py-6 sm:py-8 max-w-7xl w-full mx-auto space-y-6">
          {/* Main 2-Column Split: 8 cols (Chat) + 4 cols (Context & AI Cards) */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 sm:gap-8 items-start">
            {/* Left 8 Columns: Main Chat Area */}
            <div className="lg:col-span-8 flex flex-col space-y-4">
              {/* Chat Canvas Box */}
              <div className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-4 sm:p-6 shadow-[0_8px_32px_rgba(23,35,58,0.04)] flex flex-col justify-between min-h-[580px] max-h-[75vh]">
                {/* Chat Header inside card */}
                <div className="flex items-center justify-between pb-3 border-b border-gray-100/90 mb-4 select-none">
                  <div className="flex items-center gap-2.5">
                    <div className="w-8 h-8 rounded-xl bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b]">
                      <Bot className="w-4 h-4" />
                    </div>
                    <div>
                      <h2 className="text-sm font-bold text-[#0f1b32] tracking-tight leading-tight">
                        AI Mentor Session
                      </h2>
                      <div className="flex items-center gap-1.5 mt-0.5">
                        <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                        <span className="text-[10px] font-semibold text-gray-500 uppercase tracking-wider">
                          ONLINE & READY
                        </span>
                      </div>
                    </div>
                  </div>

                  {/* Options Menu Button */}
                  <div className="relative">
                    <button
                      type="button"
                      aria-label="Conversation Options"
                      onClick={() => setMenuOpen(!menuOpen)}
                      className="p-1.5 rounded-xl hover:bg-gray-100 text-gray-400 hover:text-gray-600 transition-colors cursor-pointer"
                    >
                      <MoreVertical className="w-4 h-4" />
                    </button>

                    {menuOpen && (
                      <div className="absolute right-0 top-full mt-1 w-44 bg-white rounded-2xl shadow-xl border border-gray-100 py-1.5 z-20 text-xs font-semibold text-[#0f1b32]">
                        <button
                          type="button"
                          onClick={handleClearConversation}
                          className="w-full px-3.5 py-2 flex items-center gap-2 hover:bg-red-50 text-red-600 transition-colors text-left cursor-pointer"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                          <span>Clear Conversation</span>
                        </button>
                      </div>
                    )}
                  </div>
                </div>

                {/* Messages Scroll Area */}
                <div className="flex-1 overflow-y-auto space-y-4 pr-1 scrollbar-thin scrollbar-thumb-gray-200">
                  {messages.map((message) => (
                    <MentorMessage
                      key={message.id}
                      message={message}
                      onSelectPrompt={handleSelectPrompt}
                      onVisualExample={handleVisualExample}
                      onExplainFurther={handleExplainFurther}
                    />
                  ))}

                  {/* Thinking Animation */}
                  {isThinking && (
                    <motion.div
                      initial={{ opacity: 0, y: 6 }}
                      animate={{ opacity: 1, y: 0 }}
                      className="flex items-center gap-2 text-xs font-semibold text-gray-400 pl-11 select-none"
                    >
                      <Sparkles className="w-3.5 h-3.5 text-[#8e4d2b] animate-spin" />
                      <span>LearnAI Mentor is thinking...</span>
                    </motion.div>
                  )}

                  <div ref={messagesEndRef} />
                </div>

                {/* Bottom Composer */}
                <div className="pt-4 border-t border-gray-100/90 mt-2">
                  <MentorComposer
                    isThinking={isThinking}
                    onSendMessage={handleSendMessage}
                    onSelectContextChip={(chip) => handleSendMessage(`Let's focus on ${chip}`)}
                    onMicClick={() => showToastNotice('Voice input ready')}
                  />
                </div>
              </div>
            </div>

            {/* Right 4 Columns: Side Panels */}
            <div className="lg:col-span-4 space-y-5">
              {/* Context Summary */}
              <MentorContextPanel />

              {/* Today's Goal / Plan Card */}
              <TodaysPlanCard />

              {/* Next Recommendation Card */}
              <MentorRecommendationCard
                onStartTopic={() => handleSendMessage('Why was Binary Search recommended for my path?')}
              />

              {/* Personalization Setting Toggle Card */}
              <MentorPersonalizationCard />

              {/* Past Topic History */}
              <ConversationHistory onSelectConversation={handleLoadPastTopic} />
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
                  <h3 className="text-base font-bold text-[#0f1b32]">LearnAI Pro Mentorship</h3>
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
                Unlock unlimited AI Mentor queries, real-time code reviews, personalized diagnostic study plans, and live mock interview sessions.
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
