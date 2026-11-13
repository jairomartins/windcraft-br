package dev.windcraft.simulation;
import java.util.*;
import dev.windcraft.config.*;
import dev.windcraft.environment.ExposureCache;
import dev.windcraft.network.WindNetwork;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.TickEvent;
public final class WindService {
    private record State(WorldSettings settings, ExposureCache cache) {}
    private static final Map<ServerLevel, State> STATES = new IdentityHashMap<>();
    private static State state(ServerLevel l) { return STATES.computeIfAbsent(l, v -> new State(new WorldSettings(v), new ExposureCache())); }
    public static WorldSettings settings(ServerLevel l) { return state(l).settings; }
    public static int cacheSize(ServerLevel l) { return state(l).cache.size(); }
    public static WindSample sample(ServerLevel level, BlockPos pos) {
        if (level.dimensionType().hasCeiling() || !level.dimensionType().hasSkyLight()) return new WindSample(0, 0);
        State s = state(level);
        double[] f = s.cache.factors(level, pos);
        return WindEngine.sample(level.getSeed(), level.getGameTime(), (level.getDefaultClockTime() % 24000) / 24000.0,
            level.getRainLevel(1), level.getThunderLevel(1), f[1], pos.getY() - level.getSeaLevel(), f[0],
            WindConfig.BASE.get(), s.settings.speed, s.settings.direction);
    }
    public static void tick(TickEvent.ServerTickEvent.Post event) {
        if (event.server().getTickCount() % WindConfig.INTERVAL.get() != 0) return;
        for (var player : event.server().getPlayerList().getPlayers()) {
            var level = (ServerLevel)player.level();
            WindNetwork.send(player, sample(level, player.blockPosition()), atmosphere(level), WindConfig.PARTICLES.get());
            if (dev.windcraft.environment.UpdraftService.debugging(player.getUUID())) {
                var field = dev.windcraft.api.WindAPI.getWindField(level, player.blockPosition());
                for (int i = 0; i < 4 && field.verticalSpeed() > .05; i++) {
                    double angle = i * Math.PI / 2;
                    level.sendParticles(player, net.minecraft.core.particles.ParticleTypes.END_ROD, false, false,
                        player.getX() + Math.cos(angle) * 2, player.getY() + .2, player.getZ() + Math.sin(angle) * 2,
                        0, 0, Math.max(.02, field.verticalSpeed() / 20), 0, 1);
                }
            }
        }
    }
    /** Open-air reference, independent of the player's shelter; no world/block reads. */
    public static WindSample atmosphere(ServerLevel level) {
        if (level.dimensionType().hasCeiling() || !level.dimensionType().hasSkyLight()) return new WindSample(0, 0);
        var settings = settings(level);
        return WindEngine.sample(level.getSeed(), level.getGameTime(), (level.getDefaultClockTime() % 24000) / 24000.0,
            level.getRainLevel(1), level.getThunderLevel(1), 1, 128, 1,
            WindConfig.BASE.get(), settings.speed, settings.direction);
    }
    public static void clear() { STATES.clear(); }
}
