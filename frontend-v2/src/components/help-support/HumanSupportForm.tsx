import React, { useState, useRef } from 'react';
import { Paperclip, X, Loader2 } from 'lucide-react';

interface HumanSupportFormProps {
  onSubmitRequest: (subject: string, category: string) => void;
}

export const HumanSupportForm: React.FC<HumanSupportFormProps> = ({
  onSubmitRequest,
}) => {
  const [category, setCategory] = useState('');
  const [subject, setSubject] = useState('');
  const [description, setDescription] = useState('');
  const [attachment, setAttachment] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      if (file.size > 10 * 1024 * 1024) {
        setError('File size exceeds 10MB limit');
        return;
      }
      setError(null);
      setAttachment(file.name);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!category || category === 'Select Category...') {
      setError('Please select a category');
      return;
    }
    if (!subject.trim()) {
      setError('Subject is required');
      return;
    }
    if (!description.trim()) {
      setError('Description is required');
      return;
    }

    setError(null);
    setLoading(true);

    setTimeout(() => {
      setLoading(false);
      onSubmitRequest(subject.trim(), category);
      setCategory('');
      setSubject('');
      setDescription('');
      setAttachment(null);
    }, 600);
  };

  return (
    <div className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-5 select-none">
      <h3 className="text-base font-extrabold text-[#0f1b32] tracking-tight">
        Contact Human Support
      </h3>

      <form onSubmit={handleSubmit} className="space-y-4 text-xs font-semibold">
        {error && (
          <p className="text-[11px] text-red-600 bg-red-50 p-2.5 rounded-xl border border-red-100 font-medium">
            {error}
          </p>
        )}

        {/* Category Select */}
        <select
          value={category}
          onChange={(e) => setCategory(e.target.value)}
          className="w-full px-3.5 py-2.5 rounded-2xl bg-white/90 border border-gray-200/80 text-xs font-semibold text-[#0f1b32] focus:outline-none focus:ring-2 focus:ring-[#8e4d2b]/20 focus:border-[#8e4d2b] shadow-2xs"
        >
          <option value="">Select Category...</option>
          <option value="Technical Issue">Technical Issue</option>
          <option value="Billing">Billing</option>
          <option value="Account Management">Account Management</option>
        </select>

        {/* Subject Input */}
        <input
          type="text"
          value={subject}
          onChange={(e) => setSubject(e.target.value)}
          placeholder="Subject"
          className="w-full px-3.5 py-2.5 rounded-2xl bg-white/90 border border-gray-200/80 text-xs font-semibold text-[#0f1b32] placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-[#8e4d2b]/20 focus:border-[#8e4d2b] shadow-2xs"
        />

        {/* Description Textarea */}
        <textarea
          rows={3}
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="Describe your issue..."
          className="w-full px-3.5 py-2.5 rounded-2xl bg-white/90 border border-gray-200/80 text-xs font-semibold text-[#0f1b32] placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-[#8e4d2b]/20 focus:border-[#8e4d2b] resize-none shadow-2xs"
        />

        {/* Attachment & Submit Row */}
        <div className="flex items-center justify-between gap-2 pt-1">
          {/* File Picker Control */}
          <div className="flex items-center gap-2">
            <input
              type="file"
              ref={fileInputRef}
              onChange={handleFileChange}
              className="hidden"
            />
            {attachment ? (
              <div className="flex items-center gap-1 px-2.5 py-1 rounded-lg bg-[#FAF4F0] border border-[#F2DACB] text-[11px] text-[#8e4d2b] font-bold">
                <Paperclip className="w-3 h-3" />
                <span className="max-w-[100px] truncate">{attachment}</span>
                <button
                  type="button"
                  onClick={() => setAttachment(null)}
                  className="p-0.5 hover:text-red-600 ml-0.5"
                >
                  <X className="w-3 h-3" />
                </button>
              </div>
            ) : (
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                className="inline-flex items-center gap-1.5 text-xs text-gray-500 hover:text-[#8e4d2b] transition-colors cursor-pointer"
              >
                <Paperclip className="w-3.5 h-3.5" />
                <span>Attach file</span>
              </button>
            )}
          </div>

          {/* Send Request Button */}
          <button
            type="submit"
            disabled={loading}
            className="py-2.5 px-4 rounded-xl bg-white/90 hover:bg-[#FAF4F0] border border-gray-200 text-xs font-bold text-[#0f1b32] hover:text-[#8e4d2b] transition-all shadow-2xs cursor-pointer active:scale-95 disabled:opacity-50"
          >
            {loading ? (
              <div className="flex items-center gap-1.5">
                <Loader2 className="w-3.5 h-3.5 animate-spin" />
                <span>Sending...</span>
              </div>
            ) : (
              <span>Send Request</span>
            )}
          </button>
        </div>
      </form>
    </div>
  );
};
