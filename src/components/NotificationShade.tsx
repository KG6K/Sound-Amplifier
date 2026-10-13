import React from 'react';
import { Zap, VolumeX, Plus, Power, Shield, Bell } from 'lucide-react';
import { ThemePalette } from '../types';

interface NotificationShadeProps {
  boostPercentage: number;
  boostActive: boolean;
  onToggleBoost: () => void;
  onQuickAdd: (amount: number) => void;
  onStopService: () => void;
  theme: ThemePalette;
}

export const NotificationShade: React.FC<NotificationShadeProps> = ({
  boostPercentage,
  boostActive,
  onToggleBoost,
  onQuickAdd,
  onStopService,
  theme,
}) => {
  return (
    <div className="w-full p-3.5 rounded-2xl bg-[#1e1d24] border border-slate-800 shadow-md transition-all">
      {/* Header */}
      <div className="flex items-center justify-between text-xs text-slate-400 mb-2">
        <div className="flex items-center gap-2">
          <div
            className="w-4 h-4 rounded-full flex items-center justify-center text-[10px]"
            style={{ backgroundColor: theme.primary, color: theme.onPrimary }}
          >
            <Zap size={10} className="fill-current" />
          </div>
          <span className="font-semibold text-slate-200">Sound Amplifier</span>
          <span>· Running in background</span>
        </div>
      </div>

      {/* Body Content */}
      <div className="flex items-center justify-between py-1 mb-2">
        <div>
          <div className="text-sm font-bold text-slate-100">
            {boostActive
              ? `Amplification Active (+${boostPercentage}%)`
              : 'Amplification Paused (0%)'}
          </div>
          <div className="text-[11px] text-slate-400">
            {boostActive
              ? 'Loudness enhancement applied to media audio'
              : 'Tap to resume background boost'}
          </div>
        </div>

        {/* Status Chip */}
        <div
          className="px-2.5 py-1 rounded-xl font-mono text-xs font-bold"
          style={{
            backgroundColor: boostActive ? `${theme.primary}25` : '#2c2b33',
            color: boostActive ? theme.primary : '#888',
          }}
        >
          {boostActive ? `+${boostPercentage}%` : 'Muted'}
        </div>
      </div>

      {/* Notification Actions Strip */}
      <div className="flex items-center gap-2 pt-2 border-t border-slate-800/80 text-xs">
        <button
          type="button"
          onClick={onToggleBoost}
          className="flex-1 py-1.5 px-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 font-medium transition-colors text-center text-[11px]"
        >
          {boostActive ? 'Pause Boost' : 'Resume Boost'}
        </button>

        <button
          type="button"
          onClick={() => onQuickAdd(20)}
          className="py-1.5 px-3 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 font-medium transition-colors flex items-center gap-1 text-[11px]"
        >
          <Plus size={12} />
          <span>+20%</span>
        </button>

        <button
          type="button"
          onClick={onStopService}
          className="py-1.5 px-2.5 rounded-xl bg-red-950/40 hover:bg-red-900/60 text-red-300 transition-colors text-[11px]"
          title="Stop Foreground Service"
        >
          <Power size={13} />
        </button>
      </div>
    </div>
  );
};
