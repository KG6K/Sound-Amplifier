import React from 'react';
import { AlertTriangle, ShieldCheck, VolumeX } from 'lucide-react';
import { ThemePalette } from '../types';

interface SafetyLimiterModalProps {
  isOpen: boolean;
  onClose: () => void;
  onEnableLimiter: () => void;
  targetPercent: number;
  theme: ThemePalette;
}

export const SafetyLimiterModal: React.FC<SafetyLimiterModalProps> = ({
  isOpen,
  onClose,
  onEnableLimiter,
  targetPercent,
  theme,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="w-full max-w-sm rounded-3xl bg-[#1e1a1a] border border-amber-600/40 p-5 shadow-2xl space-y-4">
        <div className="flex items-center gap-3 text-amber-400">
          <div className="w-10 h-10 rounded-2xl bg-amber-500/20 flex items-center justify-center shrink-0">
            <AlertTriangle size={22} className="text-amber-400" />
          </div>
          <div>
            <h3 className="text-sm font-bold text-slate-100">
              High Volume Warning (+{targetPercent}%)
            </h3>
            <span className="text-[11px] text-amber-400 font-medium">
              Hearing & Speaker Safety Limit Exceeded
            </span>
          </div>
        </div>

        <p className="text-xs text-slate-300 leading-relaxed">
          Amplifying sound beyond +60% exceeds standard Android decibel thresholds (85 dB SPL). Prolonged exposure can damage hearing or cause internal speaker membrane distortion.
        </p>

        <div className="p-3 rounded-2xl bg-amber-950/30 border border-amber-800/40 flex items-center gap-2.5 text-xs text-amber-200">
          <ShieldCheck size={18} className="shrink-0 text-emerald-400" />
          <span>
            The <strong>Dynamic Safety Limiter</strong> is automatically active to prevent harsh clipping spikes.
          </span>
        </div>

        <div className="flex items-center gap-2 pt-2">
          <button
            type="button"
            onClick={() => {
              onEnableLimiter();
              onClose();
            }}
            className="flex-1 py-2.5 px-3 rounded-xl bg-amber-500 hover:bg-amber-400 text-amber-950 font-bold text-xs transition-colors text-center"
          >
            Got It, Keep Limiter On
          </button>
          <button
            type="button"
            onClick={onClose}
            className="py-2.5 px-3 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 font-medium text-xs transition-colors"
          >
            Dismiss
          </button>
        </div>
      </div>
    </div>
  );
};
