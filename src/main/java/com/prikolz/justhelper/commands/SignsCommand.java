package com.prikolz.justhelper.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.Sign;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public abstract class SignsCommand {

    public static void register() {
        LiteralArgumentBuilder<FabricClientCommandSource> manager =
            ClientCommandManager.literal("signs")
                .then(argument("text", StringArgumentType.greedyString())
                    .executes(context -> {
                        String search = StringArgumentType.getString(context, "text");
                        Sign.searchSigns(context.getSource(), search, false);
                        return 1;
                    })
                )
                .executes(context -> {
                    Sign.searchSigns(context.getSource(), "-", true);
                    return 1;
                });
        JustCommand.registerInDispacher(manager);
    }
}