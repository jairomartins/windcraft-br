package dev.windcraft.rendering;

/** Pure client presentation logic; velocity is blocks/second, slope is blocks/block. */
public final class EnvironmentalMotion {
    private EnvironmentalMotion() {}
    public static double slope(double speed, double fallSpeed, double strength, double maxAngle) {
        return Math.min(Math.max(0, speed * strength / fallSpeed), Math.tan(Math.toRadians(maxAngle)));
    }
    public static double smooth(double value, double target) { return value + (target - value) * .08; }
    public static final class Storm {
        private int high, low;
        private boolean active;
        private double intensity;
        public double tick(double speed, boolean eligible, double start, double stop, int startTicks, int stopTicks, int fade) {
            stop = Math.min(stop, Math.max(0, start - .1));
            high = eligible && speed >= start ? high + 1 : 0;
            low = !eligible || speed <= stop ? low + 1 : 0;
            if (!active && high >= startTicks) active = true;
            if (active && low >= stopTicks) active = false;
            double target = active && eligible ? 1 : 0;
            intensity += Math.clamp(target - intensity, -1.0 / fade, 1.0 / fade);
            return intensity;
        }
    }
}
