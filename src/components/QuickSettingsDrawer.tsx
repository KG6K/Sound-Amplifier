import React, { useState } from 'react';
import {
  Volume2,
  VolumeX,
  Zap,
  Wifi,
  Bluetooth,
  Flashlight,
  Moon,
  ChevronDown,
  X,
  Sun,
  ShieldAlert,
  Sliders,
  Settings,
  Power
} from 'lucide-react';
import { ThemePalette } from '../types';

interface QuickSettingsDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  boostActive: boolean;
  boostPercentage: number;
  onToggleBoost: () => void;
  onSetBoost: (pct: number) => void;
  systemVolume: number;
  onSetSystemVolume: (vol: number) => void;
  theme: ThemePalette;
  onOpenAppSheet?: () => void;
}

export const QuickSettingsDrawer: React.FC<QuickSettingsDrawerProps> = ({
  isOpen,
  onClose,
  boostActive,
  boostPercentage,
  onToggleBoost,
  onSetBoost,
  systemVolume,
  onSetSystemVolume,
  theme,
  onOpenAppSheet,
}) => {
  const [wifiOn, setWifiOn] = useState(true);
  const [btOn, setBtOn] = useState(true);
  const [torchOn, setTorchOn] = useState(false);
  const [dndOn, setDndOn] = useState(false);
  const [brightness, setBrightness] = useState(78);
  const [showTileDetail, setShowTileDetail] = useState(false);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex flex-col justify-start bg-black/80 backdrop-blur-md animate-in fade-in duration-200">
      {/* Pull down header bar */}
      <div className="w-full max-w-lg mx-auto bg-[#141218] rounded-b-3xl border-b border-x border-slate-800 shadow-2xl p-5 select-none transition-all">
        {/* Android Status Bar */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-800/80 text-xs text-slate-300 font-medium">
          <div className="flex items-center gap-2">
            <span className="font-semibold text-sm text-slate-100">07:55</span>
            <span>· Mon, Oct 12</span>
          </div>
          <div className="flex items-center gap-3">
            <button
              onClick={onClose}
              className="p-1 rounded-full hover:bg-slate-800 text-slate-400 hover:text-white"
              aria-label="Close Quick Settings"
            >
              <X size={18} />
            </button>
          </div>
        </div>

        {/* Brightness Slider */}
        <div className="flex items-center gap-3 my-4 px-1">
          <Sun size={18} className="text-slate-400 shrink-0" />
          <div className="relative flex-1 h-8 bg-slate-900 rounded-full overflow-hidden p-1 flex items-center border border-slate-800">
            <div
              className="absolute left-1 top-1 bottom-1 rounded-full bg-slate-200"
              style={{ width: `${brightness}%` }}
            />
            <input
              type="range"
              min="10"
              max="100"
              value={brightness}
              onChange={(e) => setBrightness(Number(e.target.value))}
              className="absolute inset-0 opacity-0 cursor-pointer w-full h-full"
            />
          </div>
        </div>

        {/* Quick Settings Grid */}
        <div className="text-xs font-semibold text-slate-400 mb-2.5 px-1 uppercase tracking-wider">
          Quick Settings Tiles
        </div>

        <div className="grid grid-cols-2 gap-2.5 mb-4">
          {/* PRIMARY: Volume Booster Tile */}
          <div
            className={`col-span-2 relative p-3.5 rounded-2xl flex items-center justify-between transition-all cursor-pointer border ${
              boostActive
                ? 'shadow-lg border-transparent'
                : 'bg-[#211f26] border-slate-800 text-slate-300'
            }`}
            style={{
              backgroundColor: boostActive ? theme.primary : '#211f26',
              color: boostActive ? theme.onPrimary : '#e3e2e6',
            }}
          >
            <div
              className="flex items-center gap-3 flex-1"
              onClick={() => {
                onToggleBoost();
                if (navigator.vibrate) navigator.vibrate(12);
              }}
            >
              <div
                className={`w-10 h-10 rounded-full flex items-center justify-center ${
                  boostActive ? 'bg-black/20' : 'bg-slate-800'
                }`}
              >
                <Zap
                  size={20}
                  className={boostActive ? 'fill-current' : 'text-slate-300'}
                />
              </div>
              <div>
                <div className="text-sm font-bold tracking-tight">
                  Sound Amplifier
                </div>
                <div
                  className={`text-xs ${
                    boostActive ? 'opacity-85 font-medium' : 'text-slate-400'
                  }`}
                >
                  {boostActive ? `Active (+${boostPercentage}%)` : 'Off'}
                </div>
              </div>
            </div>

            {/* Quick Details Expand Button */}
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                setShowTileDetail(!showTileDetail);
              }}
              className={`p-2 rounded-xl transition-colors ${
                boostActive
                  ? 'hover:bg-black/10 text-current'
                  : 'hover:bg-slate-800 text-slate-400'
              }`}
              title="Quick Boost Slider"
            >
              <ChevronDown
                size={18}
                className={`transition-transform ${showTileDetail ? 'rotate-180' : ''}`}
              />
            </button>
          </div>

          {/* Expanded Inline Quick Slider inside Tile */}
          {showTileDetail && (
            <div className="col-span-2 p-3.5 rounded-2xl bg-[#1c1a22] border border-slate-700/80 animate-in slide-in-from-top-2 duration-150 space-y-3">
              <div className="flex items-center justify-between text-xs text-slate-300">
                <span className="font-medium">Boost Level Slider</span>
                <span className="font-mono font-bold text-amber-300">
                  +{boostPercentage}%
                </span>
              </div>
              <input
                type="range"
                min="0"
                max="100"
                step="5"
                value={boostPercentage}
                onChange={(e) => onSetBoost(Number(e.target.value))}
                className="w-full accent-purple-400 h-2 bg-slate-800 rounded-lg cursor-pointer"
              />
            </div>
          )}

          {/* Wi-Fi Tile */}
          <button
            type="button"
            onClick={() => setWifiOn(!wifiOn)}
            className={`p-3 rounded-2xl flex items-center gap-3 transition-colors text-left border ${
              wifiOn
                ? 'bg-slate-800 border-slate-700 text-white'
                : 'bg-[#211f26] border-slate-800 text-slate-400'
            }`}
          >
            <div className="w-8 h-8 rounded-full bg-slate-700/80 flex items-center justify-center">
              <Wifi size={17} />
            </div>
            <div>
              <div className="text-xs font-semibold">Internet</div>
              <div className="text-[10px] text-slate-400">
                {wifiOn ? 'Pixel_5G_Home' : 'Disconnected'}
              </div>
            </div>
          </button>

          {/* Bluetooth Tile */}
          <button
            type="button"
            onClick={() => setBtOn(!btOn)}
            className={`p-3 rounded-2xl flex items-center gap-3 transition-colors text-left border ${
              btOn
                ? 'bg-slate-800 border-slate-700 text-white'
                : 'bg-[#211f26] border-slate-800 text-slate-400'
            }`}
          >
            <div className="w-8 h-8 rounded-full bg-slate-700/80 flex items-center justify-center">
              <Bluetooth size={17} />
            </div>
            <div>
              <div className="text-xs font-semibold">Bluetooth</div>
              <div className="text-[10px] text-slate-400">
                {btOn ? 'Sony WH-1000XM5' : 'Off'}
              </div>
            </div>
          </button>

          {/* Flashlight Tile */}
          <button
            type="button"
            onClick={() => setTorchOn(!torchOn)}
            className={`p-3 rounded-2xl flex items-center gap-3 transition-colors text-left border ${
              torchOn
                ? 'bg-amber-400/20 border-amber-500/40 text-amber-200'
                : 'bg-[#211f26] border-slate-800 text-slate-400'
            }`}
          >
            <div className="w-8 h-8 rounded-full bg-slate-700/80 flex items-center justify-center">
              <Flashlight size={17} />
            </div>
            <div>
              <div className="text-xs font-semibold">Flashlight</div>
              <div className="text-[10px] text-slate-400">
                {torchOn ? 'On' : 'Off'}
              </div>
            </div>
          </button>

          {/* Do Not Disturb */}
          <button
            type="button"
            onClick={() => setDndOn(!dndOn)}
            className={`p-3 rounded-2xl flex items-center gap-3 transition-colors text-left border ${
              dndOn
                ? 'bg-purple-900/40 border-purple-700/50 text-purple-200'
                : 'bg-[#211f26] border-slate-800 text-slate-400'
            }`}
          >
            <div className="w-8 h-8 rounded-full bg-slate-700/80 flex items-center justify-center">
              <Moon size={17} />
            </div>
            <div>
              <div className="text-xs font-semibold">Do Not Disturb</div>
              <div className="text-[10px] text-slate-400">
                {dndOn ? 'Priority only' : 'Off'}
              </div>
            </div>
          </button>
        </div>

        {/* Bottom Drawer Actions */}
        <div className="flex items-center justify-between pt-3 border-t border-slate-800/80 text-xs">
          <div className="text-slate-400 flex items-center gap-2">
            <span>Tile Service: Running</span>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={onClose}
              className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 font-medium transition-colors"
            >
              Done
            </button>
          </div>
        </div>
      </div>

      {/* Tap backdrop to close */}
      <div className="flex-1" onClick={onClose} />
    </div>
  );
};
