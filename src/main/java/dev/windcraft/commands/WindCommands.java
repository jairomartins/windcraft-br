package dev.windcraft.commands;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import dev.windcraft.simulation.WindService;
import static net.minecraft.commands.Commands.*;
public final class WindCommands {
    public static void register(RegisterCommandsEvent e) {
        e.getDispatcher().register(literal("wind")
            .then(literal("info").executes(c -> info(c.getSource(), false)))
            .then(literal("debug").requires(hasPermission(LEVEL_GAMEMASTERS)).executes(c -> info(c.getSource(), true)))
            .then(literal("set").requires(hasPermission(LEVEL_GAMEMASTERS))
                .then(literal("speed").then(argument("value", DoubleArgumentType.doubleArg(0, 60)).executes(c -> set(c.getSource(), DoubleArgumentType.getDouble(c, "value"), true))))
                .then(literal("direction").then(argument("value", DoubleArgumentType.doubleArg(0, 360)).executes(c -> set(c.getSource(), DoubleArgumentType.getDouble(c, "value") % 360, false)))))
            .then(literal("reset").requires(hasPermission(LEVEL_GAMEMASTERS)).executes(c -> reset(c.getSource()))));
    }
    private static int info(CommandSourceStack s, boolean debug) {
        var level = s.getLevel();
        String text = WindService.sample(level, BlockPos.containing(s.getPosition())).describe();
        if (debug) {
            var field = dev.windcraft.api.WindAPI.getWindField(level, BlockPos.containing(s.getPosition()));
            text += String.format(java.util.Locale.ROOT, " | vertical=+%.2f m/s | rajada=%.2f | turbulencia=%.2f", field.verticalSpeed(), field.gust(), field.turbulence());
            text += " | cache=" + WindService.cacheSize(level) + " | tick=" + level.getGameTime();
            if (s.getEntity() instanceof net.minecraft.server.level.ServerPlayer p) {
                boolean on = dev.windcraft.environment.UpdraftService.toggleDebug(p.getUUID());
                text += on ? " | visualizacao de correntes: ON" : " | visualizacao de correntes: OFF";
            }
        }
        String result = text;
        s.sendSuccess(() -> Component.literal(result), false); return 1;
    }
    private static int set(CommandSourceStack s, double value, boolean speed) {
        var settings = WindService.settings(s.getLevel());
        double oldSpeed = settings.speed, oldDirection = settings.direction;
        if (speed) settings.speed = value; else settings.direction = value;
        return save(s, oldSpeed, oldDirection);
    }
    private static int reset(CommandSourceStack s) {
        var settings = WindService.settings(s.getLevel());
        double oldSpeed = settings.speed, oldDirection = settings.direction;
        settings.speed = settings.direction = -1;
        return save(s, oldSpeed, oldDirection);
    }
    private static int save(CommandSourceStack s, double oldSpeed, double oldDirection) {
        var settings = WindService.settings(s.getLevel());
        try { settings.save(); }
        catch (java.io.IOException e) { settings.speed = oldSpeed; settings.direction = oldDirection; s.sendFailure(Component.literal("Falha ao salvar vento: " + e.getMessage())); return 0; }
        s.sendSuccess(() -> Component.literal("WindCraft: configuração salva nesta dimensão."), true); return 1;
    }
}
