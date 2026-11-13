package dev.windcraft.rendering;

import dev.windcraft.simulation.WindSample;

/** Pure tick-based visual integration. Render rate never affects cloud speed. */
public final class WindAnimation {
    private double x, z, previousX, previousZ;
    private double cloudX, cloudZ, previousCloudX, previousCloudZ;
    private long ticks;

    public void tick(double speed, double direction, double cloudScale) {
        WindSample target = new WindSample(speed, direction);
        previousX = x; previousZ = z;
        x += (target.xPerTick() - x) * .12;
        z += (target.zPerTick() - z) * .12;
        previousCloudX = cloudX; previousCloudZ = cloudZ;
        cloudX += x * cloudScale;
        cloudZ += z * cloudScale;
        ticks++;
    }
    public double x(float partial) { return lerp(previousX, x, partial); }
    public double z(float partial) { return lerp(previousZ, z, partial); }
    public double cloudX(float partial) { return lerp(previousCloudX, cloudX, partial); }
    public double cloudZ(float partial) { return lerp(previousCloudZ, cloudZ, partial); }
    public float seconds(float partial) { return (float)(Math.max(0, ticks - 1 + Math.clamp(partial, 0, 1)) / 20.0); }
    public void reset() { x = z = previousX = previousZ = cloudX = cloudZ = previousCloudX = previousCloudZ = 0; ticks = 0; }
    private static double lerp(double a, double b, float t) { return a + (b - a) * Math.clamp(t, 0, 1); }
}
