import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Calendar } from 'lucide-react';

interface ScheduleItem {
  day: string;
  topic: string;
  duration: string;
  color: string;
  isRest?: boolean;
}

interface SchedulePreviewProps {
  hoursId: string;
}

const SCHEDULES_BY_HOURS: Record<string, { label: string; items: ScheduleItem[] }> = {
  '5': {
    label: '5 hours / week',
    items: [
      { day: 'Mon', topic: 'DSA', duration: '1h', color: 'bg-[#CC7D52]' },
      { day: 'Tue', topic: 'Rest', duration: '', color: 'bg-gray-300', isRest: true },
      { day: 'Wed', topic: 'Practice', duration: '1h', color: 'bg-[#6B65E0]' },
      { day: 'Thu', topic: 'Java', duration: '1h', color: 'bg-[#CC7D52]' },
      { day: 'Fri', topic: 'Rest', duration: '', color: 'bg-gray-300', isRest: true },
    ],
  },
  '10': {
    label: '10 hours / week',
    items: [
      { day: 'Mon', topic: 'DSA', duration: '1h', color: 'bg-[#CC7D52]' },
      { day: 'Tue', topic: 'Java', duration: '1h', color: 'bg-[#CC7D52]' },
      { day: 'Wed', topic: 'Practice', duration: '1.5h', color: 'bg-[#6B65E0]' },
      { day: 'Thu', topic: 'DSA', duration: '1h', color: 'bg-[#CC7D52]' },
      { day: 'Fri', topic: 'Rest', duration: '', color: 'bg-gray-300', isRest: true },
    ],
  },
  '15': {
    label: '15 hours / week',
    items: [
      { day: 'Mon', topic: 'DSA', duration: '2h', color: 'bg-[#CC7D52]' },
      { day: 'Tue', topic: 'Java & Spring', duration: '2h', color: 'bg-[#CC7D52]' },
      { day: 'Wed', topic: 'Practice & Labs', duration: '2h', color: 'bg-[#6B65E0]' },
      { day: 'Thu', topic: 'Project Work', duration: '2h', color: 'bg-[#CC7D52]' },
      { day: 'Fri', topic: 'Review & Quizzes', duration: '1h', color: 'bg-[#6B65E0]' },
    ],
  },
  '20': {
    label: '20+ hours / week',
    items: [
      { day: 'Mon', topic: 'DSA Core', duration: '3h', color: 'bg-[#CC7D52]' },
      { day: 'Tue', topic: 'System Design', duration: '3h', color: 'bg-[#CC7D52]' },
      { day: 'Wed', topic: 'Full-Stack Labs', duration: '3h', color: 'bg-[#6B65E0]' },
      { day: 'Thu', topic: 'Project Sprint', duration: '3h', color: 'bg-[#CC7D52]' },
      { day: 'Fri', topic: 'Mock Interview', duration: '2h', color: 'bg-[#6B65E0]' },
    ],
  },
};

export const SchedulePreview: React.FC<SchedulePreviewProps> = ({ hoursId }) => {
  const currentSchedule = SCHEDULES_BY_HOURS[hoursId] || SCHEDULES_BY_HOURS['10'];

  return (
    <div className="bg-[#F2EFFE]/80 border border-[#E6E1FF] rounded-2xl sm:rounded-3xl p-5 shadow-xs">
      {/* Header */}
      <div className="flex items-center gap-2 mb-1">
        <Calendar className="w-4 h-4 text-[#CC7D52]" />
        <span className="text-xs font-bold text-[#CC7D52] uppercase tracking-wider">
          SAMPLE SCHEDULE
        </span>
      </div>

      {/* Hours / week summary */}
      <p className="text-xs sm:text-sm font-bold text-[#1A1F36] mb-4">
        {currentSchedule.label}
      </p>

      {/* Day by day list */}
      <div className="space-y-2.5">
        <AnimatePresence mode="wait">
          <motion.div
            key={hoursId}
            initial={{ opacity: 0, y: 4 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -4 }}
            transition={{ duration: 0.2 }}
            className="space-y-2"
          >
            {currentSchedule.items.map((item, idx) => (
              <div
                key={idx}
                className="flex items-center justify-between text-xs text-gray-700"
              >
                {/* Left: Dot + Day */}
                <div className="flex items-center gap-2">
                  <span className={`w-2 h-2 rounded-full ${item.color}`} />
                  <span className="font-semibold text-[#1A1F36] w-8">{item.day}</span>
                </div>

                {/* Right: Topic + Duration */}
                <div className="text-right">
                  {item.isRest ? (
                    <span className="text-gray-400 font-normal">Rest</span>
                  ) : (
                    <span>
                      <span className="font-medium text-gray-800">{item.topic}</span>
                      <span className="text-gray-400 mx-1">·</span>
                      <span className="text-gray-500 font-medium">{item.duration}</span>
                    </span>
                  )}
                </div>
              </div>
            ))}
          </motion.div>
        </AnimatePresence>
      </div>
    </div>
  );
};
