package dev.windcraft.glider;

import dev.windcraft.WindCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/** Server smoke checks: no dependency on archived settlement or client rendering. */
public final class FlightGameTests {
    public static void register(FMLJavaModLoadingContext context) {
        var tests=net.minecraftforge.registries.DeferredRegister.<java.util.function.Consumer<GameTestHelper>>create(
            net.minecraft.core.registries.Registries.TEST_FUNCTION,WindCraft.ID);
        tests.register("flight_registry_smoke",()->FlightGameTests::smoke);
        tests.register("glider_colors",()->FlightGameTests::colors);
        tests.register(context.getModBusGroup());
    }
    private static void colors(GameTestHelper helper) {
        var level=helper.getLevel();
        var manager=level.getServer().getRecipeManager();
        for(var dye:net.minecraft.world.item.DyeColor.values()) {
            String color=dye.getName();
            var wool=net.minecraft.world.level.block.Blocks.WOOL.pick(dye).asItem();
            var stick=net.minecraft.world.item.Items.STICK;
            var string=net.minecraft.world.item.Items.STRING;
            var bamboo=net.minecraft.world.item.Items.BAMBOO;
            var input=net.minecraft.world.item.crafting.CraftingInput.of(3,3,java.util.List.of(
                new net.minecraft.world.item.ItemStack(wool),new net.minecraft.world.item.ItemStack(bamboo),new net.minecraft.world.item.ItemStack(wool),
                new net.minecraft.world.item.ItemStack(wool),new net.minecraft.world.item.ItemStack(stick),new net.minecraft.world.item.ItemStack(wool),
                new net.minecraft.world.item.ItemStack(string),new net.minecraft.world.item.ItemStack(stick),new net.minecraft.world.item.ItemStack(string)));
            var result=manager.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,input,level);
            helper.assertTrue(result.isPresent(),"Recipe exists for "+color);
            var stack=result.orElseThrow().value().assemble(input);
            helper.assertTrue(stack.is(WindCraft.WIND_GLIDER.get()),"Recipe crafts glider");
            helper.assertTrue(dev.windcraft.item.GliderColors.of(stack).equals(color),"Craft preserves wool color "+color);
            var ops=level.registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
            var encoded=net.minecraft.world.item.ItemStack.CODEC.encodeStart(ops,stack).getOrThrow();
            var restored=net.minecraft.world.item.ItemStack.CODEC.parse(ops,encoded).getOrThrow();
            helper.assertTrue(dev.windcraft.item.GliderColors.of(restored).equals(color),"Saved item retains color "+color);
            var mixedItems=new java.util.ArrayList<>(input.items());
            mixedItems.set(0,new net.minecraft.world.item.ItemStack(
                net.minecraft.world.level.block.Blocks.WOOL.pick(dye==net.minecraft.world.item.DyeColor.WHITE ? net.minecraft.world.item.DyeColor.BLACK : net.minecraft.world.item.DyeColor.WHITE)));
            // Build a fresh input, since ingredient accounting is cached by CraftingInput.
            var mixed=net.minecraft.world.item.crafting.CraftingInput.of(3,3,mixedItems);
            helper.assertTrue(manager.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,mixed,level).isEmpty(),"Mixed wool colors are rejected");
        }
        helper.assertTrue(dev.windcraft.item.GliderColors.of(new net.minecraft.world.item.ItemStack(WindCraft.WIND_GLIDER.get())).equals("orange"),"Legacy glider defaults to orange");
        helper.succeed();
    }
    private static void smoke(GameTestHelper helper) {
        helper.assertTrue(!BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.parse("windcraft:wind_monk")),"Monk unregistered");
        helper.assertTrue(!BuiltInRegistries.ITEM.containsKey(Identifier.parse("windcraft:wind_monk_spawn_egg")),"Monk egg unregistered");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.parse("windcraft:wind_glider")),"Glider item retained");
        var level=helper.getLevel();
        helper.assertTrue(!BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.parse("windcraft:white_wind_bison")),"Bison unregistered");
        helper.assertTrue(!BuiltInRegistries.ITEM.containsKey(Identifier.parse("windcraft:white_wind_bison_spawn_egg")),"Bison egg unregistered");
        var pos=helper.absolutePos(new BlockPos(2,4,2));
        var wind=dev.windcraft.api.WindAPI.getWindField(level,pos);
        var settings=dev.windcraft.config.GliderConfig.settings();
        var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),level.registryAccess());
        try {
            var packet=new dev.windcraft.network.WindNetwork.FlightSnapshot("minecraft:overworld",true,8,90,3,
                level.getGameTime(),12,40,settings,false,50,17.5f);
            var codec=dev.windcraft.network.WindNetwork.flightCodec();
            codec.encode(buffer,packet);
            helper.assertTrue(packet.equals(codec.decode(buffer)),"Flight snapshot codec retains bank, settings and wind");
            helper.assertTrue(buffer.readableBytes()==0,"No unread flight packet fields");
            buffer.clear();
            var visual=new dev.windcraft.network.WindNetwork.FlightVisual(1,java.util.UUID.randomUUID(),"minecraft:overworld",true,-25,"blue");
            var visualCodec=dev.windcraft.network.WindNetwork.visualCodec();
            visualCodec.encode(buffer,visual);
            helper.assertTrue(visual.equals(visualCodec.decode(buffer)),"Observer packet retains authoritative bank");
        } finally { buffer.release(); }
        var v=GliderPhysics.step(new GliderPhysics.Velocity(0,-.15,.6),0,0,
            GliderPhysics.Controls.IDLE,new GliderPhysics.Conditions(wind.horizontal(),wind.verticalSpeed(),level.getGameTime(),1),settings,25);
        helper.assertTrue(Double.isFinite(v.y()) && Math.hypot(v.x(),v.z())<=settings.maxSpeed(),"Live WindAPI flight stays bounded");
        helper.succeed();
    }
}
