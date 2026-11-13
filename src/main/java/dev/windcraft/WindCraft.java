package dev.windcraft;
import dev.windcraft.commands.WindCommands;
import dev.windcraft.config.WindConfig;
import dev.windcraft.network.WindNetwork;
import dev.windcraft.simulation.WindService;
import dev.windcraft.item.AnemometerItem;
import dev.windcraft.item.WindGliderItem;
import dev.windcraft.glider.GliderFlightController;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;
import net.minecraftforge.event.*;
import net.minecraftforge.event.server.ServerStoppedEvent;
@Mod(WindCraft.ID)
public final class WindCraft {
    public static final String ID = "windcraft";
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    public static final RegistryObject<Item> ANEMOMETER = ITEMS.register("anemometer", () -> new AnemometerItem(new Item.Properties().setId(ITEMS.key("anemometer")).stacksTo(1)));
    public static final RegistryObject<Item> WIND_GLIDER = ITEMS.register("wind_glider", () -> new WindGliderItem(new Item.Properties().setId(ITEMS.key("wind_glider")).durability(256)));
    public WindCraft(FMLJavaModLoadingContext context) {
        ITEMS.register(context.getModBusGroup());
        context.registerConfig(ModConfig.Type.SERVER, WindConfig.SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, dev.windcraft.config.WindVisualConfig.SPEC);
        context.registerConfig(ModConfig.Type.SERVER, dev.windcraft.config.GliderConfig.SPEC, "windcraft-glider-server.toml");
        WindNetwork.init();
        RegisterCommandsEvent.BUS.addListener(WindCommands::register);
        TickEvent.ServerTickEvent.Post.BUS.addListener(WindService::tick);
        TickEvent.PlayerTickEvent.Pre.BUS.addListener(GliderFlightController::tick);
        net.minecraftforge.event.entity.player.PlayerEvent.StartTracking.BUS.addListener(e -> {
            if (e.getEntity() instanceof net.minecraft.server.level.ServerPlayer observer
                && e.getTarget() instanceof net.minecraft.server.level.ServerPlayer pilot) WindNetwork.sendVisualTo(observer, pilot);
        });
        net.minecraftforge.event.entity.living.LivingFallEvent.BUS.addListener(GliderFlightController::land);
        net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent.BUS.addListener(e -> GliderFlightController.remove(e.getEntity()));
        net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent.BUS.addListener(e -> {
            if (e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p) { GliderFlightController.stop(p); GliderFlightController.remove(p); }
        });
        net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent.BUS.addListener(e -> {
            if (e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p) { GliderFlightController.stop(p); GliderFlightController.remove(p); }
        });
        ServerStoppedEvent.BUS.addListener(e -> { WindService.clear(); GliderFlightController.clear(); dev.windcraft.environment.UpdraftService.clear(); });
        BuildCreativeModeTabContentsEvent.BUS.addListener(e -> {
            if (e.getTabKey() == net.minecraft.world.item.CreativeModeTabs.TOOLS_AND_UTILITIES) {
                e.accept(ANEMOMETER); e.accept(WIND_GLIDER);
            }
        });
    }
}
