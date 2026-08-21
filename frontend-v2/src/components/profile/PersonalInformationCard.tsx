import React, { useState, useEffect } from 'react';
import { Pencil, Check } from 'lucide-react';

export interface PersonalInfoData {
  fullName: string;
  email: string;
  location: string;
  education: string;
  graduationYear: string;
}

interface PersonalInformationCardProps {
  initialData?: Partial<PersonalInfoData>;
  onSave?: (data: PersonalInfoData) => void;
}

export const PersonalInformationCard: React.FC<PersonalInformationCardProps> = ({ initialData, onSave }) => {
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState<PersonalInfoData>({
    fullName: initialData?.fullName || 'Parth Rathi',
    email: initialData?.email || 'parth.rathi@example.com',
    location: initialData?.location || 'San Francisco, CA',
    education: initialData?.education || 'B.Tech Computer Science',
    graduationYear: initialData?.graduationYear || '2027',
  });

  useEffect(() => {
    if (initialData) {
      setFormData((prev) => ({
        fullName: initialData.fullName ?? prev.fullName,
        email: initialData.email ?? prev.email,
        location: initialData.location ?? prev.location,
        education: initialData.education ?? prev.education,
        graduationYear: initialData.graduationYear ?? prev.graduationYear,
      }));
    }
  }, [initialData]);

  const handleToggle = () => {
    if (isEditing) {
      onSave?.(formData);
    }
    setIsEditing(!isEditing);
  };

  return (
    <section
      aria-label="Personal Information"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-5"
    >
      {/* Header */}
      <div className="flex items-center justify-between">
        <h3 className="text-base sm:text-lg font-extrabold text-[#0f1b32] tracking-tight">
          Personal Information
        </h3>

        <button
          type="button"
          onClick={handleToggle}
          className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-xl bg-[#FAF4F0] hover:bg-[#F2DACB]/60 text-[#8e4d2b] font-bold text-xs border border-[#F2DACB] transition-colors cursor-pointer shadow-2xs"
        >
          {isEditing ? (
            <>
              <Check className="w-3.5 h-3.5" />
              <span>Save</span>
            </>
          ) : (
            <>
              <Pencil className="w-3.5 h-3.5" />
              <span>Edit</span>
            </>
          )}
        </button>
      </div>

      {/* Form Fields Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 sm:gap-5 text-left">
        {/* Full Name */}
        <div className="space-y-1.5">
          <label className="text-[11px] font-bold text-gray-400 uppercase tracking-wider block">
            Full Name
          </label>
          <input
            type="text"
            disabled={!isEditing}
            value={formData.fullName}
            onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
            className="w-full px-3.5 py-2.5 rounded-2xl bg-white/90 border border-gray-200/80 text-xs sm:text-sm font-semibold text-[#0f1b32] disabled:bg-white/50 disabled:text-[#0f1b32] focus:outline-none focus:ring-2 focus:ring-[#8e4d2b]/20 focus:border-[#8e4d2b] transition-all shadow-2xs"
          />
        </div>

        {/* Email Address */}
        <div className="space-y-1.5">
          <label className="text-[11px] font-bold text-gray-400 uppercase tracking-wider block">
            Email Address
          </label>
          <input
            type="email"
            disabled={true}
            value={formData.email}
            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
            className="w-full px-3.5 py-2.5 rounded-2xl bg-white/50 border border-gray-200/80 text-xs sm:text-sm font-semibold text-gray-500 cursor-not-allowed shadow-2xs"
          />
        </div>

        {/* Location */}
        <div className="space-y-1.5">
          <label className="text-[11px] font-bold text-gray-400 uppercase tracking-wider block">
            Location
          </label>
          <input
            type="text"
            disabled={!isEditing}
            value={formData.location}
            onChange={(e) => setFormData({ ...formData, location: e.target.value })}
            className="w-full px-3.5 py-2.5 rounded-2xl bg-white/90 border border-gray-200/80 text-xs sm:text-sm font-semibold text-[#0f1b32] disabled:bg-white/50 disabled:text-[#0f1b32] focus:outline-none focus:ring-2 focus:ring-[#8e4d2b]/20 focus:border-[#8e4d2b] transition-all shadow-2xs"
          />
        </div>

        {/* Education */}
        <div className="space-y-1.5">
          <label className="text-[11px] font-bold text-gray-400 uppercase tracking-wider block">
            Education
          </label>
          <input
            type="text"
            disabled={!isEditing}
            value={formData.education}
            onChange={(e) => setFormData({ ...formData, education: e.target.value })}
            className="w-full px-3.5 py-2.5 rounded-2xl bg-white/90 border border-gray-200/80 text-xs sm:text-sm font-semibold text-[#0f1b32] disabled:bg-white/50 disabled:text-[#0f1b32] focus:outline-none focus:ring-2 focus:ring-[#8e4d2b]/20 focus:border-[#8e4d2b] transition-all shadow-2xs"
          />
        </div>

        {/* Graduation Year */}
        <div className="space-y-1.5">
          <label className="text-[11px] font-bold text-gray-400 uppercase tracking-wider block">
            Graduation Year
          </label>
          <input
            type="text"
            disabled={!isEditing}
            value={formData.graduationYear}
            onChange={(e) => setFormData({ ...formData, graduationYear: e.target.value })}
            className="w-full px-3.5 py-2.5 rounded-2xl bg-white/90 border border-gray-200/80 text-xs sm:text-sm font-semibold text-[#0f1b32] disabled:bg-white/50 disabled:text-[#0f1b32] focus:outline-none focus:ring-2 focus:ring-[#8e4d2b]/20 focus:border-[#8e4d2b] transition-all shadow-2xs"
          />
        </div>
      </div>
    </section>
  );
};
