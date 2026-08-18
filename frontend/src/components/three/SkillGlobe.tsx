import { useEffect, useRef } from 'react';
import type { GapSeverity } from '../../api/types';

interface SkillNode {
  name: string;
  severity: GapSeverity;
}

export interface SkillGlobeProps {
  skills: SkillNode[];
  className?: string;
}

export function SkillGlobe({ skills, className }: SkillGlobeProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let animationFrameId: number;
    let mouseX = -1000;
    let mouseY = -1000;
    let time = 0;

    const handleMouseMove = (e: MouseEvent) => {
      const rect = canvas.getBoundingClientRect();
      mouseX = e.clientX - rect.left;
      mouseY = e.clientY - rect.top;
    };

    const handleMouseLeave = () => {
      mouseX = -1000;
      mouseY = -1000;
    };

    canvas.addEventListener('mousemove', handleMouseMove);
    canvas.addEventListener('mouseleave', handleMouseLeave);

    const resize = () => {
      const parent = canvas.parentElement;
      if (parent) {
        canvas.width = parent.clientWidth;
        canvas.height = parent.clientHeight;
      }
    };

    const getSeverityDetails = (severity: string) => {
      switch (severity?.toUpperCase()) {
        case 'CRITICAL': return { radius: 20, opacity: 1.0 };
        case 'HIGH': return { radius: 16, opacity: 0.7 };
        case 'MEDIUM': return { radius: 12, opacity: 0.5 };
        case 'LOW': return { radius: 8, opacity: 0.3 };
        default: return { radius: 12, opacity: 0.5 };
      }
    };

    const draw = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      const centerX = canvas.width / 2;
      const centerY = canvas.height / 2;
      const maxRadius = Math.min(centerX, centerY) - 40;

      time += 0.02;

      ctx.beginPath();
      ctx.arc(centerX, centerY, 24 + Math.sin(time) * 2, 0, Math.PI * 2);
      ctx.fillStyle = '#A1A1AA';
      ctx.fill();

      let hoveredSkill: SkillNode | null = null;
      let hoveredX = 0;
      let hoveredY = 0;

      skills?.forEach((skill, i) => {
        const angle = (i / skills.length) * Math.PI * 2 + time * 0.1;
        const distance = maxRadius * 0.7; 
        const x = centerX + Math.cos(angle) * distance;
        const y = centerY + Math.sin(angle) * distance;

        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        ctx.lineTo(x, y);
        ctx.strokeStyle = 'rgba(148, 163, 184, 0.2)';
        ctx.lineWidth = 1;
        ctx.stroke();

        const { radius: baseRadius, opacity } = getSeverityDetails(skill.severity);
        const radius = baseRadius + Math.sin(time + i) * 1.5;

        ctx.beginPath();
        ctx.arc(x, y, radius, 0, Math.PI * 2);
        // Using the accent color for the skill nodes
        ctx.fillStyle = `rgba(56, 189, 248, ${opacity})`;
        ctx.fill();

        const dx = mouseX - x;
        const dy = mouseY - y;
        if (Math.sqrt(dx * dx + dy * dy) < radius + 5) {
          hoveredSkill = skill;
          hoveredX = x;
          hoveredY = y;
        }
      });

      if (hoveredSkill) {
        ctx.font = '14px sans-serif';
        const text = (hoveredSkill as any).name || (hoveredSkill as any).id || 'Skill';
        const metrics = ctx.measureText(text);
        const padding = 6;
        
        ctx.fillStyle = 'rgba(30, 41, 59, 0.9)';
        ctx.fillRect(hoveredX + 15, hoveredY - 15 - 14, metrics.width + padding * 2, 20);
        ctx.strokeStyle = '#38BDF8';
        ctx.strokeRect(hoveredX + 15, hoveredY - 15 - 14, metrics.width + padding * 2, 20);
        
        ctx.fillStyle = '#FFFFFF';
        ctx.fillText(text, hoveredX + 15 + padding, hoveredY - 15 - 2);
      }

      animationFrameId = requestAnimationFrame(draw);
    };

    const observer = new ResizeObserver(() => resize());
    if (canvas.parentElement) observer.observe(canvas.parentElement);
    
    resize();
    draw();

    return () => {
      cancelAnimationFrame(animationFrameId);
      observer.disconnect();
      canvas.removeEventListener('mousemove', handleMouseMove);
      canvas.removeEventListener('mouseleave', handleMouseLeave);
    };
  }, [skills]);

  return (
    <canvas 
      ref={canvasRef} 
      className={`block w-full h-full ${className || ''}`}
      style={{ background: 'transparent' }}
    />
  );
}
