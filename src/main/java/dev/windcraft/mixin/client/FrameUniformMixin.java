package dev.windcraft.mixin.client;

import dev.windcraft.rendering.WindClient;
import net.minecraft.client.renderer.GlobalSettingsUniform;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GlobalSettingsUniform.class, remap = false)
public abstract class FrameUniformMixin {
    @Inject(method = "update", at = @At("HEAD"))
    private void windcraft$frame(int width, int height, double glint, long time, float partial,
                                int blur, Vec3 camera, boolean rgss, CallbackInfo ci) {
        WindClient.prepareFrame(partial);
    }
}

