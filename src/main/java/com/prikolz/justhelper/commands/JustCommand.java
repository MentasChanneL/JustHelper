package com.prikolz.justhelper.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class JustCommand {

    public static final Style error = Style.EMPTY.withColor(Formatting.RED);
    public static final Style warn = Style.EMPTY.withColor(Formatting.YELLOW);
    public static final Style success = Style.EMPTY.withColor(Formatting.GREEN);
    public static final Style white = Style.EMPTY.withColor(Formatting.WHITE);
    public static final Style gold = Style.EMPTY.withColor(Formatting.GOLD);
    public static final Style aqua = Style.EMPTY.withColor(Formatting.AQUA);

    public static void registerInDispacher(LiteralArgumentBuilder<FabricClientCommandSource> manager) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(manager));
    }

    public static void send(CommandContext<FabricClientCommandSource> context, Text text) {
        context.getSource().sendFeedback(text);
    }
}
