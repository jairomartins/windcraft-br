package dev.windcraft.glider;

import dev.windcraft.api.WindAPI;
import dev.windcraft.config.GliderConfig;
import dev.windcraft.item.WindGliderItem;
import dev.windcraft.network.WindNetwork;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import java.util.*;
import java.util.function.Predicate;

public final class GliderFlightController {
    private static final Map<UUID, GliderPlayerData> FLIGHTS = new HashMap<>();
    public static Predicate<Player> clientTravel = player -> false;
    private GliderFlightController() {}
    public static boolean eligible(Player p) {
        return canDeploy(p) && !p.onGround();
    }
    public static boolean canDeploy(Player p) {
        return p.isAlive() && !p.isSpectator() && !p.getAbilities().flying
            && !p.isPassenger() && !p.isInWater() && !p.isInLava() && !p.onClimbable()
            && !p.isFallFlying() && !p.isSleeping() && !p.isInPowderSnow;
    }
    public static boolean isDeployed(ServerPlayer p) { return FLIGHTS.containsKey(p.getUUID()); }
    private static boolean equipped(Player p, GliderPlayerData data) {
        return p.getItemInHand(data.hand) == data.stack && !data.stack.isEmpty()
            && data.stack.getItem() instanceof WindGliderItem && !data.stack.isBroken();
    }
    public static boolean isFlying(ServerPlayer p) {
        var data = FLIGHTS.get(p.getUUID());
        return data != null && data.state.flying() && eligible(p) && equipped(p, data);
    }
    public static boolean managed(Player p, ItemStack stack) {
        var data = FLIGHTS.get(p.getUUID()); return data != null && data.stack == stack;
    }
    public static void toggle(ServerPlayer p, InteractionHand hand) {
        if (FLIGHTS.containsKey(p.getUUID())) { stop(p); return; }
        start(p, hand);
    }
    public static boolean start(ServerPlayer p, InteractionHand hand) {
        var stack = p.getItemInHand(hand);
        if (!canDeploy(p) || !(stack.getItem() instanceof WindGliderItem) || stack.isBroken() || FLIGHTS.containsKey(p.getUUID())) return false;
        stack.hurtAndBreak(1, p, hand);
        if (stack.isEmpty()) return false;
        var data = new GliderPlayerData(p.level().dimension().identifier().toString(), hand, stack,
            p.getDeltaMovement().y, sample(p));
        data.airborne = !p.onGround();
        data.lastInputTick = p.level().getGameTime(); data.lastPosition = p.position();
        FLIGHTS.put(p.getUUID(), data);
        WindNetwork.sendFlight(p, true, data.conditions, 0, data.settings, false);
        p.level().playSound(null, p.blockPosition(), SoundEvents.ARMOR_EQUIP_ELYTRA.value(), SoundSource.PLAYERS, .65f, .9f);
        return true;
    }
    public static void stop(ServerPlayer p) {
        var data = FLIGHTS.remove(p.getUUID());
        if (data == null || !data.state.flying()) return;
        data.state.close();
        WindNetwork.sendFlight(p, false, null, 0, data.settings, false);
        p.level().playSound(null, p.blockPosition(), SoundEvents.BAMBOO_HIT, SoundSource.PLAYERS, .6f, .8f);
    }
    public static void remove(Player p) {
        FLIGHTS.remove(p.getUUID());
        dev.windcraft.environment.UpdraftService.remove(p.getUUID());
    }
    public static void clear() { FLIGHTS.clear(); }

    /** Only axes/actions cross the network; never a requested velocity, position or wind. */
    public static void input(ServerPlayer p, WindNetwork.GliderInput input) {
        if (!Float.isFinite(input.forward()) || !Float.isFinite(input.turn()) || Math.abs(input.forward()) > 1 || Math.abs(input.turn()) > 1) return;
        var data = FLIGHTS.get(p.getUUID());
        if (data == null || !canDeploy(p) || !equipped(p, data) || input.sequence() <= data.lastSequence) return;
        long now = p.level().getGameTime();
        if (now == data.lastInputTick && data.lastSequence >= 0) return;
        data.lastSequence = input.sequence(); data.lastInputTick = now;
        if (input.close()) { stop(p); return; }
        data.forward = input.forward(); data.turn = input.turn();
        if (input.flare() && !data.flareHeld && data.flareCooldown == 0) {
            double speed = Math.hypot(p.getDeltaMovement().x, p.getDeltaMovement().z) * 20;
            if (speed >= 5) { data.flareTicks = 12; data.flareCooldown = 80; }
        }
        data.flareHeld = input.flare();
    }

