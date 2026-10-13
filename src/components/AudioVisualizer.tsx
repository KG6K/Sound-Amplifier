import React, { useEffect, useRef } from 'react';
import { ThemePalette } from '../types';

interface AudioVisualizerProps {
  boostActive: boolean;
  theme: ThemePalette;
  boostLevel: number;
  systemVolume: number;
}

export const AudioVisualizer: React.FC<AudioVisualizerProps> = ({
  boostActive,
  theme,
  boostLevel,
  systemVolume,
}) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const animFrameRef = useRef<number | null>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let time = 0;

    const render = () => {
      animFrameRef.current = requestAnimationFrame(render);
      time += 0.05;
      const width = canvas.width;
      const height = canvas.height;

      ctx.clearRect(0, 0, width, height);

      const barCount = 32;
      const barWidth = (width / barCount) * 0.65;
      const gap = (width - barCount * barWidth) / (barCount + 1);

      // Color accent varies based on boost level
      let barFillColor = theme.primary;
      if (boostLevel > 70) {
        barFillColor = '#ef4444';
      } else if (boostLevel > 30) {
        barFillColor = '#f59e0b';
      } else if (!boostActive) {
        barFillColor = '#475569';
      }

      const effectiveVolume = boostActive ? (systemVolume / 100) * (1 + boostLevel / 100) : systemVolume / 100;

      for (let i = 0; i < barCount; i++) {
        // Multi-frequency sine harmonics
        const freqWave =
          Math.sin(time * 2 + i * 0.35) * 0.4 +
          Math.sin(time * 3.5 - i * 0.5) * 0.3 +
          Math.cos(time * 1.2 + i * 0.2) * 0.3;

        const baseAmplitude = boostActive ? (effectiveVolume * 0.75 + 0.15) : 0.06;
        const normalized = Math.max(0.04, Math.min(0.95, (freqWave + 1) * 0.5 * baseAmplitude));
        const barHeight = Math.max(4, normalized * (height - 12));

        const x = gap + i * (barWidth + gap);
        const y = height / 2 - barHeight / 2;

        ctx.fillStyle = barFillColor;
        ctx.beginPath();
        const radius = barWidth / 2;
        ctx.roundRect(x, y, barWidth, barHeight, radius);
        ctx.fill();

        // Subtle dynamic glow when actively amplified
        if (boostActive && boostLevel > 0) {
          ctx.shadowBlur = boostLevel > 70 ? 12 : 6;
          ctx.shadowColor = barFillColor;
        } else {
          ctx.shadowBlur = 0;
        }
      }
    };

    render();

    return () => {
      if (animFrameRef.current) {
        cancelAnimationFrame(animFrameRef.current);
      }
    };
  }, [boostActive, theme, boostLevel, systemVolume]);

  return (
    <div className="relative w-full h-20 bg-slate-950/70 rounded-2xl border border-slate-800/80 p-2 flex items-center justify-center overflow-hidden">
      <canvas
        ref={canvasRef}
        width={360}
        height={72}
        className="w-full h-full block"
      />
      <div className="absolute top-2 right-3 pointer-events-none flex items-center gap-1.5 text-[10px] font-mono text-slate-400">
        <span>{boostActive ? `+${boostLevel}% BOOST` : 'MUTED'}</span>
      </div>
    </div>
  );
};
