package dev.windcraft.rendering;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

/** Keep automatic mod-bus subscription separate from manually registered game events. */
@Mod.EventBusSubscriber(modid = "windcraft", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class WindClientSetup {
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> { WindClient.install(); EnvironmentalEffects.install(); GliderClient.install(); GliderVisuals.install(); });
    }
}
