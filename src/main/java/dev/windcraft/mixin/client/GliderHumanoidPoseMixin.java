package dev.windcraft.mixin.client;

import dev.windcraft.rendering.GliderPlayerAnimator;
import dev.windcraft.rendering.GliderVisualAccess;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HumanoidModel.class, remap = false)
public abstract class GliderHumanoidPoseMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("TAIL"))
    private void windcraft$armsAndLegs(HumanoidRenderState state, CallbackInfo ci) {
        if (state instanceof AvatarRenderState avatar) {
            GliderPlayerAnimator.apply((HumanoidModel<?>)(Object)this, avatar, ((GliderVisualAccess)avatar).windcraft$frame());
        }
    }
}
