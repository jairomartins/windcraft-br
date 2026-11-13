package dev.windcraft.simulation;

/** Pure deterministic simulation. No Minecraft classes or per-block work. */
public final class WindEngine {
    private WindEngine() {}
    private static double noise(long seed, double t) {
        long cell = (long)Math.floor(t);
        double f = t - cell;
        f = f * f * (3 - 2 * f);
        return hash(seed, cell) * (1 - f) + hash(seed, cell + 1) * f;
    }
    private static double hash(long seed, long cell) {
        long n = seed ^ (cell * 0x9E3779B97F4A7C15L);
        n = (n ^ (n >>> 30)) * 0xBF58476D1CE4E5B9L;
        n = (n ^ (n >>> 27)) * 0x94D049BB133111EBL;
        return ((n ^ (n >>> 31)) >>> 11) * 0x1.0p-53;
    }
    public static WindSample sample(long seed, long ticks, double day, boolean rain, boolean thunder,
                                    double biome, double altitude, double exposure, double base,
                                    double speedOverride, double directionOverride) {
        return sample(seed, ticks, day, rain ? 1.0 : 0.0, thunder ? 1.0 : 0.0,
            biome, altitude, exposure, base, speedOverride, directionOverride);
    }
    public static WindSample sample(long seed, long ticks, double day, double rain, double thunder,
                                    double biome, double altitude, double exposure, double base,
                                    double speedOverride, double directionOverride) {
        double t = ticks / 20.0;
        double gust = gustIntensity(seed, ticks) * 9;
        double speed = (base * (0.45 + noise(seed, t / 100)) + gust)
            * (0.85 + 0.15 * Math.sin(day * Math.PI * 2))
            * (1 + .3 * Math.clamp(rain, 0, 1) + .6 * Math.clamp(thunder, 0, 1))
            * biome * Math.clamp(1 + altitude / 256, 0.4, 2);
        if (speedOverride >= 0) speed = speedOverride;
        double direction = directionOverride >= 0 ? directionOverride : noise(seed + 3, t / 180) * 360;
        return new WindSample(speed * Math.clamp(exposure, 0, 1), direction);
    }
    public static double gustIntensity(long seed, long ticks) { return Math.pow(noise(seed + 12, ticks / 480.0), 6); }
}
