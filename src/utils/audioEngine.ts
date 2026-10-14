// Web Audio API Audio Booster Engine

export class AudioEngine {
  private ctx: AudioContext | null = null;

  // Nodes
  private masterGain: GainNode | null = null;
  private boostGain: GainNode | null = null;
  private compressor: DynamicsCompressorNode | null = null;
  private analyser: AnalyserNode | null = null;

  // Settings
  private volumeLevel: number = 0.8; // 0 to 1
  private boostLevel: number = 0.5; // 0 to 2 (+50% boost)
  private isBoostEnabled: boolean = true;
  private isLimiterEnabled: boolean = true;

  private initContext() {
    if (!this.ctx) {
      const AudioCtx = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
      this.ctx = new AudioCtx();
    }
    if (this.ctx.state === 'suspended') {
      this.ctx.resume();
    }

    if (!this.masterGain) {
      this.masterGain = this.ctx.createGain();
      this.boostGain = this.ctx.createGain();
      this.compressor = this.ctx.createDynamicsCompressor();
      this.analyser = this.ctx.createAnalyser();

      // Configure compressor as anti-clipping safety limiter
      this.compressor.threshold.setValueAtTime(-12, this.ctx.currentTime);
      this.compressor.knee.setValueAtTime(8, this.ctx.currentTime);
      this.compressor.ratio.setValueAtTime(14, this.ctx.currentTime);
      this.compressor.attack.setValueAtTime(0.003, this.ctx.currentTime);
      this.compressor.release.setValueAtTime(0.15, this.ctx.currentTime);

      this.analyser.fftSize = 128;
      this.analyser.smoothingTimeConstant = 0.8;

      // MasterGain -> BoostGain -> Limiter/Analyser -> Destination
      this.masterGain.connect(this.boostGain);
      this.updateLimiterRouting();
    }
  }

  private updateLimiterRouting() {
    if (!this.ctx || !this.boostGain || !this.compressor || !this.analyser) return;

    try {
      this.boostGain.disconnect();
      this.compressor.disconnect();
    } catch {
      // ignore
    }

    if (this.isLimiterEnabled) {
      this.boostGain.connect(this.compressor);
      this.compressor.connect(this.analyser);
    } else {
      this.boostGain.connect(this.analyser);
    }
    this.analyser.connect(this.ctx.destination);
  }

  public setVolume(pct: number) {
    this.initContext();
    this.volumeLevel = Math.max(0, Math.min(100, pct)) / 100;
    if (this.ctx && this.masterGain) {
      this.masterGain.gain.setTargetAtTime(this.volumeLevel, this.ctx.currentTime, 0.05);
    }
  }

  public setBoost(pct: number, enabled: boolean) {
    this.initContext();
    this.boostLevel = Math.max(0, Math.min(100, pct)) / 100; // 0 to 1.0 (representing +0% to +100%)
    this.isBoostEnabled = enabled;

    if (this.ctx && this.boostGain) {
      // 0% boost = gain multiplier 1.0
      // 100% boost = gain multiplier 2.0 (+100% loudness enhancement)
      const targetGain = enabled ? 1.0 + this.boostLevel : 1.0;
      this.boostGain.gain.setTargetAtTime(targetGain, this.ctx.currentTime, 0.05);
    }
  }

  public setLimiter(enabled: boolean) {
    this.initContext();
    this.isLimiterEnabled = enabled;
    this.updateLimiterRouting();
  }


}

export const audioEngine = new AudioEngine();
