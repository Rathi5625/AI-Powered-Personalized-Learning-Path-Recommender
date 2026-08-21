import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Bot, X, Send, User } from 'lucide-react';

interface AIMentorModalProps {
  isOpen: boolean;
  onClose: () => void;
}

interface ChatMessage {
  id: string;
  sender: 'user' | 'ai';
  text: string;
}

export const AIMentorModal: React.FC<AIMentorModalProps> = ({
  isOpen,
  onClose,
}) => {
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: '1',
      sender: 'user',
      text: 'How should I structure JWT authentication for this project?',
    },
    {
      id: '2',
      sender: 'ai',
      text: 'For this Spring Boot E-Commerce project, start by creating a JwtUtils component to sign and validate tokens. Next, implement a JwtAuthenticationFilter extending OncePerRequestFilter, and wire it into your SecurityFilterChain before the UsernamePasswordAuthenticationFilter.',
    },
  ]);
  const [inputVal, setInputVal] = useState('');

  const handleSend = (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputVal.trim()) return;

    const userText = inputVal.trim();
    const newMsgId = Date.now().toString();

    setMessages((prev) => [
      ...prev,
      { id: newMsgId, sender: 'user', text: userText },
    ]);
    setInputVal('');

    // Simulated local mock AI response
    setTimeout(() => {
      setMessages((prev) => [
        ...prev,
        {
          id: (Date.now() + 1).toString(),
          sender: 'ai',
          text: `Great question regarding "${userText}". Make sure your UserDetailsService loads roles properly and passwords use BCryptPasswordEncoder.`,
        },
      ]);
    }, 600);
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          {/* Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="fixed inset-0 bg-black/40 backdrop-blur-xs"
          />

          {/* Centered Modal Card */}
          <motion.div
            initial={{ scale: 0.95, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            exit={{ scale: 0.95, opacity: 0 }}
            className="relative w-full max-w-lg bg-white/95 backdrop-blur-2xl rounded-3xl p-6 sm:p-7 shadow-2xl border border-white/90 z-10 text-left flex flex-col max-h-[85vh] space-y-4"
          >
            {/* Header */}
            <div className="flex items-center justify-between pb-3 border-b border-gray-100">
              <div className="flex items-center gap-2.5">
                <div className="w-8 h-8 rounded-xl bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b]">
                  <Bot className="w-4 h-4 text-[#8e4d2b]" />
                </div>
                <div>
                  <h3 className="text-base font-extrabold text-[#0f1b32]">AI Mentor</h3>
                  <span className="text-[11px] text-gray-400 font-medium">Spring Boot E-Commerce API</span>
                </div>
              </div>

              <button
                type="button"
                aria-label="Close modal"
                onClick={onClose}
                className="p-1.5 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100 transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Conversation Log */}
            <div className="flex-1 overflow-y-auto space-y-3 pr-1 py-1">
              {messages.map((msg) => (
                <div
                  key={msg.id}
                  className={`flex gap-2.5 ${msg.sender === 'user' ? 'justify-end' : 'justify-start'}`}
                >
                  {msg.sender === 'ai' && (
                    <div className="w-6 h-6 rounded-full bg-[#ffdbcb] text-[#8e4d2b] flex items-center justify-center shrink-0 mt-1 text-[10px] font-bold">
                      <Bot className="w-3.5 h-3.5" />
                    </div>
                  )}

                  <div
                    className={`p-3.5 rounded-2xl text-xs leading-relaxed max-w-[82%] ${
                      msg.sender === 'user'
                        ? 'bg-[#8e4d2b] text-white rounded-br-xs'
                        : 'bg-[#FAF4F0] text-[#0f1b32] border border-[#F2DACB]/60 rounded-bl-xs'
                    }`}
                  >
                    {msg.text}
                  </div>

                  {msg.sender === 'user' && (
                    <div className="w-6 h-6 rounded-full bg-gray-200 text-gray-700 flex items-center justify-center shrink-0 mt-1 text-[10px] font-bold">
                      <User className="w-3.5 h-3.5" />
                    </div>
                  )}
                </div>
              ))}
            </div>

            {/* Message Input Form */}
            <form onSubmit={handleSend} className="pt-2 border-t border-gray-100 flex items-center gap-2">
              <input
                type="text"
                value={inputVal}
                onChange={(e) => setInputVal(e.target.value)}
                placeholder="Ask about this project..."
                className="flex-1 px-4 py-2.5 rounded-xl bg-gray-50 border border-gray-200 text-xs text-[#0f1b32] placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-[#8e4d2b]/20 focus:border-[#8e4d2b]"
              />
              <button
                type="submit"
                aria-label="Send message"
                className="p-2.5 rounded-xl bg-[#8e4d2b] hover:bg-[#783e20] text-white transition-colors cursor-pointer shadow-2xs"
              >
                <Send className="w-4 h-4" />
              </button>
            </form>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};
