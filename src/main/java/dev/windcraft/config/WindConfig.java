package dev.windcraft.config;
import net.minecraftforge.common.ForgeConfigSpec;
public final class WindConfig {
    private static final ForgeConfigSpec.Builder B = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.DoubleValue BASE = B.comment("Base speed in metres per second.").defineInRange("baseSpeed", 5.0, 0, 40);
    public static final ForgeConfigSpec.IntValue INTERVAL = B.comment("Sample/network interval in ticks.").defineInRange("updateTicks", 20, 5, 100);
    public static final ForgeConfigSpec.BooleanValue PARTICLES = B.define("particles", true);
    public static final ForgeConfigSpec SPEC = B.build();
    private WindConfig() {}
}
