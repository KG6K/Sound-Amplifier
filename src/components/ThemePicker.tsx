import React from 'react';
import { Palette, Check } from 'lucide-react';
import { THEMES } from '../utils/theme';
import { ThemeColor, ThemePalette } from '../types';

interface ThemePickerProps {
  currentTheme: ThemeColor;
  onSelectTheme: (theme: ThemeColor) => void;
  isOpen: boolean;
  onClose: () => void;
}

export const ThemePicker: React.FC<ThemePickerProps> = ({
  currentTheme,
  onSelectTheme,
  isOpen,
  onClose,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-in fade-in duration-150">
      <div className="w-full max-w-sm rounded-3xl bg-[#1e1f24] border border-slate-700/80 p-5 shadow-2xl space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Palette size={18} className="text-purple-300" />
            <h3 className="text-sm font-bold text-slate-100">
              Color Theme
            </h3>
          </div>
          <button
            onClick={onClose}
            className="text-xs text-slate-400 hover:text-white px-2 py-1 rounded-lg hover:bg-slate-800"
          >
            Close
          </button>
        </div>

        <p className="text-xs text-slate-400">
          Select a dynamic accent palette for your interface.
        </p>

        <div className="space-y-2">
          {Object.values(THEMES).map((theme) => {
            const isSelected = currentTheme === theme.id;
            return (
              <button
                key={theme.id}
                type="button"
                onClick={() => {
                  onSelectTheme(theme.id);
                  if (navigator.vibrate) navigator.vibrate(8);
                  onClose();
                }}
                className={`w-full p-3 rounded-2xl flex items-center justify-between border transition-all ${
                  isSelected
                    ? 'border-purple-400/80 bg-purple-950/30 shadow-md'
                    : 'border-slate-800 bg-slate-900/60 hover:bg-slate-800/80'
                }`}
              >
                <div className="flex items-center gap-3">
                  <div
                    className="w-7 h-7 rounded-full shadow-inner border border-white/20 flex items-center justify-center"
                    style={{ backgroundColor: theme.primary }}
                  >
                    {isSelected && (
                      <Check size={14} style={{ color: theme.onPrimary }} />
                    )}
                  </div>
                  <span className="text-xs font-semibold text-slate-200">
                    {theme.name}
                  </span>
                </div>

                <div className="flex items-center gap-1.5">
                  <div
                    className="w-4 h-4 rounded-full"
                    style={{ backgroundColor: theme.primaryContainer }}
                  />
                  <div
                    className="w-4 h-4 rounded-full"
                    style={{ backgroundColor: theme.surfaceContainerHigh }}
                  />
                </div>
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
};
