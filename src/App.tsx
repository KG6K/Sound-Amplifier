import { useState, useEffect } from 'react';
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
  const dbGain = Math.round((boostPercentage * 60) / 100);

  const bgColor = isDark ? '#141218' : '#F8F9FA';
  const textTitleColor = isDark ? '#E6E1E5' : '#0F172A';
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
            <div className="w-10 h-10 rounded-2xl overflow-hidden flex items-center justify-center shadow-xs shrink-0 bg-[#0c0a14] border border-[#6834cd]/50">
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
              <p className="text-[11px] font-medium" style={{ color: textSubColor }}>
                {isBoostActive ? (
                  <span
                    className={
                      boostPercentage > 60
                        ? 'text-[#EF4444] font-bold'
                        : 'text-[#10B981] font-semibold'
                    }
                  >
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
            className="flex items-center gap-1.5 px-3 h-8 rounded-full text-xs font-semibold tracking-wide transition-all duration-200 active:scale-95 cursor-pointer shrink-0"
            style={{
              backgroundColor: isDark ? '#4A4458' : '#EDE7F6',
              color: isDark ? '#E8DEF8' : '#5E35B1',
            }}
          >
            <span className="text-xs">❤️</span>
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

        {/* Warning text below system volume slider (matching Android Compose) */}
        <div className="w-full flex items-start gap-2 px-1 py-1 mt-1">
          <span className="text-sm select-none shrink-0 leading-tight">⚠️</span>
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

      {/* Material 3 Compose AlertDialog for Support Development */}
      {showDonateDialog && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in">
          <div
            className="w-full max-w-[320px] sm:max-w-sm rounded-[28px] p-6 shadow-2xl transition-all"
            style={{
              backgroundColor: isDark ? '#2B2930' : '#ECE6F0',
              border: isDark ? '1px solid rgba(255, 255, 255, 0.08)' : '1px solid rgba(0, 0, 0, 0.08)',
              color: isDark ? '#E6E1E5' : '#1D1B20',
            }}
          >
            {/* Title Row */}
            <div className="flex items-center gap-2 mb-3">
              <span className="text-lg select-none">❤️</span>
              <h3
                className="font-bold text-lg tracking-tight"
                style={{ color: isDark ? '#E6E1E5' : '#1D1B20' }}
              >
                Support Development
              </h3>
            </div>

            {/* Content Text */}
            <p
              className="text-[13px] leading-relaxed mb-4 font-normal"
              style={{ color: isDark ? '#CAC4D0' : '#49454F' }}
            >
              An open-source, copylefted Sound Amplifier for Android.
            </p>

            {/* Action Buttons Column */}
            <div className="space-y-3 mb-5">
              {/* Material 3 Primary Filled Button */}
              <a
                href="upi://pay?pa=ansariusman@ptyes&pn=MOHAMAD%20U%20ANSHARI"
                className="w-full h-10 flex items-center justify-center gap-1.5 rounded-xl text-[13px] font-semibold transition active:scale-98 cursor-pointer text-center select-none shadow-xs"
                style={{
                  backgroundColor: isDark ? '#D0BCFF' : '#6750A4',
                  color: isDark ? '#381E72' : '#FFFFFF',
                }}
              >
                <span>⚡ Donate by UPI</span>
              </a>

              {/* Material 3 Outlined Button */}
              <a
                href="https://github.com/KG6K/Sound-Amplifier"
                target="_blank"
                rel="noopener noreferrer"
                className="w-full h-10 flex items-center justify-center gap-1.5 rounded-xl text-[13px] font-semibold transition active:scale-98 cursor-pointer text-center select-none"
                style={{
                  backgroundColor: 'transparent',
                  border: isDark ? '1px solid #938F99' : '1px solid #79747E',
                  color: isDark ? '#D0BCFF' : '#6750A4',
                }}
              >
                <span>GitHub Repository</span>
              </a>
            </div>

            {/* Material 3 Confirm/Dismiss TextButton */}
            <div className="flex justify-end">
              <button
                type="button"
                onClick={() => setShowDonateDialog(false)}
                className="px-4 py-2 rounded-full text-sm font-semibold transition cursor-pointer select-none"
                style={{
                  color: isDark ? '#D0BCFF' : '#6750A4',
                  backgroundColor: 'transparent',
                }}
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
