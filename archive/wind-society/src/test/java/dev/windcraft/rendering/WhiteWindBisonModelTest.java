package dev.windcraft.rendering;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WhiteWindBisonModelTest {
    @Test void modelBakesAndWalkUsesOppositeDiagonalPairs() {
        var model=new WhiteWindBisonModel();var s=new LivingEntityRenderState();
        s.walkAnimationSpeed=1;s.walkAnimationPos=0;s.ageInTicks=30;
        model.setupAnim(s);var root=model.root();
        float front=root.getChild("front_leg_left").xRot;
        assertEquals(front,root.getChild("back_leg_right").xRot,.0001);
        assertTrue(front*root.getChild("front_leg_right").xRot<0);
        assertEquals(root.getChild("front_leg_right").xRot,root.getChild("back_leg_left").xRot,.0001);
        assertNotNull(root.getChild("head").getChild("horn_left"));
        assertNotNull(root.getChild("head").getChild("beard"));
        assertNotNull(root.getChild("ceremonial_cloth"));
        s.walkAnimationSpeed=0;model.setupAnim(s);
        assertEquals(0,root.getChild("front_leg_left").xRot);
        assertNotEquals(0,root.getChild("tail").zRot);
        assertNotEquals(1,root.getChild("body").yScale);
    }
    @Test void hoovesStayAtGroundDuringSupportAndLiftOnlyDuringRecovery() {
        var model=new WhiteWindBisonModel();var s=new LivingEntityRenderState();
        s.walkAnimationSpeed=.3f;
        for(float cycle:new float[]{0,.1f,.3f,.5f,.63f,.64f,.82f,.99f,1}) {
            s.walkAnimationPos=cycle*(float)(Math.PI*2)/.42f;
            model.setupAnim(s);
            var leg=model.root().getChild("front_leg_left");
            float hoof=leg.y+12*(float)Math.cos(leg.xRot);
            if(cycle<=.64f || cycle==1) assertEquals(24,hoof,.0001);
            else assertTrue(hoof<24);
            float y=leg.y;model.setupAnim(s);assertEquals(y,leg.y);
        }
        s.walkAnimationSpeed=0;model.setupAnim(s);
        assertEquals(12,model.root().getChild("front_leg_left").y);
    }
    @Test void posesBlendContinuouslyAndStayBoundedWithoutAccumulating() {
        var model=new WhiteWindBisonModel();var s=new LivingEntityRenderState();
        for(float speed:new float[]{0,.01f,.15f,.3f,.45f,.6f,.85f,1}) {
            var w=BisonAnimations.weights(speed);
            assertEquals(1,w.idle()+w.walk()+w.run(),.00001);
            for(int frame=0;frame<100;frame++) {
                s.walkAnimationSpeed=speed;s.walkAnimationPos=frame*.2f;
                model.setupAnim(s);
                var body=model.root().getChild("body");
                assertTrue(Math.abs(body.y-3)<=.241f);
                assertTrue(Math.abs(model.root().getChild("front_leg_left").xRot)<=.621f);
                float y=body.y;model.setupAnim(s);assertEquals(y,body.y);
            }
        }
        assertEquals(1,BisonAnimations.weights(0).idle());
        assertEquals(1,BisonAnimations.weights(.3f).walk());
        assertEquals(1,BisonAnimations.weights(1).run());
        var a=BisonAnimations.weights(.45f);var b=BisonAnimations.weights(.4501f);
        assertEquals(a.walk(),b.walk(),.001);
    }
}
