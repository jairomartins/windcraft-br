package dev.windcraft.api;
import dev.windcraft.simulation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
/** Call on the logical server thread. Does not load chunks. */
public interface WindAPI {
    static WindSample getWind(ServerLevel level, BlockPos position) { return WindService.sample(level, position); }
    static WindField getWindField(ServerLevel level, BlockPos position) {
        return dev.windcraft.environment.UpdraftService.sample(level, position);
    }
}
