package dev.windcraft.api;

import dev.windcraft.simulation.WindSample;

/** Air vector in m/s, with normalized gust and turbulence intensities. */
public record WindField(WindSample horizontal, double verticalSpeed, double gust, double turbulence) {
    public double x() { return horizontal.xPerTick() * 20; }
    public double y() { return verticalSpeed; }
    public double z() { return horizontal.zPerTick() * 20; }
}
