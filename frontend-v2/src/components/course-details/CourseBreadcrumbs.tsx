import React from 'react';
import { Link } from 'react-router-dom';
import { ChevronRight } from 'lucide-react';

export const CourseBreadcrumbs: React.FC = () => {
  return (
    <nav aria-label="Breadcrumb" className="flex items-center gap-2 text-xs text-gray-500 font-medium select-none mb-6">
      <Link
        to="/explore-courses"
        className="hover:text-[#8e4d2b] transition-colors cursor-pointer"
      >
        Explore Courses
      </Link>
      <ChevronRight className="w-3.5 h-3.5 text-gray-400 shrink-0" />
      <span className="text-[#0f1b32] font-semibold">
        Course Details
      </span>
    </nav>
  );
};
