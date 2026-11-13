package dev.windcraft.rendering;

import dev.windcraft.config.WindVisualConfig;
import dev.windcraft.network.WindNetwork;
import dev.windcraft.simulation.WindSample;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.text.NumberFormat;
import java.util.Locale;

public final class GliderFlightHud {
    private static final NumberFormat NUMBER = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR"));
    static { NUMBER.setMinimumFractionDigits(1); NUMBER.setMaximumFractionDigits(1); NUMBER.setGroupingUsed(false); }
    public static void render(GuiGraphicsExtractor graphics, WindNetwork.FlightSnapshot snapshot) {
        var mc = Minecraft.getInstance();
        if (snapshot == null || mc.player == null || mc.gui.hud.isHidden() || mc.getDebugOverlay().showDebugScreen()
            || !WindVisualConfig.GLIDER_HUD.get()) return;
        var frame = GliderVisuals.local(1);
        String phase = mc.player.onGround() && snapshot.flightTicks() == 0 ? "Preparado: salte para planar"
            : frame.phase() == GliderVisualState.Phase.OPENING ? "Abrindo"
            : frame.phase() == GliderVisualState.Phase.CLOSING ? "Fechando" : "Planador aberto";
        double speed = mc.player.getDeltaMovement().length() * 20;
        String first = phase + " | " + NUMBER.format(speed) + " m/s | Altitude Y " + Math.round(mc.player.getY()) + " m";
        String direction = new WindSample(snapshot.speed(), snapshot.direction()).cardinal();
        String vertical = snapshot.updraft() < 0 ? "Descendente " : "Ascendente +";
        String second = "Vento " + NUMBER.format(snapshot.speed()) + " m/s " + direction + " | " + vertical + NUMBER.format(snapshot.updraft()) + " m/s";
        int textWidth = Math.max(mc.font.width(first), mc.font.width(second));
        var layout = GliderHudLayout.fit(graphics.guiWidth(), graphics.guiHeight(), textWidth, 2,
            WindVisualConfig.HUD_SCALE.get(), WindVisualConfig.HUD_X.get(), WindVisualConfig.HUD_Y.get());
        graphics.pose().pushMatrix();
        graphics.pose().translate(layout.x(), layout.y());
        graphics.pose().scale(layout.scale(), layout.scale());
        graphics.fill(0, 0, layout.width(), layout.height(), 0xAD000000);
        graphics.text(mc.font, first, 6, 5, 0xFFFFE3B3);
        graphics.text(mc.font, second, 6, 16, snapshot.updraft() > 2 ? 0xFF9EE8B6 : 0xFFF2F2F2);
        graphics.pose().popMatrix();
    }
}
