package dev.windcraft.rendering;

import dev.windcraft.config.WindVisualConfig;
import dev.windcraft.network.WindNetwork;
import dev.windcraft.simulation.WindSample;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.client.event.ViewportEvent;
import java.util.ArrayList;
import java.util.List;

/** Local presentation derived from the authoritative wind snapshot; no weather/world mutation. */
public final class EnvironmentalEffects {
    private static double x, z, previousX, previousZ, intensity, previousIntensity;
    private static EnvironmentalMotion.Storm storm = new EnvironmentalMotion.Storm();
    private static final List<Particle> dust = new ArrayList<>();
    private static final java.util.Map<List<BlockPos>, Boolean> roofCache = new java.util.HashMap<>();
    private static long ticks;
    private static boolean eligible;
    private static int sandColor = 0xD8BB80;
    private EnvironmentalEffects() {}
    public static void install() {
        ViewportEvent.RenderFog.BUS.addListener(e -> {
            if (e.getType() != FogType.NONE || intensity <= 0) return;
            float factor = (float)(1 - fogIntensity(e.getPartialTick()) * WindVisualConfig.SAND_FOG.get());
            e.scaleFarPlaneDistance(factor);
            e.scaleNearPlaneDistance(factor);
            // Forge 66.1.8 passes FogData by reference; its hook does not gate changes on cancellation.
        });
        ViewportEvent.ComputeFogColor.BUS.addListener(e -> {
            var mc = Minecraft.getInstance();
            if (mc.player == null || e.getCamera().getFluidInCamera() != FogType.NONE) return;
            float blend = (float)(fogIntensity(e.getPartialTick()) * WindVisualConfig.SAND_FOG.get() * .5);
            e.setRed(e.getRed() + (((sandColor >> 16) & 255) / 255f - e.getRed()) * blend);
            e.setGreen(e.getGreen() + (((sandColor >> 8) & 255) / 255f - e.getGreen()) * blend);
            e.setBlue(e.getBlue() + ((sandColor & 255) / 255f - e.getBlue()) * blend);
        });
    }
    private static double fogIntensity(float partial) {
        return WindVisualConfig.SAND.get() ? previousIntensity + (intensity - previousIntensity) * Math.clamp(partial, 0, 1) : 0;
    }
    public static double windX(float partial) { return previousX + (x - previousX) * Math.clamp(partial, 0, 1); }
    public static double windZ(float partial) { return previousZ + (z - previousZ) * Math.clamp(partial, 0, 1); }
    public static double time(float partial) { return ticks + partial; }
    public static void reset() {
        dust.forEach(Particle::remove); dust.clear();
        x = z = previousX = previousZ = intensity = previousIntensity = 0;
        roofCache.clear();
        ticks = 0; eligible = false; storm = new EnvironmentalMotion.Storm();
    }
    private static boolean desert(net.minecraft.client.multiplayer.ClientLevel level, BlockPos pos) {
        var biome = level.getBiome(pos);
        return biome.is(Biomes.DESERT) || biome.is(BiomeTags.IS_BADLANDS);
    }
    private static boolean sand(net.minecraft.client.multiplayer.ClientLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.is(Blocks.SAND) || state.is(Blocks.RED_SAND);
    }
    /** Conservative footprint guard cached for at most five client ticks. */
    public static boolean exposedPath(int cx, int cz, int bottom, double dx, double dz) {
        var level = Minecraft.getInstance().level;
        if (level == null) return false;
        var points = new ArrayList<BlockPos>();
        int steps = Math.max(1, (int)Math.ceil(Math.max(Math.abs(dx), Math.abs(dz)) * 2));
        for (int i = 0; i <= steps; i++) {
            double f = i / (double)steps;
            points.add(BlockPos.containing(cx + .5 + dx * f, bottom, cz + .5 + dz * f));
        }
        if (roofCache.size() >= 2048) roofCache.clear();
        return roofCache.computeIfAbsent(List.copyOf(points), key -> {
            for (var pos : key) if (!level.hasChunkAt(pos)
                || level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) > bottom) return false;
            return true;
        });
    }
    public static void tick(WindNetwork.Snapshot snapshot) {
        var mc = Minecraft.getInstance();
        var level = mc.level;
        if (level == null || mc.player == null) return;
        ticks++;
        if (ticks % 5 == 0) roofCache.clear();
        previousX = x; previousZ = z; previousIntensity = intensity;
        // Snapshot local speed already includes server biome, altitude and exposure.
        var wind = new WindSample(snapshot.speed(), snapshot.direction());
        x = EnvironmentalMotion.smooth(x, wind.xPerTick() * 20);
        z = EnvironmentalMotion.smooth(z, wind.zPerTick() * 20);
        var playerPos = mc.player.blockPosition();
        if (ticks % 5 == 1) {
            eligible = WindVisualConfig.SAND.get() && !level.dimensionType().hasCeiling()
                && level.dimensionType().hasSkyLight() && level.hasChunkAt(playerPos)
                && level.canSeeSky(playerPos) && desert(level, playerPos);
            if (eligible) {
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, playerPos.getX(), playerPos.getZ());
                var ground = new BlockPos(playerPos.getX(), y - 1, playerPos.getZ());
                eligible = Math.abs(playerPos.getY() - y) < 12 && sand(level, ground);
                sandColor = level.getBlockState(ground).is(Blocks.RED_SAND) ? 0xBF7545 : 0xD8BB80;
            }
        }
        intensity = storm.tick(snapshot.speed(), eligible && WindVisualConfig.SAND.get(),
            WindVisualConfig.SAND_START.get(), WindVisualConfig.SAND_STOP.get(),
            WindVisualConfig.SAND_START_TICKS.get(), WindVisualConfig.SAND_STOP_TICKS.get(), WindVisualConfig.SAND_FADE.get());
        if (!WindVisualConfig.SAND.get() || !snapshot.particles()) {
            dust.forEach(Particle::remove); dust.clear(); return;
        }
        int radius = Math.min(20, Math.max(4, mc.options.getEffectiveRenderDistance() * 16));
        dust.removeIf(p -> {
            if (!p.isAlive()) return true;
            var pos = BlockPos.containing(p.getPos());
            if (p.getPos().distanceToSqr(mc.player.position()) > radius * radius * 2
                || !level.hasChunkAt(pos) || !level.canSeeSky(pos)) { p.remove(); return true; }
            p.setParticleSpeed(x / 20 * .65, -.015, z / 20 * .65);
            return false;
        });
        var status = mc.options.particles().get();
        int budget = status == ParticleStatus.MINIMAL ? 0 : WindVisualConfig.SAND_BUDGET.get() / (status == ParticleStatus.DECREASED ? 3 : 1);
        while (dust.size() > budget) dust.removeLast().remove();
        int attempts = Math.min(6, (int)Math.ceil(intensity * WindVisualConfig.SAND_DENSITY.get() * 4));
        for (int i = 0; i < attempts && dust.size() < budget; i++) {
            var random = level.getRandom();
            double px = mc.player.getX() + (random.nextDouble() * 2 - 1) * radius;
            double pz = mc.player.getZ() + (random.nextDouble() * 2 - 1) * radius;
            var column = BlockPos.containing(px, mc.player.getY(), pz);
            if (!level.hasChunkAt(column)) continue;
            int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING, column.getX(), column.getZ());
            var base = new BlockPos(column.getX(), ground, column.getZ());
            if (Math.abs(ground - mc.player.getY()) > 12 || !desert(level, base) || !sand(level, base.below())) continue;
            double py = ground + .15 + random.nextDouble() * (i % 3 == 0 ? 5 : .8);
            var pos = BlockPos.containing(px, py, pz);
            if (!level.canSeeSky(pos) || !level.getBlockState(pos).isAir()) continue;
            int color = level.getBlockState(base.below()).is(Blocks.RED_SAND) ? 0xBF7545 : 0xD8BB80;
            var p = mc.particleEngine.createParticle(new DustParticleOptions(color, i % 3 == 0 ? 2f : 1f), px, py, pz, 0, 0, 0);
            if (p != null) { p.setLifetime(35 + random.nextInt(25)); p.setParticleSpeed(x / 20 * .65, -.015, z / 20 * .65); dust.add(p); }
        }
    }
}
