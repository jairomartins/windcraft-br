package dev.windcraft.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
    private static final Identifier WOOD = Identifier.parse("windcraft:textures/item/wind_glider_frame.png");
    private static final Identifier SAIL = Identifier.parse("windcraft:textures/item/wind_glider_sail.png");
    private final GliderModel model = new GliderModel();
    public GliderModelRenderer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) { super(parent); }
    @Override public void submit(PoseStack pose, SubmitNodeCollector collector, int light,
                                  AvatarRenderState state, float yaw, float pitch) {
        var frame = ((GliderVisualAccess)state).windcraft$frame();
        if (!frame.visible() || state.isSpectator || state.isInvisible) return;
        pose.pushPose();
        float width = .12f + .88f * frame.blend();
        pose.scale(width, 1, 1);
        collector.submitModel(model, state, pose, RenderTypes.entityCutout(WOOD), light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(SAIL), (transform, vertices) -> {
            vertex(vertices, transform, -1.5f, -1, 1.02f, 0, 0, light);
            vertex(vertices, transform, 1.5f, -1, 1.02f, 1, 0, light);
            vertex(vertices, transform, 1.5f, .5f, 1.02f, 1, 1, light);
            vertex(vertices, transform, -1.5f, .5f, 1.02f, 0, 1, light);
        });
        pose.popPose();
    }
    private static void vertex(VertexConsumer out, PoseStack.Pose pose, float x, float y, float z,
                               float u, float v, int light) {
        out.addVertex(pose, x, y, z).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light).setNormal(pose, 0, 0, 1);
    }
}
