package com.prikolz.justhelper.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.CommandBuffer;
import com.prikolz.justhelper.Config;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class MultiMsgCommand {
    public static void register() {
        LiteralArgumentBuilder<FabricClientCommandSource> manager =
                ClientCommandManager.literal( Config.getCommandName("multimsg") )
                        .then(ClientCommandManager.argument("content", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String content = StringArgumentType.getString(context, "content");
                                    for(String msg : content.split("\\\\n")) {
                                        CommandBuffer.sendCommand("§" + msg);
                                    }
                                    Text.literal("JustHelper > Сообщения отправлены в буфер.").setStyle(JustCommand.aqua);
                                    return 1;
                                })
                        )
                        .executes(context -> {
                            context.getSource().sendFeedback(
                                    Text.literal("JustHelper > /multimsg [сообщение1]\n[сообщение2]\n[...").setStyle(JustCommand.warn)
                            );
                            return 1;
                        })
                ;
        JustCommand.registerInDispacher(manager);
    }
}
