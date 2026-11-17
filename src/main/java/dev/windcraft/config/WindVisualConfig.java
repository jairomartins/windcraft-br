package dev.windcraft.config;

import net.minecraftforge.common.ForgeConfigSpec;

/** Client preferences only; never changes the server simulation. */
public final class WindVisualConfig {
    private static final ForgeConfigSpec.Builder B = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.BooleanValue CLOUDS = B.comment("Move vanilla clouds in the atmospheric wind direction.").define("animateClouds", true);
    public static final ForgeConfigSpec.DoubleValue CLOUD_SCALE = B.comment("Cloud visual speed as a fraction of atmospheric metres per second.").defineInRange("cloudSpeedScale", 0.25, 0, 1);
    public static final ForgeConfigSpec.BooleanValue GLIDER_HUD = B.define("gliderHud", true);
    public static final ForgeConfigSpec.BooleanValue GLIDER_SOUND = B.define("gliderWindSound", true);
    public static final ForgeConfigSpec.DoubleValue GLIDER_SOUND_VOLUME = B.defineInRange("gliderWindVolume", .65, 0, 1);
    public static final ForgeConfigSpec.BooleanValue GLIDER_VISUALS = B.define("gliderBodyPose", true);
    public static final ForgeConfigSpec.BooleanValue TRANSITIONS = B.define("gliderTransitions", true);
    public static final ForgeConfigSpec.DoubleValue HUD_SCALE = B.defineInRange("gliderHudScale", 1.0, .5, 2);
    public static final ForgeConfigSpec.IntValue HUD_X = B.defineInRange("gliderHudX", 8, 0, 2000);
    public static final ForgeConfigSpec.IntValue HUD_Y = B.defineInRange("gliderHudY", 8, 0, 2000);
    public static final ForgeConfigSpec.BooleanValue RAIN_WIND = B.define("windRain", true);
    public static final ForgeConfigSpec.BooleanValue SNOW_WIND = B.define("windSnow", true);
    public static final ForgeConfigSpec.DoubleValue RAIN_STRENGTH = B.defineInRange("rainWindStrength", 1.0, 0, 3);
    public static final ForgeConfigSpec.DoubleValue SNOW_STRENGTH = B.defineInRange("snowWindStrength", 1.0, 0, 3);
    public static final ForgeConfigSpec.DoubleValue RAIN_ANGLE = B.defineInRange("rainMaxAngle", 25.0, 0, 45);
    public static final ForgeConfigSpec.DoubleValue SNOW_ANGLE = B.defineInRange("snowMaxAngle", 35.0, 0, 45);
    public static final ForgeConfigSpec.BooleanValue SAND = B.define("sandstorms", true);
    public static final ForgeConfigSpec.DoubleValue SAND_START = B.defineInRange("sandstormStartSpeed", 7.0, 0.1, 60);
    public static final ForgeConfigSpec.DoubleValue SAND_STOP = B.comment("Effective stop threshold is capped below start speed.").defineInRange("sandstormStopSpeed", 5.0, 0, 59);
    public static final ForgeConfigSpec.IntValue SAND_START_TICKS = B.defineInRange("sandstormStartTicks", 100, 1, 2400);
    public static final ForgeConfigSpec.IntValue SAND_STOP_TICKS = B.defineInRange("sandstormStopTicks", 60, 1, 2400);
    public static final ForgeConfigSpec.IntValue SAND_FADE = B.defineInRange("sandstormFadeTicks", 80, 1, 1200);
    public static final ForgeConfigSpec.IntValue SAND_BUDGET = B.defineInRange("sandstormMaxParticles", 120, 0, 500);
    public static final ForgeConfigSpec.DoubleValue SAND_DENSITY = B.defineInRange("sandstormDensity", 1.0, 0, 2);
    public static final ForgeConfigSpec.DoubleValue SAND_FOG = B.defineInRange("sandstormVisibilityReduction", 0.35, 0, 0.65);
    public static final ForgeConfigSpec SPEC = B.build();
    private WindVisualConfig() {}
}
