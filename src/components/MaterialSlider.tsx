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
  ticks = [0, 20, 40, 60, 80, 100],
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
  let statusBadge = 'Normal';
  let statusBadgeStyle = isDark
    ? 'text-slate-400 bg-slate-800/80 border-slate-700/60'
    : 'text-slate-600 bg-slate-100 border-slate-300';

  if (isBoostMode) {
    if (value === 0) {
      trackColor = isDark ? '#64748b' : '#94a3b8';
      statusBadge = 'Off';
      statusBadgeStyle = isDark
        ? 'text-slate-400 bg-slate-800/70 border-slate-700/60'
        : 'text-slate-500 bg-slate-100 border-slate-300';
    } else if (value <= 30) {
      trackColor = '#10b981';
      statusBadge = 'Safe Boost';
      statusBadgeStyle = isDark
        ? 'text-emerald-300 bg-emerald-950/80 border-emerald-800/60'
        : 'text-emerald-700 bg-emerald-50 border-emerald-300';
    } else if (value <= 60) {
      trackColor = '#f59e0b';
      statusBadge = 'High Volume';
      statusBadgeStyle = isDark
        ? 'text-amber-300 bg-amber-950/80 border-amber-800/60'
        : 'text-amber-700 bg-amber-50 border-amber-300';
    } else {
      trackColor = '#ef4444';
      statusBadge = 'Max Boost';
      statusBadgeStyle = isDark
        ? 'text-red-300 bg-red-950/80 border-red-800/60'
        : 'text-red-700 bg-red-50 border-red-300';
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
  const textTitle = isDark ? '#E3E2E6' : '#0F172A';
  const textSub = isDark ? '#938F99' : '#64748B';

  return (
    <div
      className={`w-full p-5 rounded-3xl transition-all ${
        isDisabled ? 'opacity-40 pointer-events-none' : ''
      }`}
      style={{
        backgroundColor: cardBg,
        border: `1px solid ${cardBorder}`,
        boxShadow: isDark ? 'none' : '0 1px 3px 0 rgba(0, 0, 0, 0.05)',
      }}
    >
      {/* Header Info: Label, Status Pill, and Value Pill */}
      <div className="flex items-center justify-between mb-1">
        <div className="flex items-center gap-2 min-w-0 pr-2">
          <span
            className="text-base font-bold tracking-tight whitespace-nowrap"
            style={{ color: textTitle }}
          >
            {label}
          </span>
          {isBoostMode && (
            <span
              className={`text-[11px] font-semibold px-2 py-0.5 rounded-md border whitespace-nowrap ${statusBadgeStyle}`}
            >
              {statusBadge}
            </span>
          )}
        </div>

        {/* Tabular Value Display */}
        <div
          className="px-2.5 py-1 rounded-xl flex items-baseline font-mono border"
          style={{
            backgroundColor: `${trackColor}18`,
            borderColor: `${trackColor}40`,
            color: trackColor,
          }}
        >
          <span className="text-lg font-bold">
            {prefix}
            {value}
          </span>
          <span className="text-xs font-bold ml-0.5">{unit}</span>
        </div>
      </div>

      <p className="text-xs mb-5 line-clamp-1" style={{ color: textSub }}>
        {sublabel}
      </p>

      {/* Sleek Line Slider Track */}
      <div className="relative py-2 select-none">
        <div
          ref={trackRef}
          onMouseDown={handleMouseDown}
          onTouchStart={handleTouchStart}
          className="relative w-full h-8 flex items-center cursor-pointer touch-none"
        >
          {/* Inactive Track Line */}
          <div
            className="w-full h-1.5 rounded-full"
            style={{ backgroundColor: trackInactive }}
          />

          {/* Active Filled Line */}
          <div
            className="absolute left-0 h-1.5 rounded-full transition-all duration-75"
            style={{
              width: `${percent}%`,
              backgroundColor: trackColor,
            }}
          />

          {/* Smooth Circular Elevated Thumb */}
          <div
            className="absolute top-1/2 -translate-y-1/2 w-5 h-5 rounded-full bg-white shadow-md cursor-grab active:cursor-grabbing active:scale-110 transition-transform"
            style={{
              left: `calc(${percent}% - 10px)`,
              border: `3px solid ${trackColor}`,
            }}
          />
        </div>

        {/* Quick Presets (e.g. 0%, 20%, 40%, 60%, 80%, 100%) */}
        {isBoostMode && ticks.length > 0 && (
          <div className="grid grid-cols-6 gap-1.5 mt-2">
            {ticks.map((tick) => {
              const isSelected = value === tick;
              const chipInactiveBg = isDark ? '#1E212B' : '#F1F5F9';
              const chipInactiveBorder = isDark ? '#2C3242' : '#CBD5E1';
              const chipInactiveText = isDark ? '#938F99' : '#64748B';

              return (
                <button
                  key={tick}
                  type="button"
                  onClick={() => onChange(tick)}
                  className="py-1.5 rounded-lg text-xs font-mono font-medium transition-all border"
                  style={{
                    backgroundColor: isSelected ? `${trackColor}25` : chipInactiveBg,
                    borderColor: isSelected ? trackColor : chipInactiveBorder,
                    color: isSelected ? trackColor : chipInactiveText,
                    fontWeight: isSelected ? '700' : '500',
                  }}
                >
                  {prefix}
                  {tick}%
                </button>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};
