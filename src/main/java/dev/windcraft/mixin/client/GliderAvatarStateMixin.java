package dev.windcraft.mixin.client;

import dev.windcraft.rendering.GliderVisualAccess;
import dev.windcraft.rendering.GliderVisualState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = AvatarRenderState.class, remap = false)
public abstract class GliderAvatarStateMixin implements GliderVisualAccess {
    @Unique private GliderVisualState.Frame windcraft$frame = GliderVisualState.Frame.NONE;
    @Unique private String windcraft$color="orange";
    public String windcraft$color() { return windcraft$color; }
    public void windcraft$color(String color) { windcraft$color=color; }
    public GliderVisualState.Frame windcraft$frame() { return windcraft$frame; }
    public void windcraft$frame(GliderVisualState.Frame frame) { windcraft$frame = frame; }
}
