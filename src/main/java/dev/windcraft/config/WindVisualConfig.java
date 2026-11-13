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
    public static final ForgeConfigSpec SPEC = B.build();
    private WindVisualConfig() {}
}
