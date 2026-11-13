package dev.windcraft.environment;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
/** 8x8x8 cells; bounded cache expires after two seconds. Never loads chunks. */
public final class ExposureCache {
    private record Cell(int x, int y, int z) {}
    private record Entry(long tick, double exposure, double biome) {}
    private final Map<Cell, Entry> cache = new LinkedHashMap<>();
    public double[] factors(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) return new double[]{0, 1};
        long now = level.getGameTime();
        Cell key = new Cell(pos.getX() >> 3, pos.getY() >> 3, pos.getZ() >> 3);
        Entry entry = cache.get(key);
        if (entry == null || now - entry.tick >= 40) {
            BlockPos center = new BlockPos(key.x * 8 + 4, key.y * 8, key.z * 8 + 4);
            int open = 0;
            for (int[] offset : new int[][]{{0,0},{3,0},{-3,0},{0,3},{0,-3}}) {
                BlockPos p = center.offset(offset[0], 0, offset[1]);
                if (level.hasChunkAt(p) && level.getHeight(Heightmap.Types.MOTION_BLOCKING, p.getX(), p.getZ()) <= p.getY()) open++;
            }
            String biome = level.getBiome(center).unwrapKey().map(k -> k.identifier().getPath()).orElse("");
            double multiplier = biome.contains("forest") || biome.contains("jungle") ? 0.65 : biome.contains("ocean") || biome.contains("beach") ? 1.3 : biome.contains("peak") || biome.contains("savanna") ? 1.15 : 1;
            entry = new Entry(now, open == 0 ? 0.05 : open / 5.0, multiplier);
            if (cache.size() >= 2048) cache.clear();
            cache.put(key, entry);
        }
        return new double[]{entry.exposure, entry.biome};
    }
    public int size() { return cache.size(); }
}
