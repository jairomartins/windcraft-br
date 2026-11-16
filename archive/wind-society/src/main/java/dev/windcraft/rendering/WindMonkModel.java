package dev.windcraft.rendering;

import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** Vanilla villager proportions/animation with an open hood, using only our own atlas. */
public final class WindMonkModel extends VillagerModel {
    public WindMonkModel(){super(createLayer().bakeRoot());}
    public static LayerDefinition createLayer() {
        var mesh=VillagerModel.createBodyModel();
        var hood=mesh.getRoot().getChild("head").addOrReplaceChild("hat",CubeListBuilder.create()
            .texOffs(64,0).addBox(-4.5f,-10.5f,-4.5f,9,2,9)
            .texOffs(64,0).addBox(-4.5f,-8.5f,-4.5f,1,9,9)
            .texOffs(64,0).addBox(3.5f,-8.5f,-4.5f,1,9,9)
            .texOffs(64,0).addBox(-3.5f,-8.5f,3.5f,7,9,1),PartPose.ZERO);
        // Replacing a PartDefinition retains its children; remove the vanilla wide rim.
        hood.addOrReplaceChild("hat_rim",CubeListBuilder.create(),PartPose.ZERO);
        return LayerDefinition.create(mesh,128,64);
    }
}
