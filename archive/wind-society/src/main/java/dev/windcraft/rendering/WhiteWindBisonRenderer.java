package dev.windcraft.rendering;

import dev.windcraft.entity.WhiteWindBison;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public final class WhiteWindBisonRenderer extends MobRenderer<WhiteWindBison,LivingEntityRenderState,WhiteWindBisonModel> {
    private static final Identifier TEXTURE=Identifier.parse("windcraft:textures/entity/white_wind_bison.png");
    public WhiteWindBisonRenderer(EntityRendererProvider.Context context){super(context,new WhiteWindBisonModel(),.85f);}
    @Override public LivingEntityRenderState createRenderState(){return new LivingEntityRenderState();}
    @Override public Identifier getTextureLocation(LivingEntityRenderState state){return TEXTURE;}
}
