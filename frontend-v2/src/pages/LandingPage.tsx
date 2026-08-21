import React from 'react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { Header } from '../components/layout/Header';
import { Hero } from '../components/landing/Hero';
import { Footer } from '../components/layout/Footer';

export const LandingPage: React.FC = () => {
  return (
    <div className="relative min-h-screen flex flex-col justify-between overflow-x-hidden selection:bg-[#FFB091]/30 selection:text-[#9C5B33]">
      {/* Soft Atmospheric Lighting */}
      <AmbientBackground />

      {/* Floating Pill Dynamic Navbar */}
      <Header />

      {/* Hero Section with Live Dashboard Preview */}
      <main className="flex-1 flex flex-col justify-center">
        <Hero />
      </main>

      {/* Minimalist Editorial Footer */}
      <Footer />
    </div>
  );
};
