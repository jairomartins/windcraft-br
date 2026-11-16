package dev.windcraft.rendering;

import dev.windcraft.entity.WindMonk;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;

public final class WindMonkRenderer extends MobRenderer<WindMonk,VillagerRenderState,WindMonkModel> {
    private static final Identifier TEXTURE=Identifier.parse("windcraft:textures/entity/wind_monk.png");
    public WindMonkRenderer(EntityRendererProvider.Context context){super(context,new WindMonkModel(),.5f);}
    @Override public VillagerRenderState createRenderState(){return new VillagerRenderState();}
    @Override public Identifier getTextureLocation(VillagerRenderState state){return TEXTURE;}
}
