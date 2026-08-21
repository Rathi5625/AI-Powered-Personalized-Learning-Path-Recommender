import React from 'react';
import { Network, User, Shield, Layers, Database } from 'lucide-react';
import { motion } from 'framer-motion';

export const ArchitectureDiagram: React.FC = () => {
  return (
    <div className="relative w-full h-[260px] sm:h-[300px] rounded-2xl bg-[#FAF4F0]/60 border border-[#F2DACB]/60 p-4 flex flex-col justify-between overflow-hidden select-none">
      {/* Background Dot Grid Pattern */}
      <div
        className="absolute inset-0 opacity-25 pointer-events-none"
        style={{
          backgroundImage: 'radial-gradient(#8e4d2b 1px, transparent 1px)',
          backgroundSize: '16px 16px',
        }}
      />

      {/* Header Label */}
      <div className="relative z-10 flex items-center gap-1.5 text-[#8e4d2b]">
        <Network className="w-3.5 h-3.5 text-[#8e4d2b]" />
        <span className="text-[11px] font-bold tracking-tight">Architecture Concept</span>
      </div>

      {/* Interactive Diagram Nodes Canvas */}
      <div className="relative z-10 flex-1 w-full flex items-center justify-center">
        {/* SVG Connection Lines */}
        <svg className="absolute inset-0 w-full h-full pointer-events-none stroke-[#d98b63]/40 stroke-2 stroke-dasharray-4">
          {/* User to Center */}
          <line x1="32%" y1="28%" x2="50%" y2="50%" strokeDasharray="3 3" />
          {/* Admin to Center */}
          <line x1="68%" y1="28%" x2="50%" y2="50%" strokeDasharray="3 3" />
          {/* Center to DB */}
          <line x1="50%" y1="50%" x2="50%" y2="78%" strokeDasharray="3 3" />
        </svg>

        {/* Top-Left: User Node */}
        <motion.div
          whileHover={{ scale: 1.08 }}
          className="absolute left-[24%] top-[16%] w-8 h-8 rounded-full bg-white/90 border border-gray-200/90 shadow-2xs flex items-center justify-center text-gray-700"
        >
          <User className="w-3.5 h-3.5" />
        </motion.div>

        {/* Top-Right: Admin/Security Node */}
        <motion.div
          whileHover={{ scale: 1.08 }}
          className="absolute right-[24%] top-[16%] w-8 h-8 rounded-full bg-white/90 border border-gray-200/90 shadow-2xs flex items-center justify-center text-gray-700"
        >
          <Shield className="w-3.5 h-3.5" />
        </motion.div>

        {/* Center: Main Spring Boot API Node */}
        <motion.div
          whileHover={{ scale: 1.05 }}
          className="relative z-20 w-14 h-14 rounded-2xl bg-gradient-to-br from-[#8e4d2b] to-[#d98b63] shadow-md shadow-[#8e4d2b]/20 flex items-center justify-center text-white"
        >
          <Layers className="w-6 h-6" />
        </motion.div>

        {/* Bottom: Database Node */}
        <motion.div
          whileHover={{ scale: 1.08 }}
          className="absolute bottom-[10%] w-8 h-8 rounded-full bg-white/90 border border-gray-200/90 shadow-2xs flex items-center justify-center text-gray-700"
        >
          <Database className="w-3.5 h-3.5" />
        </motion.div>
      </div>

      {/* Subtle Caption */}
      <div className="relative z-10 text-center">
        <span className="text-[10px] text-gray-400 font-medium">REST API + JPA + MySQL</span>
      </div>
    </div>
  );
};
