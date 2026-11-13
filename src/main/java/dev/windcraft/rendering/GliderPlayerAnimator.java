package dev.windcraft.rendering;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

/** Runs after native pose reset, for base skin and armor models alike. */
public final class GliderPlayerAnimator {
    public static void apply(HumanoidModel<?> model, AvatarRenderState state, GliderVisualState.Frame frame) {
        if (!frame.visible()) return;
        float b = frame.blend();
        joint(model.rightArm, 2.30f, 0, -.04f, b);
        joint(model.leftArm, 2.30f, 0, .04f, b);
        joint(model.rightLeg, -.04f, 0, -.025f + frame.bank() * .001f, b);
        joint(model.leftLeg, .04f, 0, .025f + frame.bank() * .001f, b);
        joint(model.body, 0, 0, 0, b);
        // Compensate the prone torso while keeping a bounded camera-relative look.
        joint(model.head, -.95f + Math.clamp(state.xRot, -30, 30) * .004f,
            Math.clamp(state.yRot, -40, 40) * (float)Math.PI / 180, 0, b);
    }
    private static void joint(ModelPart part, float x, float y, float z, float blend) {
        part.xRot += (x - part.xRot) * blend;
        part.yRot += (y - part.yRot) * blend;
        part.zRot += (z - part.zRot) * blend;
    }
}
