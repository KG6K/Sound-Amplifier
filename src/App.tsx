import { useState, useEffect } from 'react';
import { Zap } from 'lucide-react';
import { audioEngine } from './utils/audioEngine';
import { MaterialSlider } from './components/MaterialSlider';

export default function App() {
  // Booster State - 0% boost is default/off, >0% is active
  const [boostPercentage, setBoostPercentage] = useState<number>(0);
  const [systemVolume, setSystemVolume] = useState<number>(80);

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
  const dbGain = ((boostPercentage * 40) / 100).toFixed(1);

  const bgColor = isDark ? '#141218' : '#F8F9FA';
  const textTitleColor = isDark ? '#E3E2E6' : '#0F172A';
  const textSubColor = isDark ? '#938F99' : '#64748B';
  const iconBgColor = isBoostActive ? (isDark ? '#D0BCFF' : '#6750A4') : isDark ? '#26242E' : '#E2E8F0';
  const iconColor = isBoostActive ? (isDark ? '#381E72' : '#FFFFFF') : isDark ? '#94A3B8' : '#64748B';

  return (
    <div
      className="min-h-screen w-full flex flex-col items-center justify-start p-4 sm:p-6 transition-colors duration-300 font-sans select-none"
      style={{ backgroundColor: bgColor }}
    >
      <div className="w-full max-w-md space-y-4">
        {/* App Header Bar - Exact Match to Native Kotlin MainActivity (No Version Badge, No Switches) */}
        <div className="flex items-center justify-between py-2 px-1">
          <div className="flex items-center gap-3">
            <div
              className="w-10 h-10 rounded-2xl flex items-center justify-center shadow-sm transition-colors"
              style={{
                backgroundColor: iconBgColor,
                color: iconColor,
              }}
            >
              <Zap size={20} className={isBoostActive ? 'fill-current' : ''} />
            </div>
            <div>
              <h1
                className="text-lg font-bold tracking-tight"
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
        </div>

        {/* 1. PRIMARY: Boost Amplification Modern Line Slider */}
        <MaterialSlider
          label="Boost Amplification"
          sublabel="Amplifies audio volume past standard 100% Android speaker ceiling"
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
      </div>
    </div>
  );
}
