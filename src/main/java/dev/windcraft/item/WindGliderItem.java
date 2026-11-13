package dev.windcraft.item;

import dev.windcraft.glider.GliderFlightController;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class WindGliderItem extends Item {
    public WindGliderItem(Properties properties) { super(properties); }

    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            player.startUsingItem(hand);
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 10);
            return InteractionResult.CONSUME;
        }
        if (player instanceof ServerPlayer server && GliderFlightController.isDeployed(server)) {
            GliderFlightController.stop(server);
            player.startUsingItem(hand);
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 10);
            return InteractionResult.CONSUME;
        }
        if (!GliderFlightController.canDeploy(player)) {
            if (player instanceof ServerPlayer) player.sendOverlayMessage(Component.translatable("message.windcraft.glider.airborne"));
            return InteractionResult.FAIL;
        }
        if (player instanceof ServerPlayer server && !GliderFlightController.start(server, hand)) {
            return InteractionResult.FAIL;
        }
        player.getCooldowns().addCooldown(player.getItemInHand(hand), 10);
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }
    @Override public int getUseDuration(ItemStack stack, net.minecraft.world.entity.LivingEntity user) { return 72000; }

    @Override public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level,
                                       net.minecraft.world.entity.Entity owner, net.minecraft.world.entity.EquipmentSlot slot) {
        int max = dev.windcraft.config.GliderConfig.DURABILITY.get();
        if (stack.getMaxDamage() != max) {
            stack.set(net.minecraft.core.component.DataComponents.MAX_DAMAGE, max);
            stack.setDamageValue(Math.min(stack.getDamageValue(), max - 1));
        }
    }
    @Override public void appendHoverText(ItemStack stack, Item.TooltipContext context,
        net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> lines,
        net.minecraft.world.item.TooltipFlag flag) {
        lines.accept(Component.translatable("tooltip.windcraft.glider.controls"));
        lines.accept(Component.translatable("tooltip.windcraft.glider.safety"));
    }
}
