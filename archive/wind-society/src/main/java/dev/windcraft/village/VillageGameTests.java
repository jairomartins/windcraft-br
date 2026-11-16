package dev.windcraft.village;

import dev.windcraft.entity.VillageEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.*;

/** Only discovered in development/GameTest runs, never during production worldgen. */
@GameTestNamespace("windcraft")
public final class VillageGameTests {
    public static void register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext context) {
        var tests=net.minecraftforge.registries.DeferredRegister.<java.util.function.Consumer<GameTestHelper>>create(
            net.minecraft.core.registries.Registries.TEST_FUNCTION,"windcraft");
        tests.register("foundation_and_bison",()->VillageGameTests::foundationAndBison);
        tests.register("rejects_unsafe_sites",()->VillageGameTests::rejectsUnsafeSitesWithoutWriting);
        tests.register(context.getModBusGroup());
    }
    @GameTest(structure="windcraft:village_test_space",maxTicks=100)
    public static void foundationAndBison(GameTestHelper helper) {
        var level=helper.getLevel();
        var origin=helper.absolutePos(new BlockPos(14,3,4));
        for(int x=-12;x<=40;x++)for(int z=-2;z<=38;z++)
            level.setBlock(origin.offset(x,-1,z),Blocks.STONE.defaultBlockState(),2);
        var blueprint=VillageBlueprint.create("foundation");
        var plan=VillagePlacement.plan(level,origin,blueprint);
        helper.assertTrue(plan.valid(),"Foundation preflight: "+plan.error());
        VillagePlacement.place(level,plan);
        helper.runAfterDelay(5,()->{
            helper.assertTrue(level.getBlockState(origin.offset(30,24,11)).is(Blocks.LADDER),"Tower ladder exit survives updates");
            helper.assertTrue(level.getBlockState(origin.offset(30,23,9)).is(Blocks.LANTERN),"Suspended light survives");
            helper.assertTrue(!VillagePlacement.plan(level,origin,blueprint).valid(),"Second placement must reject existing buildings");
            var bison=VillageEntities.BISON.get().create(level,EntitySpawnReason.COMMAND);
            helper.assertTrue(bison!=null,"Bison factory");
            bison.snapTo(origin.getX()+10.5,origin.getY()+1,origin.getZ()+10.5,0,0);
            bison.setHomeTo(origin.offset(10,1,10),8);
            helper.assertTrue(level.addFreshEntity(bison),"Bison inserted into world");
            var egg=new net.minecraft.world.item.ItemStack(VillageEntities.BISON_EGG.get());
            helper.assertTrue(net.minecraft.world.item.SpawnEggItem.getType(egg)==VillageEntities.BISON.get(),
                "Bison egg resolves exclusively to the registered bison type");
            var spawned=VillageEntities.BISON.get().spawn(level,egg,null,origin.offset(15,1,5),
                EntitySpawnReason.SPAWN_ITEM_USE,true,false);
            helper.assertTrue(spawned!=null && spawned.isAlive(),"Bison spawns through the egg item path");
            var monk=VillageEntities.MONK.get().create(level,EntitySpawnReason.COMMAND);
            helper.assertTrue(monk!=null,"Monk factory");
            monk.snapTo(origin.getX()+4.5,origin.getY()+1,origin.getZ()+4.5,0,0);
            monk.setTempleHome(origin.offset(6,1,4),12);
            helper.assertTrue(level.addFreshEntity(monk),"Monk inserted into world");
            var vanilla=net.minecraft.world.entity.EntityTypes.VILLAGER.create(level,EntitySpawnReason.COMMAND);
            helper.assertTrue(vanilla!=null && vanilla.getMaxHealth()==20,"Vanilla villager attributes preserved");
            helper.runAfterDelay(10,()->{
                // Let the entity establish its grounded state before pathfinding.
                var path=monk.getNavigation().createPath(origin.offset(8,1,4),0);
                helper.assertTrue(path!=null && path.canReach(),"Monk finds a walkable plaza route");
                helper.assertTrue(bison.isAlive()&&bison.getMaxHealth()==60,"Bison ticks with registered attributes");
                helper.assertTrue(monk.isAlive() && monk.getMaxHealth()==24 && monk.getTarget()==null,"Monk ticks peacefully");
                helper.assertTrue(monk.getHomePosition().equals(origin.offset(6,1,4)),"Temple home preserved through ticks");
                helper.succeed();
            });
        });
    }
    @GameTest(structure="windcraft:village_test_space")
    public static void rejectsUnsafeSitesWithoutWriting(GameTestHelper helper) {
        var level=helper.getLevel();var origin=helper.absolutePos(new BlockPos(5,16,5));
        var module=VillageBlueprint.create("plaza");
        var before=level.getBlockState(origin);
        helper.assertTrue(!VillagePlacement.plan(level,origin,module).valid(),"Unsupported platform rejected");
        helper.assertTrue(level.getBlockState(origin)==before,"Preflight never writes world blocks");
        level.setBlock(origin,Blocks.CHEST.defaultBlockState(),3);
        helper.assertTrue(!VillagePlacement.plan(level,origin,module).valid(),"Protected build rejected");
        helper.assertTrue(level.getBlockState(origin).is(Blocks.CHEST),"Chest preserved");
        helper.succeed();
    }
}
