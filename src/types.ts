export type ThemeColor = 'lavender' | 'blue' | 'emerald' | 'amber' | 'amoled';

export interface ThemePalette {
  id: ThemeColor;
  name: string;
  primary: string;
  primaryContainer: string;
  onPrimary: string;
  onPrimaryContainer: string;
  surface: string;
  surfaceContainer: string;
  surfaceContainerHigh: string;
  outline: string;
  accent: string;
  warning: string;
  danger: string;
}

export interface BoosterState {
  systemVolume: number; // 0 to 100 (%)
  boostPercentage: number; // 0 to 100 (%)
  isBoostActive: boolean;
  isBackgroundRunning: boolean;
  limiterActive: boolean;
  currentPreset: string | null;
}
