import React, { useState } from 'react';
import { ChevronDown, ChevronUp } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

export interface FAQItemData {
  id: string;
  question: string;
  answer: string;
  category?: string;
}

const FAQS: FAQItemData[] = [
  {
    id: 'faq-1',
    question: 'How does the AI Match determine my learning path?',
    answer:
      'LearnAI uses your initial assessment scores, stated career goals, and ongoing progress data to continuously tailor your curriculum. The AI Match algorithm looks for knowledge gaps and adjusts the difficulty in real-time.',
    category: 'learning',
  },
  {
    id: 'faq-2',
    question: 'Can I change my career goals later?',
    answer:
      'Yes, you can update your career goals at any time in your Profile Settings. Your learning path will automatically recalibrate to align with your new objectives.',
    category: 'account',
  },
  {
    id: 'faq-3',
    question: 'What are the rules for completing assessments?',
    answer:
      'Assessments are untimed but must be completed in a single session. You can retake an assessment after a 24-hour cooldown period to ensure you have time to review the material.',
    category: 'learning',
  },
];

interface FAQSectionProps {
  searchQuery?: string;
  onAskAIMentor?: () => void;
}

export const FAQSection: React.FC<FAQSectionProps> = ({
  searchQuery = '',
  onAskAIMentor,
}) => {
  const [openId, setOpenId] = useState<string | null>('faq-1');

  const filteredFaqs = FAQS.filter(
    (faq) =>
      faq.question.toLowerCase().includes(searchQuery.toLowerCase()) ||
      faq.answer.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const toggleItem = (id: string) => {
    setOpenId(openId === id ? null : id);
  };

  return (
    <section
      aria-label="Frequently Asked Questions"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-8 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-6 select-none"
    >
      <h3 className="text-lg sm:text-xl font-extrabold text-[#0f1b32] tracking-tight">
        Frequently Asked Questions
      </h3>

      {filteredFaqs.length > 0 ? (
        <div className="space-y-4">
          {filteredFaqs.map((faq, idx) => {
            const isOpen = openId === faq.id;

            return (
              <div
                key={faq.id}
                className={`rounded-2xl border transition-all ${
                  isOpen
                    ? 'bg-white/90 border-[#FAF4F0]'
                    : 'bg-white/60 border-transparent hover:bg-white/80'
                } ${idx !== filteredFaqs.length - 1 ? 'pb-2' : ''}`}
              >
                {/* Question Header */}
                <button
                  type="button"
                  aria-expanded={isOpen}
                  onClick={() => toggleItem(faq.id)}
                  className="w-full flex items-center justify-between gap-4 p-4 text-left cursor-pointer"
                >
                  <span className="text-xs sm:text-sm font-extrabold text-[#0f1b32]">
                    {faq.question}
                  </span>
                  {isOpen ? (
                    <ChevronUp className="w-4 h-4 text-gray-400 shrink-0" />
                  ) : (
                    <ChevronDown className="w-4 h-4 text-gray-400 shrink-0" />
                  )}
                </button>

                {/* Answer Content */}
                <AnimatePresence>
                  {isOpen && (
                    <motion.div
                      initial={{ height: 0, opacity: 0 }}
                      animate={{ height: 'auto', opacity: 1 }}
                      exit={{ height: 0, opacity: 0 }}
                      transition={{ duration: 0.2 }}
                      className="overflow-hidden"
                    >
                      <div className="px-4 pb-4 pt-1 text-xs text-[#53433c] font-normal leading-relaxed">
                        {faq.answer}
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>
            );
          })}
        </div>
      ) : (
        /* Empty State */
        <div className="text-center py-8 space-y-3">
          <p className="text-xs text-gray-500 font-medium">
            No help articles found for &ldquo;{searchQuery}&rdquo;.
          </p>
          <button
            type="button"
            onClick={onAskAIMentor}
            className="px-4 py-2 rounded-xl bg-[#FAF4F0] border border-[#F2DACB] text-[#8e4d2b] font-bold text-xs hover:bg-[#F2DACB]/60 transition-colors"
          >
            Ask AI Mentor
          </button>
        </div>
      )}
    </section>
  );
};
