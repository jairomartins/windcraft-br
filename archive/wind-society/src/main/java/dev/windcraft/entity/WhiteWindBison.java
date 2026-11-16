package dev.windcraft.entity;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Controlled spawn only; ground navigation never plans a drop larger than one block. */
public final class WhiteWindBison extends PathfinderMob {
    public WhiteWindBison(EntityType<? extends WhiteWindBison> type,Level level) {
        super(type,level);setPersistenceRequired();
    }
    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,60)
            .add(Attributes.MOVEMENT_SPEED,.16).add(Attributes.FOLLOW_RANGE,20)
            .add(Attributes.ATTACK_DAMAGE,7).add(Attributes.KNOCKBACK_RESISTANCE,.8);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(1,new MeleeAttackGoal(this,1.15,false));
        goalSelector.addGoal(2,new MoveTowardsRestrictionGoal(this,.8));
        goalSelector.addGoal(3,new WaterAvoidingRandomStrollGoal(this,.65));
        goalSelector.addGoal(4,new LookAtPlayerGoal(this,Player.class,8));
        goalSelector.addGoal(5,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
    }
    @Override public int getMaxFallDistance(){return 1;}
}
