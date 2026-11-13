package dev.windcraft.rendering;

import dev.windcraft.item.WindGliderItem;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "windcraft", value = Dist.CLIENT)
public final class GliderRenderEvents {
    @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getModelTypes()) {
            var renderer = event.getPlayerRenderer(skin);
            if (renderer instanceof AvatarRenderer<?> avatar) avatar.addLayer(new GliderModelRenderer(avatar));
        }
    }
    @SubscribeEvent public static boolean hand(RenderHandEvent event) {
        return dev.windcraft.config.WindVisualConfig.GLIDER_VISUALS.get()
            && event.getItemStack().getItem() instanceof WindGliderItem && GliderVisuals.local(event.getPartialTick()).visible();
    }
}
