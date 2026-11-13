package dev.windcraft.environment;

import dev.windcraft.api.WindField;
import dev.windcraft.config.GliderConfig;
import dev.windcraft.simulation.WindEngine;
import dev.windcraft.simulation.WindService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.levelgen.Heightmap;
import java.util.*;

/** Bounded 16³-cell cache, forty-tick expiry, fixed sample count, no chunk loading. */
public final class UpdraftService {
    private record Cell(int x, int y, int z) {}
    private record Terrain(long tick, int ground, double heat, double lava, int east, int west, int north, int south) {}
    private static final Map<ServerLevel, Map<Cell, Terrain>> CACHE = new IdentityHashMap<>();
    private static final Set<UUID> DEBUG = new HashSet<>();
    public static boolean toggleDebug(UUID id) { if (DEBUG.remove(id)) return false; DEBUG.add(id); return true; }
    public static boolean debugging(UUID id) { return DEBUG.contains(id); }
    public static void remove(UUID id) { DEBUG.remove(id); }
    public static void clear() { CACHE.clear(); DEBUG.clear(); }
    private static int height(ServerLevel level, BlockPos p, int fallback) {
        return level.hasChunkAt(p) ? level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, p.getX(), p.getZ()) : fallback;
    }
    public static WindField sample(ServerLevel level, BlockPos pos) {
        var wind = WindService.sample(level, pos);
        double gust = WindEngine.gustIntensity(level.getSeed(), level.getGameTime());
        double turbulence = Math.clamp((wind.speed() - 12) / 18, 0, 1);
        if (!GliderConfig.THERMALS.get() || !level.hasChunkAt(pos) || !level.canSeeSky(pos)
            || level.dimensionType().hasCeiling() || !level.dimensionType().hasSkyLight()) return new WindField(wind, 0, gust, turbulence);
        var cells = CACHE.computeIfAbsent(level, l -> new HashMap<>());
        var cell = new Cell(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
        var terrain = cells.get(cell);
        long now = level.getGameTime();
        if (terrain == null || now - terrain.tick >= 40) {
            int ground = height(level, pos, pos.getY());
            double heat = Math.clamp(level.getBiome(pos).value().getBaseTemperature(), 0, 2);
            double lava = 0;
            for (int d : new int[]{2, 4, 6, 8}) {
                BlockPos below = pos.below(d);
                if (level.getFluidState(below).is(FluidTags.LAVA)) { lava = 1; break; }
            }
            terrain = new Terrain(now, ground, heat, lava, height(level, pos.east(12), ground), height(level, pos.west(12), ground),
                height(level, pos.north(12), ground), height(level, pos.south(12), ground));
            if (cells.size() >= 1024) cells.clear();
            cells.put(cell, terrain);
        }
        double altitude = pos.getY() - terrain.ground;
        double envelope = Math.clamp(1 - Math.max(0, altitude - 24) / 72.0, 0, 1);
        double sunlight = Math.max(0, Math.cos((level.getDefaultClockTime() % 24000 - 6000) / 24000.0 * Math.PI * 2));
        double heatLift = terrain.heat * sunlight * (1 - level.getRainLevel(1)) * .65;
        double slope = -(terrain.east - terrain.west) / 24.0 * wind.xPerTick()
            - (terrain.south - terrain.north) / 24.0 * wind.zPerTick();
        double ridgeLift = Math.clamp(slope, 0, 1);
        double strength = Math.clamp(heatLift + ridgeLift + terrain.lava, 0, 1);
        double vertical = GliderConfig.THERMAL_STRENGTH.get() * strength * envelope;
        return new WindField(wind, vertical, gust, turbulence);
    }
}
