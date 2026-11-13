package dev.windcraft.rendering;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraft.resources.Identifier;

@Mod.EventBusSubscriber(modid = "windcraft", value = Dist.CLIENT)
public final class GliderHudSetup {
    @SubscribeEvent public static void overlays(AddGuiOverlayLayersEvent event) {
        event.getLayeredDraw().add(Identifier.parse("windcraft:glider"), (graphics, delta) -> GliderClient.hud(graphics));
    }
}
