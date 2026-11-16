package dev.windcraft.rendering;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Stateless poses: vanilla's interpolated, distance-driven walk cycle belongs to each entity. */
public final class BisonAnimations {
    private BisonAnimations() {}

    public record Weights(float idle, float walk, float run) {}

    private static float smooth(float low, float high, float value) {
        float t=Math.clamp((value-low)/(high-low),0,1);
        return t*t*(3-2*t);
    }

    public static Weights weights(float speed) {
        float moving=smooth(0,.3f,speed);
        float running=smooth(.45f,.85f,speed);
        return new Weights(1-moving,moving*(1-running),moving*running);
    }

    private static void leg(ModelPart part,float phase,float amplitude,float stance,float lift) {
        if(amplitude==0) {
            part.xRot=0;
            return;
        }
        float cycle=(float)(phase/(Math.PI*2));
        cycle-= (float)Math.floor(cycle);
        float angle;
        float clearance=0;
        if(cycle<stance) {
            // Longer grounded sweep, followed by a shorter, eased recovery.
            angle=amplitude*(2*smooth(0,stance,cycle)-1);
        } else {
            float swing=(cycle-stance)/(1-stance);
            angle=amplitude*(1-2*smooth(0,1,swing));
            float arc=(float)Math.sin(Math.PI*swing);
            clearance=lift*arc*arc;
        }
        part.xRot=angle;
        // The hoof is 12 model pixels below its pivot. Compensate the rotation
        // during support so the rigid leg does not lift its hoof on every sweep.
        part.y+=12*(1-(float)Math.cos(angle))-clearance;
    }

    public static void apply(ModelPart root, LivingEntityRenderState s) {
        var w=weights(s.walkAnimationSpeed);
        float moving=w.walk()+w.run();
        // A slower distance-driven cadence; never use age to advance the feet.
        float phase=s.walkAnimationPos*.42f;
        float amplitude=.38f*w.walk()+.5f*w.run();
        float stance=.64f-.11f*w.run();
        float lift=.8f*w.walk()+1.4f*w.run();
        leg(root.getChild("front_leg_left"),phase,amplitude,stance,lift);
        leg(root.getChild("front_leg_right"),phase+(float)Math.PI,amplitude,stance,lift);
        leg(root.getChild("back_leg_left"),phase+(float)Math.PI,amplitude,stance,lift);
        leg(root.getChild("back_leg_right"),phase,amplitude,stance,lift);
        float bob=(float)Math.cos(phase*2)*(.16f*w.walk()+.24f*w.run());
        for(String name:new String[]{"body","ceremonial_cloth","harness","tail"})
            root.getChild(name).y+=bob;
        var hump=root.getChild("hump");
        hump.y+=bob;
        hump.zRot=(float)Math.sin(phase)*(.008f*w.walk()+.012f*w.run());
        var head=root.getChild("head");
        head.y+=bob*.65f;
        head.xRot=s.xRot*((float)Math.PI/180)
            +(float)Math.sin(s.ageInTicks*.045f)*.018f*w.idle()
            +(float)Math.sin(phase*2)*(.018f*w.walk()+.028f*w.run());
        head.yRot=s.yRot*((float)Math.PI/180);
        root.getChild("tail").zRot=(float)Math.sin(s.ageInTicks*.065f)*.045f
            +(float)Math.sin(phase)*.045f*moving;
        root.getChild("body").yScale=1+(float)Math.sin(s.ageInTicks*.045f)*.006f;
    }
}
