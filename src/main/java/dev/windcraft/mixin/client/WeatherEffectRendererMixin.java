package dev.windcraft.mixin.client;

import dev.windcraft.config.WindVisualConfig;
import dev.windcraft.rendering.EnvironmentalEffects;
import dev.windcraft.rendering.EnvironmentalMotion;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

/** Changes only vanilla weather geometry; textures, precipitation selection and passes remain vanilla. */
@Mixin(value = WeatherEffectRenderer.class, remap = false)
public abstract class WeatherEffectRendererMixin {
    @Unique private int windcraft$vertex;
    @Unique private float windcraft$partial;
    @Unique private double windcraft$dx, windcraft$dz;
    @Inject(method = "extractRenderState", at = @At("HEAD"), require = 0)
    private void windcraft$partial(net.minecraft.client.multiplayer.ClientLevel level, float partial, Vec3 camera, WeatherRenderState state, CallbackInfo ci) {
        windcraft$partial = partial;
    }
    @Inject(method = "prepareInstances", at = @At("HEAD"), require = 0)
    private void windcraft$begin(VertexConsumer builder, List<WeatherEffectRenderer.ColumnInstance> columns, Vec3 camera, float alpha, int radius, float intensity, CallbackInfo ci) {
        windcraft$vertex = 0;
    }
    @Redirect(method = "prepareInstances", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;addVertex(FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"), require = 0)
    private VertexConsumer windcraft$tilt(VertexConsumer consumer, float vx, float vy, float vz,
        VertexConsumer builder, List<WeatherEffectRenderer.ColumnInstance> columns, Vec3 camera, float alpha, int radius, float intensity) {
        int vertex = windcraft$vertex++;
        boolean snow = alpha < .9f;
        if (!(snow ? WindVisualConfig.SNOW_WIND.get() : WindVisualConfig.RAIN_WIND.get())) return consumer.addVertex(vx, vy, vz);
        var column = columns.get(vertex / 4);
        if (vertex % 4 == 0) {
            double x = EnvironmentalEffects.windX(windcraft$partial), z = EnvironmentalEffects.windZ(windcraft$partial);
            double speed = Math.hypot(x, z);
            double variation = snow ? .88 + .12 * Math.sin(EnvironmentalEffects.time(windcraft$partial) / 35 + column.x() * .7 + column.z() * .4) : 1;
            double slope = EnvironmentalMotion.slope(speed, snow ? 5 : 20,
                snow ? WindVisualConfig.SNOW_STRENGTH.get() : WindVisualConfig.RAIN_STRENGTH.get(),
                snow ? WindVisualConfig.SNOW_ANGLE.get() : WindVisualConfig.RAIN_ANGLE.get()) * variation;
            // Bottom stays anchored to vanilla's exposed column. Upper edge leans upwind.
            windcraft$dx = speed > .001 ? -x / speed * slope * (column.topY() - column.bottomY()) : 0;
            windcraft$dz = speed > .001 ? -z / speed * slope * (column.topY() - column.bottomY()) : 0;
            if (!EnvironmentalEffects.exposedPath(column.x(), column.z(), column.bottomY(), windcraft$dx, windcraft$dz)) {
                windcraft$dx = windcraft$dz = 0;
            }
        }
        if (vertex % 4 < 2) { vx += (float)windcraft$dx; vz += (float)windcraft$dz; }
        return consumer.addVertex(vx, vy, vz);
    }
}
