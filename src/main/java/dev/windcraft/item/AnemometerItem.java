package dev.windcraft.item;
import dev.windcraft.api.WindAPI;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
public final class AnemometerItem extends Item {
    public AnemometerItem(Properties properties) { super(properties); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel server) player.sendOverlayMessage(Component.literal(WindAPI.getWind(server, player.blockPosition()).describe()));
        return InteractionResult.SUCCESS;
    }
}
