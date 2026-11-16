package dev.windcraft.village;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;

/** Explicit placement only. Preflight finishes before the first world mutation. */
public final class VillagePlacement {
    public record Plan(Map<BlockPos,BlockState> blocks, String error) { public boolean valid(){return error==null;} }
    private static BlockState state(VillageBlueprint.Material m) {
        return switch(m) {
            case STONE -> Blocks.STONE_BRICKS.defaultBlockState();
            case WOOD -> Blocks.SPRUCE_PLANKS.defaultBlockState();
            case LOG -> Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
            case SLAB -> Blocks.SPRUCE_SLAB.defaultBlockState();
            case STAIR -> Blocks.SPRUCE_STAIRS.defaultBlockState();
            case FENCE -> Blocks.SPRUCE_FENCE.defaultBlockState();
            case BLUE -> Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.LIGHT_BLUE).defaultBlockState();
            case WHITE -> Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.WHITE).defaultBlockState();
            case BEIGE -> Blocks.SMOOTH_SANDSTONE.defaultBlockState();
            case LIGHT -> Blocks.LANTERN.defaultBlockState();
            case HANGING_LIGHT -> Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true);
            case LADDER -> Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH);
            case BED_HEAD -> Blocks.BED.pick(net.minecraft.world.item.DyeColor.LIGHT_BLUE).defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.HEAD);
            case BED_FOOT -> Blocks.BED.pick(net.minecraft.world.item.DyeColor.LIGHT_BLUE).defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.FOOT);
        };
    }
    public static Plan plan(ServerLevel level,BlockPos origin,VillageBlueprint blueprint) {
        Map<BlockPos,BlockState> result=new LinkedHashMap<>();
        Map<Long,Integer> tops=new LinkedHashMap<>();
        for(var e:blueprint.blocks().entrySet()) {
            var c=e.getKey();var p=origin.offset(c.x(),c.y(),c.z());
            result.put(p,state(e.getValue()));
            long column=BlockPos.asLong(p.getX(),0,p.getZ());
            tops.merge(column,p.getY()+3,Math::max);
        }
        for(var column:tops.entrySet()) {
            var base=BlockPos.of(column.getKey()).atY(origin.getY());
            if(origin.getY()-12<level.getMinY() || column.getValue()>=level.getMaxY()) return new Plan(Map.of(),"Altura fora dos limites do mundo.");
            for(int y=origin.getY();y<=column.getValue();y++) {
                var p=base.atY(y);
                if(!level.getWorldBorder().isWithinBounds(p)) return new Plan(Map.of(),"Area fora da borda do mundo.");
                if(!level.hasChunkAt(p)) return new Plan(Map.of(),"Carregue toda a area antes de posicionar.");
                var old=level.getBlockState(p);
                if(!old.canBeReplaced() || !level.getFluidState(p).isEmpty() || level.getBlockEntity(p)!=null)
                    return new Plan(Map.of(),"Obstrucao em "+p.toShortString()+". Escolha uma area livre acima do terreno.");
            }
            // Every ground-floor tile receives a foundation; reject cliffs deeper than 12.
            if(!blueprint.blocks().containsKey(new VillageBlueprint.Cell(base.getX()-origin.getX(),0,base.getZ()-origin.getZ()))) continue;
            var p=base.below();int depth=0;
            while(depth<12) {
                if(!level.hasChunkAt(p) || !level.getFluidState(p).isEmpty() || level.getBlockEntity(p)!=null)
                    return new Plan(Map.of(),"Apoio inseguro em "+p.toShortString());
                var old=level.getBlockState(p);
                if(old.isSolidRender()) break;
                if(!old.canBeReplaced()) return new Plan(Map.of(),"Apoio obstruido em "+p.toShortString());
                result.put(p,Blocks.STONE_BRICKS.defaultBlockState());p=p.below();depth++;
            }
            if(depth==12) return new Plan(Map.of(),"Desnivel maior que 12 blocos. Escolha terreno mais regular.");
        }
        for(var p:result.keySet()) if(!level.getEntities(null,new AABB(p)).isEmpty())
            return new Plan(Map.of(),"Entidade na area de construcao em "+p.toShortString());
        return new Plan(Collections.unmodifiableMap(result),null);
    }
    public static void place(ServerLevel level,Plan plan) {
        if(!plan.valid())throw new IllegalArgumentException(plan.error());
        // Supports first, then floors, then decorations. Notify neighbors after all blocks exist.
        plan.blocks().entrySet().stream().sorted(Comparator.comparingInt(e->e.getKey().getY()))
            .forEach(e->level.setBlock(e.getKey(),e.getValue(),2));
        plan.blocks().forEach((p,s)->level.updateNeighborsAt(p,s.getBlock()));
    }
}
