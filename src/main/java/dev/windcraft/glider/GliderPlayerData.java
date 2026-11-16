package dev.windcraft.glider;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/** Ephemeral server state. Never persisted across logout, death or dimension changes. */
public final class GliderPlayerData {
    public final String dimension;
    public final InteractionHand hand;
    public final ItemStack stack;
    public int flightTicks;
    public double verticalSpeed;
    public GliderPhysics.Conditions conditions;
    public GliderPhysics.Settings settings;
    public final GliderState state = new GliderState();
    public float forward, turn;
    public float bank;
    public int flareTicks, flareCooldown;
    public boolean flareHeld;
    public boolean airborne;
    public long lastInputTick, lastSequence = -1;
    public net.minecraft.world.phys.Vec3 lastPosition;

    public GliderPlayerData(String dimension, InteractionHand hand, ItemStack stack, double verticalSpeed, GliderPhysics.Conditions conditions) {
        this.dimension = dimension; this.hand = hand; this.stack = stack;
        this.verticalSpeed = verticalSpeed; this.conditions = conditions;
        settings = dev.windcraft.config.GliderConfig.settings();
        state.open();
    }
}
