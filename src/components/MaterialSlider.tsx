import React, { useRef, useState, useCallback, useEffect } from 'react';

interface MaterialSliderProps {
  value: number;
  min: number;
  max: number;
  step?: number;
  unit?: string;
  prefix?: string;
  label: string;
  sublabel?: string;
  isBoostMode?: boolean;
  disabled?: boolean;
  enabled?: boolean;
  isDark?: boolean;
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
  isBoostMode = false,
  disabled = false,
  enabled = true,
  isDark = true,
  onChange,
  ticks = [],
  onDangerousThreshold,
}) => {
  const isDisabled = disabled || !enabled;
  const trackRef = useRef<HTMLDivElement>(null);
  const [isDragging, setIsDragging] = useState(false);

  // Compute percentage 0-100% of the slider range
  const percent = Math.max(0, Math.min(100, ((value - min) / (max - min)) * 100));

  // Dynamic theme colors
  const primaryColor = isDark ? '#D0BCFF' : '#6750A4';
  let trackColor = primaryColor;

  if (isBoostMode) {
    if (value === 0) {
      trackColor = isDark ? '#64748B' : '#94A3B8';
    } else if (value <= 60) {
      trackColor = '#10B981'; // EmeraldSafe
    } else {
      trackColor = '#EF4444'; // RedDanger
    }
  }

  const handlePointer = useCallback(
    (clientX: number) => {
      if (!trackRef.current || isDisabled) return;
      const rect = trackRef.current.getBoundingClientRect();
      const relativeX = clientX - rect.left;
      const ratio = Math.max(0, Math.min(1, relativeX / rect.width));
      let rawVal = min + ratio * (max - min);

      if (step > 0) {
        rawVal = Math.round(rawVal / step) * step;
      }
      const clamped = Math.max(min, Math.min(max, rawVal));

      if (clamped !== value) {
        onChange(clamped);
        if (isBoostMode && clamped >= 70 && value < 70 && onDangerousThreshold) {
          onDangerousThreshold();
        }
      }
    },
    [min, max, step, value, onChange, isDisabled, isBoostMode, onDangerousThreshold]
  );

  const handleMouseDown = (e: React.MouseEvent) => {
    if (isDisabled) return;
    setIsDragging(true);
    handlePointer(e.clientX);
  };

  const handleTouchStart = (e: React.TouchEvent) => {
    if (isDisabled) return;
    setIsDragging(true);
    handlePointer(e.touches[0].clientX);
  };

  useEffect(() => {
    const handleMove = (e: MouseEvent) => {
      if (isDragging) handlePointer(e.clientX);
    };
    const handleTouchMove = (e: TouchEvent) => {
      if (isDragging && e.touches[0]) handlePointer(e.touches[0].clientX);
    };
    const handleEnd = () => setIsDragging(false);

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

  const cardBg = isDark ? '#211F26' : '#FFFFFF';
  const cardBorder = isDark ? '#33303D' : '#E2E8F0';
  const trackInactive = isDark ? '#262A36' : '#E2E8F0';
  const textTitle = isDark ? '#E6E1E5' : '#0F172A';
  const textSub = isDark ? '#938F99' : '#64748B';

  const fraction = (percent / 100);

  return (
    <div
      className={`w-full px-4 py-3.5 rounded-2xl transition-all ${
        isDisabled ? 'opacity-40 pointer-events-none' : ''
      }`}
      style={{
        backgroundColor: cardBg,
        border: `1px solid ${cardBorder}`,
        boxShadow: isDark ? 'none' : '0 1px 3px 0 rgba(0, 0, 0, 0.05)',
      }}
    >
      {/* Header Info: Label and Value Pill */}
      <div className={`flex items-center justify-between ${sublabel ? 'mb-0.5' : 'mb-2'}`}>
        <div className="flex items-center gap-2 min-w-0 pr-2">
          <span
            className="text-sm font-semibold tracking-tight whitespace-nowrap"
            style={{ color: textTitle }}
          >
            {label}
          </span>
        </div>

        {/* Fixed-dimension Stable Value Display (56px x 24px) */}
        <div
          className="w-14 h-6 px-1 rounded-lg flex items-center justify-center font-mono border tabular-nums shrink-0 transition-colors duration-150"
          style={{
            backgroundColor: isBoostMode && value > 60 ? (isDark ? 'rgba(239, 68, 68, 0.15)' : '#fee2e2') : `${trackColor}26`,
            borderColor: isBoostMode && value > 60 ? 'rgba(239, 68, 68, 0.4)' : `${trackColor}40`,
            color: isBoostMode && value > 60 ? '#EF4444' : trackColor,
          }}
        >
          <span className="text-sm font-bold text-center inline-flex items-baseline">
            {prefix}
            {value}
            <span className="text-[10px] font-bold ml-0.5">{unit}</span>
          </span>
        </div>
      </div>

      {sublabel && (
        <p className="text-[11px] mb-2 leading-tight line-clamp-1" style={{ color: textSub }}>
          {sublabel}
        </p>
      )}

      {/* Modern Line Slider Track & Constrained Round Thumb */}
      <div className="relative py-1 select-none">
        <div
          ref={trackRef}
          onMouseDown={handleMouseDown}
          onTouchStart={handleTouchStart}
          className="relative w-full h-7 flex items-center cursor-pointer touch-none"
        >
          {/* Inactive Background Track Line (6px height) */}
          <div
            className="w-full h-1.5 rounded-full"
            style={{ backgroundColor: trackInactive }}
          />

          {/* Active Filled Track Line */}
          <div
            className="absolute left-0 h-1.5 rounded-full transition-all duration-75"
            style={{
              width: `${percent}%`,
              backgroundColor: isBoostMode && value > 60 ? '#EF4444' : trackColor,
            }}
          />

          {/* Smooth Circular Elevated Thumb (18px diameter, perfectly bounded inside track) */}
          <div
            className="absolute top-1/2 -translate-y-1/2 w-[18px] h-[18px] rounded-full bg-white cursor-grab active:cursor-grabbing active:scale-110 transition-transform"
            style={{
              left: `calc(${fraction} * (100% - 18px))`,
              border: `2.5px solid ${isBoostMode && value > 60 ? '#EF4444' : trackColor}`,
              boxShadow: '0 2px 4px 0 rgba(0, 0, 0, 0.35)',
            }}
          />
        </div>
      </div>
    </div>
  );
};
