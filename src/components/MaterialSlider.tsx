import React, { useRef, useState, useCallback, useEffect } from 'react';
import { Minus, Plus } from 'lucide-react';
import { ThemePalette } from '../types';

interface MaterialSliderProps {
  value: number;
  min: number;
  max: number;
  step?: number;
  unit?: string;
  prefix?: string;
  label: string;
  sublabel?: string;
  theme: ThemePalette;
  isBoostMode?: boolean;
  disabled?: boolean;
  onChange: (val: number) => void;
  ticks?: number[];
  onDangerousThreshold?: () => void;
}

export const MaterialSlider: React.FC<MaterialSliderProps> = ({
  value,
  min,
  max,
  step = 1,
  unit = '%',
  prefix = '',
  label,
  sublabel,
  theme,
  isBoostMode = false,
  disabled = false,
  onChange,
  ticks = [0, 25, 50, 75, 100],
  onDangerousThreshold,
}) => {
  const trackRef = useRef<HTMLDivElement>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [hovered, setHovered] = useState(false);

  // Compute percentage 0-100% of the slider range
  const percent = Math.max(0, Math.min(100, ((value - min) / (max - min)) * 100));

  // Determine color state based on boost level
  let trackColor = theme.primary;
  let statusBadge = 'Normal';
  let statusBadgeColor = 'text-slate-400 bg-slate-800/80';

  if (isBoostMode) {
    if (value === 0) {
      trackColor = theme.outline;
      statusBadge = 'Off';
      statusBadgeColor = 'text-slate-400 bg-slate-800/70';
    } else if (value <= 30) {
      trackColor = theme.primary;
      statusBadge = 'Safe Boost';
      statusBadgeColor = 'text-emerald-300 bg-emerald-950/80 border border-emerald-800/60';
    } else if (value <= 70) {
      trackColor = '#f59e0b'; // Amber warning
      statusBadge = 'High Volume';
      statusBadgeColor = 'text-amber-300 bg-amber-950/80 border border-amber-800/60';
    } else {
      trackColor = '#ef4444'; // Red danger
      statusBadge = 'Max Boost';
      statusBadgeColor = 'text-red-300 bg-red-950/80 border border-red-800/60';
    }
  }

  const handlePointer = useCallback(
    (clientX: number) => {
      if (!trackRef.current || disabled) return;
      const rect = trackRef.current.getBoundingClientRect();
      const relativeX = clientX - rect.left;
      const ratio = Math.max(0, Math.min(1, relativeX / rect.width));
      let rawVal = min + ratio * (max - min);

      // Snap to step
      if (step > 0) {
        rawVal = Math.round(rawVal / step) * step;
      }
      const clamped = Math.max(min, Math.min(max, rawVal));

      if (clamped !== value) {
        onChange(clamped);
        if (navigator.vibrate) {
          navigator.vibrate(8);
        }
        if (isBoostMode && clamped >= 70 && value < 70 && onDangerousThreshold) {
          onDangerousThreshold();
        }
      }
    },
    [min, max, step, value, onChange, disabled, isBoostMode, onDangerousThreshold]
  );

  const handleMouseDown = (e: React.MouseEvent) => {
    if (disabled) return;
    setIsDragging(true);
    handlePointer(e.clientX);
  };

  const handleTouchStart = (e: React.TouchEvent) => {
    if (disabled) return;
    setIsDragging(true);
    handlePointer(e.touches[0].clientX);
  };

  useEffect(() => {
    const handleMove = (e: MouseEvent) => {
      if (isDragging) {
        handlePointer(e.clientX);
      }
    };
    const handleTouchMove = (e: TouchEvent) => {
      if (isDragging && e.touches[0]) {
        handlePointer(e.touches[0].clientX);
      }
    };
    const handleEnd = () => {
      setIsDragging(false);
    };

    if (isDragging) {
      window.addEventListener('mousemove', handleMove);
      window.addEventListener('mouseup', handleEnd);
      window.addEventListener('touchmove', handleTouchMove);
      window.addEventListener('touchend', handleEnd);
    }

    return () => {
      window.removeEventListener('mousemove', handleMove);
      window.removeEventListener('mouseup', handleEnd);
      window.removeEventListener('touchmove', handleTouchMove);
      window.removeEventListener('touchend', handleEnd);
    };
  }, [isDragging, handlePointer]);

  const increment = () => {
    const next = Math.min(max, value + (step || 5));
    onChange(next);
    if (navigator.vibrate) navigator.vibrate(10);
  };

  const decrement = () => {
    const prev = Math.max(min, value - (step || 5));
    onChange(prev);
    if (navigator.vibrate) navigator.vibrate(10);
  };

  return (
    <div
      className={`w-full p-4 rounded-3xl transition-all ${
        disabled ? 'opacity-40 pointer-events-none' : ''
      }`}
      style={{
        backgroundColor: theme.surfaceContainer,
      }}
    >
      {/* Header Info */}
      <div className="flex items-center justify-between mb-3">
        <div className="space-y-0.5">
          <div className="flex items-center gap-2">
            <span className="text-base font-semibold text-slate-100 tracking-tight">
              {label}
            </span>
            {isBoostMode && (
              <span className={`text-[11px] font-medium px-2 py-0.5 rounded-full ${statusBadgeColor}`}>
                {statusBadge}
              </span>
            )}
          </div>
          {sublabel && (
            <p className="text-xs text-slate-400 font-normal leading-relaxed">
              {sublabel}
            </p>
          )}
        </div>

        {/* Big Tabular Percentage Value Pill */}
        <div
          className="flex items-baseline gap-0.5 px-3.5 py-1.5 rounded-2xl border transition-colors shadow-sm"
          style={{
            backgroundColor: `${trackColor}18`,
            borderColor: `${trackColor}40`,
          }}
        >
          <span
            className="text-2xl font-bold tracking-tight font-mono tabular-nums"
            style={{ color: trackColor }}
          >
            {prefix}
            {value}
          </span>
          <span className="text-xs font-semibold" style={{ color: trackColor }}>
            {unit}
          </span>
        </div>
      </div>

      {/* Expressive M3 Track Slider */}
      <div className="relative py-2 select-none">
        <div
          ref={trackRef}
          onMouseDown={handleMouseDown}
          onTouchStart={handleTouchStart}
          onMouseEnter={() => setHovered(true)}
          onMouseLeave={() => setHovered(false)}
          className="relative h-14 w-full rounded-2xl bg-slate-900/90 border border-slate-800/80 cursor-pointer overflow-hidden p-1 flex items-center shadow-inner group"
          role="slider"
          aria-valuemin={min}
          aria-valuemax={max}
          aria-valuenow={value}
          aria-label={label}
        >
          {/* Active Filled Track Bar */}
          <div
            className="absolute left-1 top-1 bottom-1 rounded-xl transition-[width] duration-75 ease-out shadow-md"
            style={{
              width: `calc(${percent}% - 8px)`,
              backgroundColor: trackColor,
              opacity: 0.92,
            }}
          />

          {/* Background Grid Ticks */}
          <div className="absolute inset-x-4 inset-y-0 pointer-events-none flex items-center justify-between opacity-30">
            {ticks.map((tick) => {
              const tickPct = ((tick - min) / (max - min)) * 100;
              const isPast = percent >= tickPct;
              return (
                <div
                  key={tick}
                  className={`w-1 h-3 rounded-full transition-colors ${
                    isPast ? 'bg-slate-950' : 'bg-slate-400'
                  }`}
                  style={{ left: `${tickPct}%` }}
                />
              );
            })}
          </div>

          {/* Interactive Thumb Pill with Indicator */}
          <div
            className="absolute top-1.5 bottom-1.5 w-6 rounded-xl bg-white shadow-xl flex items-center justify-center pointer-events-none transition-transform"
            style={{
              left: `calc(${percent}% - ${percent > 90 ? '22px' : percent < 10 ? '2px' : '12px'})`,
              transform: isDragging ? 'scale(1.08)' : hovered ? 'scale(1.03)' : 'scale(1)',
              boxShadow: `0 4px 16px ${trackColor}60`,
            }}
          >
            {/* Tactile Grip Lines */}
            <div className="w-1 h-4 rounded-full bg-slate-400" />
          </div>
        </div>

        {/* Floating Tick Labels */}
        <div className="flex justify-between items-center px-2 mt-2">
          {ticks.map((tick) => (
            <button
              key={tick}
              type="button"
              onClick={() => onChange(tick)}
              className={`text-[11px] font-mono tabular-nums transition-colors px-1 py-0.5 rounded hover:bg-slate-800/80 ${
                value === tick
                  ? 'font-bold text-white'
                  : 'text-slate-500 hover:text-slate-300'
              }`}
            >
              {prefix}
              {tick}
              {unit}
            </button>
          ))}
        </div>
      </div>

      {/* Tactile Fine-Tuning Step Controls */}
      <div className="flex items-center justify-between pt-2 border-t border-slate-800/60 mt-1">
        <div className="text-xs text-slate-400 flex items-center gap-1.5">
          <span>Fine-tune</span>
        </div>
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={decrement}
            className="w-9 h-9 rounded-xl bg-slate-800/90 hover:bg-slate-700 active:scale-95 text-slate-200 flex items-center justify-center transition-all border border-slate-700/60 shadow-sm"
            aria-label="Decrease"
          >
            <Minus size={16} />
          </button>
          <button
            type="button"
            onClick={increment}
            className="w-9 h-9 rounded-xl bg-slate-800/90 hover:bg-slate-700 active:scale-95 text-slate-200 flex items-center justify-center transition-all border border-slate-700/60 shadow-sm"
            aria-label="Increase"
          >
            <Plus size={16} />
          </button>
        </div>
      </div>
    </div>
  );
};
