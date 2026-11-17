package dev.windcraft.rendering;

import dev.windcraft.config.WindVisualConfig;
import dev.windcraft.glider.GliderFlightController;
import dev.windcraft.glider.GliderPhysics;
import dev.windcraft.item.WindGliderItem;
import dev.windcraft.network.WindNetwork;
import dev.windcraft.simulation.WindSample;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import java.util.Locale;

public final class GliderClient {
    private static WindNetwork.FlightSnapshot snapshot;
    private static WindNetwork.FlightSnapshot hudSnapshot;
    private static long receivedTick, sequence;
    private static ClientLevel world;
    private static WindNetwork.GliderInput previous;
    private static int sendAge;
    private static float bank, heading;
    private static boolean airborne;
    public static void install() {
        WindNetwork.flightReceiver = GliderClient::accept;
        GliderFlightController.clientTravel = GliderClient::travel;
        TickEvent.ClientTickEvent.Post.BUS.addListener(GliderClient::tick);
    }
    private static void accept(WindNetwork.FlightSnapshot message) {
        var level = Minecraft.getInstance().level;
        if (level == null || !message.dimension().equals(level.dimension().identifier().toString())) return;
        boolean newFlight = world != level || snapshot == null;
        if (world != level) hudSnapshot = null;
        if (newFlight) airborne = message.flightTicks() > 0 || (Minecraft.getInstance().player != null && !Minecraft.getInstance().player.onGround());
        if (newFlight || message.flightTicks() > 0 || !airborne) heading = message.heading();
        airborne |= message.flightTicks() > 0;
        bank=message.active() ? message.bank() : 0;
        world = level; receivedTick = level.getGameTime(); snapshot = message.active() ? message : null;
        if (message.active()) hudSnapshot = message;
        if (snapshot == null) { previous = null; airborne = false; heading = 0; }
    }
    private static GliderPhysics.Controls controls() {
        var mc = Minecraft.getInstance();
        if (mc.gui.screen() != null) return GliderPhysics.Controls.IDLE;
        return new GliderPhysics.Controls((mc.options.keyUp.isDown() ? 1 : 0) - (mc.options.keyDown.isDown() ? 1 : 0),
            (mc.options.keyRight.isDown() ? 1 : 0) - (mc.options.keyLeft.isDown() ? 1 : 0), mc.options.keyJump.isDown());
    }
    private static void tick(TickEvent.ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (mc.level != world || mc.player == null) { snapshot = hudSnapshot = null; world = null; previous = null; airborne = false; heading = bank = 0; GliderWindSound.clear(); return; }
        if (!mc.isPaused()) {
            var wind = snapshot == null ? new WindSample(0, 0) : new WindSample(snapshot.speed(), snapshot.direction());
            var v = mc.player.getDeltaMovement();
            double relativeSpeed = Math.sqrt(Math.pow((v.x - wind.xPerTick()) * 20, 2)
                + Math.pow((v.z - wind.zPerTick()) * 20, 2) + Math.pow(v.y * 20 - (snapshot == null ? 0 : snapshot.updraft()), 2));
            GliderWindSound.update(snapshot != null && GliderFlightController.eligible(mc.player)
                && mc.level.getGameTime() - receivedTick <= 40, relativeSpeed);
        }
        if (snapshot == null || mc.isPaused()) return;
        if (!airborne && mc.player.onGround()) heading = GliderPhysics.wrapHeading(mc.player.getYRot());
        var controls = controls();
        bank = !airborne && mc.player.onGround() ? 0 : GliderPhysics.updateBank(bank,controls.turn(),snapshot.settings());
        boolean close = mc.gui.screen() == null && mc.options.keyShift.isDown();
        var next = new WindNetwork.GliderInput(controls.forward(), controls.turn(), controls.flare(), close, sequence);
        boolean changed = previous == null || previous.forward() != next.forward() || previous.turn() != next.turn()
            || previous.flare() != next.flare() || previous.close() != next.close();
        if (changed || ++sendAge >= 5) {
            sequence++; sendAge = 0;
            WindNetwork.sendInput(new WindNetwork.GliderInput(next.forward(), next.turn(), next.flare(), close, sequence));
            previous = next;
        }
    }
    private static boolean travel(Player player) {
        var mc = Minecraft.getInstance();
        if (mc.player != player || mc.level != world || snapshot == null) return false;
        if (!GliderFlightController.eligible(player) || !(player.getMainHandItem().getItem() instanceof WindGliderItem
            || player.getOffhandItem().getItem() instanceof WindGliderItem)) return false;
        long elapsed = mc.level.getGameTime() - receivedTick;
        if (elapsed < 0 || elapsed > 40) { snapshot = null; return false; }
        var conditions = new GliderPhysics.Conditions(new WindSample(snapshot.speed(), snapshot.direction()), snapshot.updraft(), snapshot.tick() + elapsed, snapshot.seed());
        var input = controls();
        // Flare activation is confirmed by the server and expires after its short impulse.
        var predicted = new GliderPhysics.Controls(input.forward(), input.turn(), snapshot.flare() && elapsed < 5);
        if (!airborne) { heading = GliderPhysics.wrapHeading(player.getYRot()); airborne = true; }
        var v = player.getDeltaMovement();
        heading = GliderPhysics.updateHeading(heading, bank, new GliderPhysics.Velocity(v.x, v.y, v.z), conditions, snapshot.settings());
        var next = GliderFlightController.calculate(player, conditions, predicted, snapshot.settings(), bank, heading);
        if (GliderPhysics.safeLanding(snapshot.flightTicks() + (int)elapsed, next.y())) player.resetFallDistance();
        GliderFlightController.move(player, next); return true;
    }
    public static float predictedHeading() { return heading; }
    public static float predictedBank() { return bank; }
    public static void hud(GuiGraphicsExtractor graphics) {
        if (snapshot == null && !GliderVisuals.local(1).visible()) hudSnapshot = null;
        GliderFlightHud.render(graphics, hudSnapshot);
    }
}
