package dev.windcraft.rendering;

import dev.windcraft.entity.VillageEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;

@Mod.EventBusSubscriber(modid="windcraft",value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class VillageRenderers {
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(VillageEntities.BISON.get(),WhiteWindBisonRenderer::new);
    }
}