    public static void tick(TickEvent.PlayerTickEvent.Pre event) {
        if (!(event.player() instanceof ServerPlayer p)) return;
        var data = FLIGHTS.get(p.getUUID());
        if (data == null) return;
        if (!p.isAlive() || !p.level().dimension().identifier().toString().equals(data.dimension)) { stop(p); remove(p); return; }
        if (!data.airborne && p.onGround() && canDeploy(p) && equipped(p, data)) {
            // Ready on the ground; no thrust, wear, exhaustion or gravity override until jumping.
            data.lastPosition = p.position();
            if (p.level().getGameTime() % 5 == 0) {
                data.conditions = sample(p);
                WindNetwork.sendFlight(p, true, data.conditions, 0, data.settings, false);
            }
            return;
        }
        data.airborne = true;
        if (data.state.flying() && (!eligible(p) || !equipped(p, data))) stop(p);
        if (data.state.phase() == GliderState.Phase.CLOSED) { FLIGHTS.remove(p.getUUID()); return; }
        if (!data.state.flying()) return;
        data.flightTicks++;
        if (data.flareTicks > 0) data.flareTicks--;
        if (data.flareCooldown > 0) data.flareCooldown--;
        if (p.level().getGameTime() - data.lastInputTick > 20) { data.forward = data.turn = 0; data.flareHeld = false; }
        // Allow ordinary packet batching and external impulses, reject grossly abnormal travel.
        double displacement = p.position().subtract(data.lastPosition).horizontalDistance();
        if (data.flightTicks > 10 && displacement > data.settings.maxSpeed() * 4 + 1 && !p.isInPostImpulseGraceTime()) {
            p.connection.teleport(data.lastPosition.x, data.lastPosition.y, data.lastPosition.z, p.getYRot(), p.getXRot());
            stop(p); return;
        }
        data.lastPosition = p.position();
        if (data.flightTicks % 5 == 0) {
            data.conditions = sample(p); data.settings = GliderConfig.settings();
            WindNetwork.sendFlight(p, true, data.conditions, data.flightTicks, data.settings, data.flareTicks > 0);
        }
        int wear = Math.max(5, GliderConfig.WEAR_TICKS.get() / (data.conditions.wind().speed() >= 14 ? 2 : 1));
        if (data.flightTicks % wear == 0) {
            data.stack.hurtAndBreak(1, p, data.hand);
            if (data.stack.isEmpty()) { stop(p); return; }
        }
        p.causeFoodExhaustion(.005f);
        if (data.flightTicks % 20 == 0) p.connection.send(new ClientboundSetEntityMotionPacket(p));
    }
    public static GliderPhysics.Conditions sample(ServerPlayer p) {
        var field = WindAPI.getWindField(p.level(), p.blockPosition());
        return new GliderPhysics.Conditions(field.horizontal(), field.verticalSpeed(), p.level().getGameTime(), p.getUUID().getLeastSignificantBits());
    }
    public static boolean travel(Player p) {
        if (p.level().isClientSide()) return clientTravel.test(p);
        if (!(p instanceof ServerPlayer server) || !isFlying(server)) return false;
        var data = FLIGHTS.get(p.getUUID());
        var conditions = new GliderPhysics.Conditions(data.conditions.wind(), data.conditions.updraft(), p.level().getGameTime(), data.conditions.seed());
        var controls = new GliderPhysics.Controls(data.forward, data.turn, data.flareTicks > 0);
        var next = calculate(p, conditions, controls, data.settings);
        data.verticalSpeed = next.y();
        if (GliderPhysics.safeLanding(data.flightTicks, next.y())) p.resetFallDistance();
        double speed = Math.hypot(next.x(), next.z());
        move(p, next);
        if (p.horizontalCollision && speed > .5) {
            data.stack.hurtAndBreak(4, p, data.hand);
            server.hurtServer(server.level(), p.damageSources().flyIntoWall(), (float)Math.min(8, speed * 8));
            stop(server);
        }
        return true;
    }
    public static GliderPhysics.Velocity calculate(Player p, GliderPhysics.Conditions conditions, GliderPhysics.Controls controls, GliderPhysics.Settings settings) {
        Vec3 v = p.getDeltaMovement();
        return GliderPhysics.step(new GliderPhysics.Velocity(v.x, v.y, v.z), p.getYRot(), p.getXRot(), controls, conditions, settings);
    }
    public static void move(Player p, GliderPhysics.Velocity velocity) {
        p.setDeltaMovement(velocity.x(), velocity.y(), velocity.z()); p.move(MoverType.SELF, p.getDeltaMovement());
    }
    public static void land(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p)) return;
        var data = FLIGHTS.get(p.getUUID());
        if (data != null && data.state.flying() && equipped(p, data) && GliderPhysics.safeLanding(data.flightTicks, data.verticalSpeed)) event.setDistance(0);
    }
}
