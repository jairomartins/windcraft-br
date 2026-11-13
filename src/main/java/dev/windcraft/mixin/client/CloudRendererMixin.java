package dev.windcraft.mixin.client;

import dev.windcraft.rendering.WindClient;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = CloudRenderer.class, remap = false)
public abstract class CloudRendererMixin {
    private static final String PREPARE = "prepare(ILnet/minecraft/client/CloudStatus;FILnet/minecraft/world/phys/Vec3;JF)V";

    @ModifyVariable(method = PREPARE, at = @At("HEAD"), argsOnly = true)
    private Vec3 windcraft$camera(Vec3 camera) {
        return WindClient.cloudsActive ? WindClient.cloudCamera(camera) : camera;
    }
    @ModifyVariable(method = PREPARE, at = @At("HEAD"), argsOnly = true)
    private long windcraft$cancelVanillaDrift(long ticks) {
        return WindClient.cloudsActive ? 0 : ticks;
    }
    @ModifyVariable(method = PREPARE, at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private float windcraft$cancelVanillaPartial(float partial) {
        return WindClient.cloudsActive ? 0 : partial;
    }
}

