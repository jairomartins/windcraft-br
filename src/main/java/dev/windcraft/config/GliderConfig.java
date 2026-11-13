package dev.windcraft.config;

import dev.windcraft.glider.GliderPhysics;
import net.minecraftforge.common.ForgeConfigSpec;

public final class GliderConfig {
    private static final ForgeConfigSpec.Builder B = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.DoubleValue MAX_SPEED = B.comment("Maximum horizontal blocks/tick; 1.1 = 22 m/s.").defineInRange("maxSpeed", 1.1, .3, 2);
    public static final ForgeConfigSpec.DoubleValue DENSITY = B.defineInRange("airDensity", 1.225, .5, 2);
    public static final ForgeConfigSpec.DoubleValue AREA = B.defineInRange("wingArea", 12.0, 4, 20);
    public static final ForgeConfigSpec.DoubleValue LIFT = B.defineInRange("lift", 1.0, .2, 2);
    public static final ForgeConfigSpec.DoubleValue DRAG = B.defineInRange("drag", .08, .02, .4);
    public static final ForgeConfigSpec.DoubleValue WIND = B.defineInRange("windInfluence", .35, 0, 1);
    public static final ForgeConfigSpec.DoubleValue SENSITIVITY = B.defineInRange("controlSensitivity", 1.0, .25, 2);
    public static final ForgeConfigSpec.DoubleValue TURBULENCE = B.defineInRange("turbulence", 1.0, 0, 2);
    public static final ForgeConfigSpec.IntValue DURABILITY = B.comment("Applied to gliders in player inventories on the server.").defineInRange("durability", 256, 16, 4096);
    public static final ForgeConfigSpec.IntValue WEAR_TICKS = B.defineInRange("durabilityTicks", 40, 10, 200);
    public static final ForgeConfigSpec.BooleanValue THERMALS = B.define("thermals", true);
    public static final ForgeConfigSpec.DoubleValue THERMAL_STRENGTH = B.comment("Maximum vertical air speed in m/s.").defineInRange("thermalStrength", 8.0, 0, 16);
    public static final ForgeConfigSpec SPEC = B.build();
    public static GliderPhysics.Settings settings() {
        return new GliderPhysics.Settings(DENSITY.get(), AREA.get(), LIFT.get(), DRAG.get(), WIND.get(),
            MAX_SPEED.get(), SENSITIVITY.get(), TURBULENCE.get());
    }
    private GliderConfig() {}
}
