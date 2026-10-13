import React, { useState, useEffect } from 'react';
import {
  Volume2,
  VolumeX,
  Zap,
  Palette,
  Smartphone,
  ChevronDown,
  Activity,
  Info,
  X,
  BatteryCharging,
  Wifi,
  CheckCircle2,
} from 'lucide-react';
import { ThemeColor, BoosterState } from './types';
import { THEMES } from './utils/theme';
import { audioEngine } from './utils/audioEngine';
import { MaterialSlider } from './components/MaterialSlider';
import { AudioVisualizer } from './components/AudioVisualizer';
import { QuickSettingsDrawer } from './components/QuickSettingsDrawer';
import { ThemePicker } from './components/ThemePicker';
import { NotificationShade } from './components/NotificationShade';
import { SafetyLimiterModal } from './components/SafetyLimiterModal';

export default function App() {
  // Theme State
  const [currentThemeId, setCurrentThemeId] = useState<ThemeColor>('lavender');
  const [themePickerOpen, setThemePickerOpen] = useState(false);
  const theme = THEMES[currentThemeId];

  // Instruction display state: Shown for first launch only (persisted in localStorage)
  const [showInstructions, setShowInstructions] = useState<boolean>(() => {
    try {
      return (
        localStorage.getItem('soundamplifier_onboarding_seen') !== 'true' &&
        localStorage.getItem('soundsurge_onboarding_seen') !== 'true'
      );
    } catch {
      return false;
    }
  });

  // Booster State
  const [state, setState] = useState<BoosterState>({
    systemVolume: 80,
    boostPercentage: 50,
    isBoostActive: true,
    isBackgroundRunning: true,
    limiterActive: true,
    currentPreset: null,
  });

  // Drawers & Modals
  const [qsDrawerOpen, setQsDrawerOpen] = useState(false);
  const [safetyModalOpen, setSafetyModalOpen] = useState(false);
  const [deviceFrameMode, setDeviceFrameMode] = useState(true);

  // Synchronize audio engine with initial settings
  useEffect(() => {
    audioEngine.setVolume(state.systemVolume);
    audioEngine.setBoost(state.boostPercentage, state.isBoostActive);
    audioEngine.setLimiter(state.limiterActive);
  }, []);

  // Persistent foreground service simulation with wakeLock
  useEffect(() => {
    let wakeLock: any = null;
    const requestWake = async () => {
      try {
        if ('wakeLock' in navigator) {
          wakeLock = await (navigator as any).wakeLock.request('screen');
        }
      } catch {
        // wakeLock might fail in unprompted contexts
      }
    };
    if (state.isBackgroundRunning) {
      requestWake();
    }
    return () => {
      if (wakeLock) wakeLock.release();
    };
  }, [state.isBackgroundRunning]);

  // Dismiss instruction and save to localStorage (so it's shown for first time only)
  const dismissInstructions = () => {
    setShowInstructions(false);
    try {
      localStorage.setItem('soundamplifier_onboarding_seen', 'true');
    } catch {
      // ignore
    }
  };

  // Handlers
  const handleSystemVolumeChange = (vol: number) => {
    setState((prev) => ({ ...prev, systemVolume: vol }));
    audioEngine.setVolume(vol);
  };

  const handleBoostChange = (pct: number) => {
    const clamped = Math.max(0, Math.min(100, pct));
    setState((prev) => ({
      ...prev,
      boostPercentage: clamped,
      isBoostActive: clamped > 0 ? true : prev.isBoostActive,
    }));
    audioEngine.setBoost(clamped, clamped > 0 ? true : state.isBoostActive);
  };

  const handleToggleBoost = () => {
    setState((prev) => {
      const nextActive = !prev.isBoostActive;
      audioEngine.setBoost(prev.boostPercentage, nextActive);
      return { ...prev, isBoostActive: nextActive };
    });
  };

  return (
    <div
      className="min-h-screen w-full flex flex-col items-center justify-start p-2 sm:p-4 md:p-6 transition-colors duration-300 font-sans"
      style={{ backgroundColor: theme.surface, color: '#e3e2e6' }}
    >
      {/* Top Banner / Device Frame Toggle for Desktop */}
      <div className="w-full max-w-5xl flex items-center justify-between pb-3 text-xs text-slate-400">
        <div className="flex items-center gap-2">
          <div
            className="w-2.5 h-2.5 rounded-full"
            style={{ backgroundColor: theme.primary }}
          />
          <span className="font-semibold text-slate-200">
            Sound Amplifier
          </span>
          <span className="hidden sm:inline">
            · Volume & Audio Booster
          </span>
        </div>

        <div className="flex items-center gap-2">
          {/* Quick Settings Drawer Launcher Button */}
          <button
            type="button"
            onClick={() => setQsDrawerOpen(true)}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-800/80 hover:bg-slate-700 text-slate-200 transition-colors border border-slate-700/60"
            title="Open Android Quick Settings Shade"
          >
            <ChevronDown size={14} className="text-purple-300" />
            <span className="font-medium text-xs">Quick Settings Shade</span>
          </button>

          {/* Instructions re-open icon */}
          <button
            type="button"
            onClick={() => setShowInstructions((prev) => !prev)}
            className="p-1.5 rounded-xl bg-slate-800/80 hover:bg-slate-700 text-slate-300 hover:text-white transition-colors border border-slate-700/60"
            title="Toggle Instructions"
          >
            <Info size={16} />
          </button>

          {/* Theme Picker Trigger */}
          <button
            type="button"
            onClick={() => setThemePickerOpen(true)}
            className="p-1.5 rounded-xl bg-slate-800/80 hover:bg-slate-700 text-slate-300 hover:text-white transition-colors border border-slate-700/60"
            title="Change Color Theme"
          >
            <Palette size={16} />
          </button>

          {/* Device Frame Viewport Toggle */}
          <button
            type="button"
            onClick={() => setDeviceFrameMode(!deviceFrameMode)}
            className="hidden md:flex items-center gap-1.5 px-2.5 py-1.5 rounded-xl bg-slate-800/80 hover:bg-slate-700 text-slate-300 hover:text-white transition-colors border border-slate-700/60"
            title="Toggle Smartphone Frame View"
          >
            <Smartphone size={14} />
            <span className="text-xs">{deviceFrameMode ? 'Phone Frame' : 'Wide Layout'}</span>
          </button>
        </div>
      </div>

      {/* Main Container - Either Phone Frame or Full Responsive Container */}
      <div
        className={`w-full transition-all duration-300 ${
          deviceFrameMode
            ? 'max-w-[420px] rounded-[44px] border-[10px] border-[#222329] shadow-2xl overflow-hidden bg-[#141218] relative'
            : 'max-w-4xl rounded-3xl border border-slate-800 shadow-2xl bg-[#141218] p-4 sm:p-6'
        }`}
      >
        {/* Android Phone Status Bar (Always on Phone Frame) */}
        <div
          onClick={() => setQsDrawerOpen(true)}
          className="w-full pt-3 px-5 pb-2 flex items-center justify-between text-xs text-slate-300 font-medium cursor-pointer hover:bg-white/5 transition-colors select-none group"
          title="Pull down to open Android Quick Settings"
        >
          <div className="flex items-center gap-2">
            <span className="font-bold text-sm">07:55</span>
            <div className="w-2.5 h-2.5 rounded-full bg-slate-700 group-hover:bg-purple-400 transition-colors" />
          </div>

          {/* Android Punch Hole Camera Sim */}
          <div className="w-3.5 h-3.5 rounded-full bg-black border border-slate-800 flex items-center justify-center">
            <div className="w-1.5 h-1.5 rounded-full bg-slate-900" />
          </div>

          <div className="flex items-center gap-2 text-slate-400">
            <Wifi size={13} />
            <span className="text-[10px] font-mono">5G</span>
            <div className="flex items-center gap-0.5">
              <span className="text-[10px] font-mono">94%</span>
              <BatteryCharging size={14} className="text-emerald-400" />
            </div>
          </div>
        </div>

        {/* Quick Settings Swipe Down Handle Hint */}
        <div
          onClick={() => setQsDrawerOpen(true)}
          className="w-full flex items-center justify-center py-0.5 cursor-pointer hover:bg-white/5"
        >
          <div className="w-10 h-1 rounded-full bg-slate-700/60" />
        </div>

        {/* App Top Bar */}
        <div className="px-5 pt-3 pb-3 flex items-center justify-between border-b border-slate-800/80">
          <div className="flex items-center gap-3">
            <div
              className="w-10 h-10 rounded-2xl flex items-center justify-center shadow-md"
              style={{
                backgroundColor: state.isBoostActive ? theme.primary : '#2a2830',
                color: state.isBoostActive ? theme.onPrimary : '#999',
              }}
            >
              <Zap size={20} className={state.isBoostActive ? 'fill-current' : ''} />
            </div>
            <div>
              <h1 className="text-base font-bold tracking-tight text-white">
                Sound Amplifier
              </h1>
            </div>
          </div>

          <div className="flex items-center gap-2">
            {/* Quick Master Power Toggle */}
            <button
              type="button"
              onClick={handleToggleBoost}
              className={`p-2.5 rounded-2xl border transition-all ${
                state.isBoostActive
                  ? 'border-purple-400/80 bg-purple-950/40 text-purple-200'
                  : 'border-slate-800 bg-slate-900 text-slate-500'
              }`}
              title="Toggle Master Boost"
            >
              {state.isBoostActive ? <Volume2 size={18} /> : <VolumeX size={18} />}
            </button>
          </div>
        </div>

        {/* Content Body */}
        <div className="p-4 sm:p-5 space-y-4 max-h-[76vh] overflow-y-auto">
          {/* First-Time Only Instructions Banner */}
          {showInstructions && (
            <div
              className="p-4 rounded-3xl border border-purple-500/30 shadow-lg space-y-2.5 relative animate-in fade-in duration-200"
              style={{ backgroundColor: `${theme.surfaceContainerHigh}` }}
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2 text-xs font-bold text-slate-100">
                  <Info size={16} style={{ color: theme.primary }} />
                  <span>Quick Setup Guide (First Launch)</span>
                </div>
                <button
                  type="button"
                  onClick={dismissInstructions}
                  className="p-1 rounded-full text-slate-400 hover:text-white hover:bg-slate-800"
                  aria-label="Dismiss instructions"
                >
                  <X size={15} />
                </button>
              </div>

              <div className="text-xs text-slate-300 space-y-1.5 leading-relaxed">
                <div className="flex items-start gap-2">
                  <span className="font-mono text-purple-300 font-bold shrink-0">1.</span>
                  <span>Drag the <strong>Boost Amplification</strong> slider to increase audio beyond normal Android 100% output.</span>
                </div>
                <div className="flex items-start gap-2">
                  <span className="font-mono text-purple-300 font-bold shrink-0">2.</span>
                  <span>Audio continues running in the background for music, video, and apps.</span>
                </div>
                <div className="flex items-start gap-2">
                  <span className="font-mono text-purple-300 font-bold shrink-0">3.</span>
                  <span>Swipe down your notification shade and edit tiles to add the <strong>Volume Booster Tile</strong> for one-tap control.</span>
                </div>
              </div>

              <div className="pt-1 flex justify-end">
                <button
                  type="button"
                  onClick={dismissInstructions}
                  className="px-3.5 py-1.5 rounded-xl font-semibold text-xs transition-colors flex items-center gap-1.5 shadow-sm"
                  style={{
                    backgroundColor: theme.primary,
                    color: theme.onPrimary,
                  }}
                >
                  <CheckCircle2 size={13} />
                  <span>Got It, Don't Show Again</span>
                </button>
              </div>
            </div>
          )}

          {/* Android Notification Shade Card */}
          {state.isBackgroundRunning && (
            <NotificationShade
              boostPercentage={state.boostPercentage}
              boostActive={state.isBoostActive}
              onToggleBoost={handleToggleBoost}
              onQuickAdd={(amount) => {
                const next = Math.min(100, state.boostPercentage + amount);
                handleBoostChange(next);
              }}
              onStopService={() => {
                setState((prev) => ({
                  ...prev,
                  isBackgroundRunning: false,
                  isBoostActive: false,
                }));
                audioEngine.setBoost(0, false);
              }}
              theme={theme}
            />
          )}

          {/* Real-time Dynamic Spectrum Monitor */}
          <div className="space-y-1.5">
            <div className="flex items-center justify-between text-xs text-slate-400 px-1">
              <span className="flex items-center gap-1.5 font-medium">
                <Activity size={13} style={{ color: theme.primary }} />
                Output Spectrum Power
              </span>
              <span className="font-mono text-[11px] text-slate-400">
                {state.isBoostActive
                  ? `Gain +${((state.boostPercentage / 100) * 12).toFixed(1)} dB (${(1 + state.boostPercentage / 100).toFixed(1)}x)`
                  : '0.0 dB (Bypassed)'}
              </span>
            </div>
            <AudioVisualizer
              boostActive={state.isBoostActive}
              theme={theme}
              boostLevel={state.isBoostActive ? state.boostPercentage : 0}
              systemVolume={state.systemVolume}
            />
          </div>

          {/* PRIMARY: Expressive Boost Percentage Slider */}
          <MaterialSlider
            label="Boost Amplification"
            sublabel="Increases volume past normal 100% Android speaker ceiling"
            value={state.isBoostActive ? state.boostPercentage : 0}
            min={0}
            max={100}
            step={5}
            prefix="+"
            unit="%"
            theme={theme}
            isBoostMode={true}
            disabled={!state.isBoostActive}
            ticks={[0, 25, 50, 75, 100]}
            onChange={handleBoostChange}
            onDangerousThreshold={() => setSafetyModalOpen(true)}
          />

          {/* Standard Android Media Volume Slider */}
          <MaterialSlider
            label="System Media Volume"
            sublabel="Base device audio stream level (0% to 100%)"
            value={state.systemVolume}
            min={0}
            max={100}
            step={5}
            unit="%"
            theme={theme}
            isBoostMode={false}
            ticks={[0, 25, 50, 75, 100]}
            onChange={handleSystemVolumeChange}
          />

          {/* Quick Settings Tile Card */}
          <div
            className="p-4 rounded-3xl border border-slate-800/80 shadow-md space-y-3"
            style={{ backgroundColor: theme.surfaceContainer }}
          >
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2.5">
                <div
                  className="w-8 h-8 rounded-xl flex items-center justify-center"
                  style={{ backgroundColor: theme.primary, color: theme.onPrimary }}
                >
                  <Zap size={16} className="fill-current" />
                </div>
                <div>
                  <h3 className="text-xs font-bold text-slate-100">
                    Quick Settings Tile
                  </h3>
                  <p className="text-[11px] text-slate-400">
                    Quick Settings Integration
                  </p>
                </div>
              </div>

              <button
                type="button"
                onClick={() => setQsDrawerOpen(true)}
                className="px-2.5 py-1 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-medium flex items-center gap-1 transition-colors"
              >
                <span>Pull Down Shade</span>
                <ChevronDown size={13} />
              </button>
            </div>

            {/* Tap to Toggle Tile Preview */}
            <button
              type="button"
              onClick={handleToggleBoost}
              className={`w-full p-3.5 rounded-2xl flex items-center justify-between transition-all shadow-md border ${
                state.isBoostActive
                  ? 'border-transparent'
                  : 'bg-[#211f26] border-slate-800 text-slate-300'
              }`}
              style={{
                backgroundColor: state.isBoostActive ? theme.primary : '#211f26',
                color: state.isBoostActive ? theme.onPrimary : '#e3e2e6',
              }}
            >
              <div className="flex items-center gap-3">
                <div
                  className={`w-9 h-9 rounded-full flex items-center justify-center ${
                    state.isBoostActive ? 'bg-black/20' : 'bg-slate-800'
                  }`}
                >
                  <Zap
                    size={18}
                    className={state.isBoostActive ? 'fill-current' : 'text-slate-300'}
                  />
                </div>
                <div className="text-left">
                  <div className="text-xs font-bold">Sound Amplifier Tile</div>
                  <div
                    className={`text-[11px] ${
                      state.isBoostActive ? 'opacity-85 font-medium' : 'text-slate-400'
                    }`}
                  >
                    {state.isBoostActive
                      ? `Active (+${state.boostPercentage}%)`
                      : 'Off'}
                  </div>
                </div>
              </div>

              <div className="px-2 py-0.5 rounded-lg text-[10px] font-mono font-bold bg-black/15">
                {state.isBoostActive ? 'ON' : 'OFF'}
              </div>
            </button>
          </div>
        </div>

        {/* Android Gesture Navigation Bar (Bottom pill line) */}
        <div className="py-2.5 flex items-center justify-center bg-[#141218] border-t border-slate-900">
          <div className="w-32 h-1 rounded-full bg-slate-600" />
        </div>
      </div>

      {/* Android 14/15 Quick Settings Pull-Down Drawer */}
      <QuickSettingsDrawer
        isOpen={qsDrawerOpen}
        onClose={() => setQsDrawerOpen(false)}
        boostActive={state.isBoostActive}
        boostPercentage={state.boostPercentage}
        onToggleBoost={handleToggleBoost}
        onSetBoost={handleBoostChange}
        systemVolume={state.systemVolume}
        onSetSystemVolume={handleSystemVolumeChange}
        theme={theme}
      />

      {/* Safety Alert Modal */}
      <SafetyLimiterModal
        isOpen={safetyModalOpen}
        onClose={() => setSafetyModalOpen(false)}
        onEnableLimiter={() => {
          setState((p) => ({ ...p, limiterActive: true }));
          audioEngine.setLimiter(true);
        }}
        targetPercent={state.boostPercentage}
        theme={theme}
      />

      {/* Theme Picker Modal */}
      <ThemePicker
        currentTheme={currentThemeId}
        onSelectTheme={setCurrentThemeId}
        isOpen={themePickerOpen}
        onClose={() => setThemePickerOpen(false)}
      />
    </div>
  );
}
