package dev.windcraft.rendering;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

/** Same bamboo/sail design as the item, expressed in the player's model coordinates. */
public final class GliderModel extends EntityModel<AvatarRenderState> {
    public static final float BAR_Y = -4.66f, BAR_Z = 7.46f;
    public GliderModel() { super(makeRoot()); }
    private static net.minecraft.client.model.geom.ModelPart makeRoot() {
        var mesh = new MeshDefinition();
        var wood = CubeListBuilder.create().texOffs(0, 0)
            .addBox(-9, BAR_Y - .5f, BAR_Z - .5f, 18, 1, 1)
            .addBox(-.6f, -16, 15.5f, 1.2f, 25, 1.2f)
            .addBox(-24, 6, 15.5f, 48, 1, 1)
            .addBox(-5.5f, BAR_Y - .5f, BAR_Z + .5f, 1, 1, 8.1f)
            .addBox(4.5f, BAR_Y - .5f, BAR_Z + .5f, 1, 1, 8.1f);
        mesh.getRoot().addOrReplaceChild("frame", wood, PartPose.ZERO);
        return LayerDefinition.create(mesh, 16, 16).bakeRoot();
    }
}
