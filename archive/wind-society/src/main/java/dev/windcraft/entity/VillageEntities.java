package dev.windcraft.entity;

import dev.windcraft.WindCraft;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.*;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;

public final class VillageEntities {
    public static final DeferredRegister<EntityType<?>> TYPES=DeferredRegister.create(ForgeRegistries.ENTITY_TYPES,WindCraft.ID);
    public static final RegistryObject<EntityType<WhiteWindBison>> BISON=TYPES.register("white_wind_bison",()->
        EntityType.Builder.of(WhiteWindBison::new,MobCategory.CREATURE).sized(1.5f,2.25f).clientTrackingRange(10)
            .build(TYPES.key("white_wind_bison")));
    public static final RegistryObject<Item> BISON_EGG=WindCraft.ITEMS.register("white_wind_bison_spawn_egg",()->
        new SpawnEggItem(new Item.Properties().setId(WindCraft.ITEMS.key("white_wind_bison_spawn_egg")).spawnEgg(BISON.get())));
    public static void register(FMLJavaModLoadingContext context) {
        TYPES.register(context.getModBusGroup());
        EntityAttributeCreationEvent.BUS.addListener(e->{
            e.put(BISON.get(),WhiteWindBison.attributes().build());
        });
    }
}
