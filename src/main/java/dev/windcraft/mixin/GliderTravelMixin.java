package dev.windcraft.mixin;

import dev.windcraft.glider.GliderFlightController;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Player.class, remap = false)
public abstract class GliderTravelMixin {
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void windcraft$glide(Vec3 input, CallbackInfo ci) {
        if (GliderFlightController.travel((Player)(Object)this)) ci.cancel();
    }
}
