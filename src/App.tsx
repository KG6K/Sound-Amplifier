import { useState, useEffect } from 'react';
import { AlertTriangle, Heart, X } from 'lucide-react';
import { audioEngine } from './utils/audioEngine';
import { MaterialSlider } from './components/MaterialSlider';

export default function App() {
  // Booster State - 0% boost is default/off, >0% is active
  const [boostPercentage, setBoostPercentage] = useState<number>(0);
  const [systemVolume, setSystemVolume] = useState<number>(80);
  const [showDonateDialog, setShowDonateDialog] = useState<boolean>(false);

  // Automatic System Theme Detection
  const [isDark, setIsDark] = useState<boolean>(() => {
    if (typeof window !== 'undefined' && window.matchMedia) {
      return window.matchMedia('(prefers-color-scheme: dark)').matches;
    }
    return true;
  });

  useEffect(() => {
    if (typeof window === 'undefined' || !window.matchMedia) return;
    const media = window.matchMedia('(prefers-color-scheme: dark)');
    const listener = (e: MediaQueryListEvent) => setIsDark(e.matches);
    media.addEventListener('change', listener);
    return () => media.removeEventListener('change', listener);
  }, []);

  // Synchronize audio engine with initial settings
  useEffect(() => {
    audioEngine.setVolume(systemVolume);
    audioEngine.setBoost(boostPercentage, boostPercentage > 0);
  }, []);

  const handleSystemVolumeChange = (vol: number) => {
    setSystemVolume(vol);
    audioEngine.setVolume(vol);
  };

  const handleBoostChange = (pct: number) => {
    const clamped = Math.max(0, Math.min(100, pct));
    setBoostPercentage(clamped);
    audioEngine.setBoost(clamped, clamped > 0);
  };

  const isBoostActive = boostPercentage > 0;
  // Maximum boost amplification is 60 dB
  const dbGain = ((boostPercentage * 60) / 100).toFixed(1);

  const bgColor = isDark ? '#141218' : '#F8F9FA';
  const textTitleColor = isDark ? '#E3E2E6' : '#0F172A';
  const textSubColor = isDark ? '#938F99' : '#64748B';

  return (
    <div
      className="min-h-screen w-full flex flex-col items-center justify-start p-4 sm:p-6 transition-colors duration-300 font-sans select-none"
      style={{ backgroundColor: bgColor }}
    >
      <div className="w-full max-w-md space-y-3">
        {/* App Header Bar with App Icon, Title, Active dB, and Donate Button in Top Right Corner */}
        <div className="flex items-center justify-between py-2 px-1">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-2xl overflow-hidden flex items-center justify-center shadow-sm shrink-0 bg-[#0a0812] border border-[#6834cd]/40">
              <img
                src="/app_icon.svg"
                alt="Sound Amplifier Icon"
                className="w-full h-full object-contain"
              />
            </div>
            <div>
              <h1
                className="text-lg font-bold tracking-tight leading-tight"
                style={{ color: textTitleColor }}
              >
                Sound Amplifier
              </h1>
              <p className="text-xs font-medium" style={{ color: textSubColor }}>
                {isBoostActive ? (
                  <span className="text-amber-500 font-semibold">
                    Amplification active (+{dbGain} dB)
                  </span>
                ) : (
                  <span>Amplification idle (0%)</span>
                )}
              </p>
            </div>
          </div>

          {/* Donate in top right corner */}
          <button
            type="button"
            onClick={() => setShowDonateDialog(true)}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold tracking-wide transition-all duration-200 active:scale-95 shadow-xs cursor-pointer"
            style={{
              backgroundColor: isDark ? '#2E1C4D' : '#EDE7F6',
              color: isDark ? '#D0BCFF' : '#5E35B1',
              border: isDark ? '1px solid #5C3A9C' : '1px solid #D1C4E9',
            }}
          >
            <Heart size={13} className="fill-rose-500 text-rose-500" />
            <span>Donate</span>
          </button>
        </div>

        {/* 1. PRIMARY: Boost Amplification Modern Line Slider (Max 60 dB) */}
        <MaterialSlider
          label="Boost Amplification"
          value={boostPercentage}
          min={0}
          max={100}
          step={1}
          prefix="+"
          unit="%"
          isBoostMode={true}
          enabled={true}
          isDark={isDark}
          onChange={handleBoostChange}
        />

        {/* 2. System Media Volume Line Slider */}
        <MaterialSlider
          label="System Media Volume"
          value={systemVolume}
          min={0}
          max={100}
          step={5}
          unit="%"
          isBoostMode={false}
          enabled={true}
          isDark={isDark}
          onChange={handleSystemVolumeChange}
        />

        {/* Warning text below system volume slider */}
        <div
          className="w-full p-3.5 rounded-2xl border flex items-start gap-3 mt-1.5 transition-colors"
          style={{
            backgroundColor: isDark ? '#23180d' : '#FFFBEB',
            borderColor: isDark ? '#5C3815' : '#FDE68A',
          }}
        >
          <div className="mt-0.5 shrink-0 text-amber-500">
            <AlertTriangle size={17} />
          </div>
          <p
            className="text-[11.5px] leading-relaxed font-normal"
            style={{
              color: isDark ? '#FCD34D' : '#92400E',
            }}
          >
            Warning: Playing audio at high volumes may damage speakers/hearing. ALL USE IS AT YOUR OWN RISK. Particularly beware of boosts above 60%, especially with music.
          </p>
        </div>
      </div>

      {/* Donate Modal Dialog */}
      {showDonateDialog && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in">
          <div
            className="w-full max-w-sm rounded-3xl p-6 shadow-2xl border transition-all"
            style={{
              backgroundColor: isDark ? '#1F1D25' : '#FFFFFF',
              borderColor: isDark ? '#353241' : '#E2E8F0',
              color: textTitleColor,
            }}
          >
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2.5">
                <div className="w-9 h-9 rounded-xl bg-rose-500/10 text-rose-500 flex items-center justify-center">
                  <Heart size={18} className="fill-current" />
                </div>
                <h3 className="font-bold text-base">Support Development</h3>
              </div>
              <button
                type="button"
                onClick={() => setShowDonateDialog(false)}
                className="p-1 rounded-full text-zinc-400 hover:text-zinc-200 transition"
              >
                <X size={18} />
              </button>
            </div>
            <p className="text-xs leading-relaxed mb-5" style={{ color: textSubColor }}>
              Thank you for wanting to support Sound Amplifier! Donation options and links will be configured here.
            </p>
            <div className="flex justify-end">
              <button
                type="button"
                onClick={() => setShowDonateDialog(false)}
                className="px-4 py-2 rounded-xl text-xs font-semibold bg-violet-600 hover:bg-violet-500 text-white active:scale-95 transition"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
