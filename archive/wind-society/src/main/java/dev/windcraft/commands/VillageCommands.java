package dev.windcraft.commands;

import dev.windcraft.village.*;
import dev.windcraft.api.WindAPI;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import static net.minecraft.commands.Commands.*;

public final class VillageCommands {
    public static void register(RegisterCommandsEvent event) {
        var root=literal("windvillage").requires(hasPermission(LEVEL_GAMEMASTERS));
        for(String module:new String[]{"portal","plaza","cell","tower","foundation"}) {
            var branch=literal(module);
            branch.then(literal("check").then(argument("origin",BlockPosArgument.blockPos()).executes(c->run(c.getSource(),module,BlockPosArgument.getLoadedBlockPos(c,"origin"),false))));
            branch.then(literal("place").then(argument("origin",BlockPosArgument.blockPos()).executes(c->run(c.getSource(),module,BlockPosArgument.getLoadedBlockPos(c,"origin"),true))));
            root.then(branch);
        }
        event.getDispatcher().register(root);
    }
    private static int run(CommandSourceStack source,String module,net.minecraft.core.BlockPos origin,boolean place) {
        var plan=VillagePlacement.plan(source.getLevel(),origin,VillageBlueprint.create(module));
        if(!plan.valid()){source.sendFailure(Component.literal(plan.error()));return 0;}
        if(place)VillagePlacement.place(source.getLevel(),plan);
        var wind=WindAPI.getWind(source.getLevel(),origin);
        source.sendSuccess(()->Component.literal((place?"Construido: ":"Area valida: ")+module+" | "+plan.blocks().size()+" blocos | "+wind.describe()+" | torre: saida leste (+X), plataforma a +24"),true);
        return 1;
    }
}
