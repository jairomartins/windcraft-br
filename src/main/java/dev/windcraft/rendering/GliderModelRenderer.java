package dev.windcraft.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** One centered glider layer, sharing every torso transform with skin and armor. */
public final class GliderModelRenderer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private final GliderModel model = new GliderModel();
    public GliderModelRenderer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) { super(parent); }
    @Override public void submit(PoseStack pose, SubmitNodeCollector collector, int light,
                                  AvatarRenderState state, float yaw, float pitch) {
        var frame = ((GliderVisualAccess)state).windcraft$frame();
        if (!frame.visible() || state.isSpectator || state.isInvisible) return;
        pose.pushPose();
        float width = .12f + .88f * frame.blend();
        pose.scale(width, 1, 1);
        var color=((GliderVisualAccess)state).windcraft$color();
        var texture=Identifier.parse("windcraft:textures/entity/glider/"+color+".png");
        collector.submitModel(model, state, pose, RenderTypes.entityCutout(texture), light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        pose.popPose();
    }
}
