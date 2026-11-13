package dev.windcraft.rendering;

import dev.windcraft.config.WindVisualConfig;
import dev.windcraft.item.WindGliderItem;
import dev.windcraft.network.WindNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import java.util.*;

public final class GliderVisuals {
    private static final class Pilot {
        final GliderVisualState pose = new GliderVisualState();
        int id; boolean active; long received; Vec3 previous;
        float heading;
    }
    private static final Map<UUID, Pilot> PILOTS = new HashMap<>();
    private static ClientLevel world;
    public static void install() {
        WindNetwork.visualReceiver = GliderVisuals::receive;
        TickEvent.ClientTickEvent.Post.BUS.addListener(GliderVisuals::tick);
    }
    private static void receive(WindNetwork.FlightVisual packet) {
        var level = Minecraft.getInstance().level;
        if (level == null || !packet.dimension().equals(level.dimension().identifier().toString())) return;
        if (world != level) { clear(); world = level; }
        var pilot = PILOTS.computeIfAbsent(packet.player(), id -> new Pilot());
        if (pilot.id != packet.entityId()) { pilot.pose.reset(); pilot.previous = null; }
        pilot.id = packet.entityId(); pilot.active = packet.active(); pilot.received = level.getGameTime();
    }
    public static GliderVisualState.Frame frame(UUID id, float partial) {
        var pilot = PILOTS.get(id); return pilot == null ? GliderVisualState.Frame.NONE : pilot.pose.frame(partial);
    }
    public static GliderVisualState.Frame local(float partial) {
        var player = Minecraft.getInstance().player;
        return player == null ? GliderVisualState.Frame.NONE : frame(player.getUUID(), partial);
    }
    public static void clear() { PILOTS.clear(); world = null; GliderWindSound.clear(); }
    private static void tick(TickEvent.ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (mc.level != world || mc.player == null) { clear(); return; }
        if (mc.isPaused()) return;
        for (var iterator = PILOTS.entrySet().iterator(); iterator.hasNext();) {
            var entry = iterator.next(); var pilot = entry.getValue();
            if (!(world.getEntity(pilot.id) instanceof Player p) || !p.getUUID().equals(entry.getKey())) {
                if (world.getGameTime() - pilot.received > 40) iterator.remove();
                continue;
            }
            boolean active = pilot.active && world.getGameTime() - pilot.received < 30 && p.isAlive()
                && !p.onGround() && !p.isPassenger() && !p.isInWater() && !p.isInLava()
                && (p.getMainHandItem().getItem() instanceof WindGliderItem || p.getOffhandItem().getItem() instanceof WindGliderItem);
            if (!p.isAlive()) { pilot.pose.reset(); pilot.active = false; }
            Vec3 movement = p == mc.player ? p.getDeltaMovement() : pilot.previous == null ? Vec3.ZERO : p.position().subtract(pilot.previous);
            if (pilot.previous != null && p.position().distanceToSqr(pilot.previous) > 24 * 24) {
                pilot.pose.reset(); pilot.active = false; active = false;
            }
            float heading = movement.horizontalDistanceSqr() > .0001 ? (float)Math.toDegrees(Math.atan2(-movement.x, movement.z)) : p.getYRot();
            float turn = pilot.previous == null ? 0 : GliderVisualState.wrap(heading - pilot.heading) * 3;
            float pitch = (float)-Math.toDegrees(Math.atan2(movement.y, Math.max(.01, movement.horizontalDistance())));
            pilot.pose.tick(active, heading, pitch, turn, WindVisualConfig.TRANSITIONS.get());
            pilot.heading = heading; pilot.previous = p.position();
            if (!active && !pilot.pose.frame(1).visible() && world.getGameTime() - pilot.received > 40) iterator.remove();
        }
    }
}
