package dev.windcraft.mixin;

import dev.windcraft.glider.GliderFlightController;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Exempt only server-validated gliding from vanilla's hovering timeout. */
@Mixin(value = ServerGamePacketListenerImpl.class, remap = false)
public abstract class GliderConnectionMixin {
    @Shadow public ServerPlayer player;
    @Shadow private boolean clientIsFloating;
    @Shadow private int aboveGroundTickCount;
    @Inject(method = "tick", at = @At("HEAD"))
    private void windcraft$allowThermalLift(CallbackInfo ci) {
        if (GliderFlightController.isFlying(player)) { clientIsFloating = false; aboveGroundTickCount = 0; }
    }
}
