package dev.windcraft.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.windcraft.item.WindGliderItem;
import dev.windcraft.rendering.*;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AvatarRenderer.class, remap = false)
public abstract class GliderAvatarRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void windcraft$extract(Avatar entity, AvatarRenderState state, float partial, CallbackInfo ci) {
        var frame = dev.windcraft.config.WindVisualConfig.GLIDER_VISUALS.get() && entity instanceof Player p && p.isAlive()
            ? GliderVisuals.frame(p.getUUID(), partial) : GliderVisualState.Frame.NONE;
        ((GliderVisualAccess)state).windcraft$frame(frame);
        ((GliderVisualAccess)state).windcraft$color(entity instanceof Player p ? GliderVisuals.color(p.getUUID()) : "orange");
        if (!frame.visible()) return;
        state.bodyRot += GliderVisualState.wrap(frame.yaw() - state.bodyRot) * frame.blend();
        state.walkAnimationSpeed *= 1 - frame.blend(); state.swingAnimation *= 1 - frame.blend();
        state.isCrouching = false; state.isUpsideDown = false; state.isUsingItem = false;
        // Extracted states only: equipped stacks/inventory are never modified.
        if (state.rightHandItemStack.getItem() instanceof WindGliderItem) state.rightHandItemState.clear();
        if (state.leftHandItemStack.getItem() instanceof WindGliderItem) state.leftHandItemState.clear();
    }
    @Inject(method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V", at = @At("TAIL"))
    private void windcraft$body(AvatarRenderState state, PoseStack pose, float rotation, float scale, CallbackInfo ci) {
        var frame = ((GliderVisualAccess)state).windcraft$frame();
        if (!frame.visible()) return;
        pose.translate(0, .7f * frame.blend(), .25f * frame.blend());
        pose.rotateDegrees(Axis.XP, (-82 - frame.pitch() * .25f) * frame.blend());
        pose.rotateDegrees(Axis.YP, frame.bank() * frame.blend());
    }
}
