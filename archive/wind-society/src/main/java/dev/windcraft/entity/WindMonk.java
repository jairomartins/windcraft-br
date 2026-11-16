package dev.windcraft.entity;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Separate peaceful NPC. No villager professions, trades or global entity hooks. */
public final class WindMonk extends PathfinderMob {
    public WindMonk(EntityType<? extends WindMonk> type, Level level) {
        super(type,level);
        setPersistenceRequired();
    }
    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,24)
            .add(Attributes.MOVEMENT_SPEED,.3).add(Attributes.FOLLOW_RANGE,24);
    }
    /** Future temple generation can assign a saved home without changing AI or vanilla villagers. */
    public void setTempleHome(BlockPos center,int radius) {
        setHomeTo(center.immutable(),Math.clamp(radius,4,64));
    }
    @Override public void tick() {
        super.tick();
        if(!level().isClientSide() && !hasHome()) setTempleHome(blockPosition(),24);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(1,new PanicGoal(this,1.2));
        goalSelector.addGoal(2,new MoveTowardsRestrictionGoal(this,1));
        goalSelector.addGoal(3,new MeditateGoal());
        goalSelector.addGoal(4,new WaterAvoidingRandomStrollGoal(this,.65));
        goalSelector.addGoal(5,new LookAtPlayerGoal(this,Player.class,8));
        goalSelector.addGoal(6,new LookAtPlayerGoal(this,WindMonk.class,6));
        goalSelector.addGoal(7,new RandomLookAroundGoal(this));
    }
    @Override public int getMaxFallDistance(){return 1;}
    public boolean isMeditationTime() {
        long time=Math.floorMod(level().getDefaultClockTime(),24000);
        return time>=13000 && time<23000;
    }
    private final class MeditateGoal extends Goal {
        MeditateGoal(){setFlags(EnumSet.of(Flag.MOVE));}
        @Override public boolean canUse(){return isMeditationTime() && hasHome() && isWithinHome() && !isInWater() && !isPanicking();}
        @Override public boolean canContinueToUse(){return canUse();}
        @Override public void start(){getNavigation().stop();}
    }
}
