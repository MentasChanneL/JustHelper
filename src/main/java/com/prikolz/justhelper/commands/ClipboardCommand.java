package com.prikolz.justhelper.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.vars.Texts;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public abstract class ClipboardCommand {
    public static void register() {
        LiteralArgumentBuilder<FabricClientCommandSource>  manager =
                ClientCommandManager.literal( Config.getCommandName("clipboard") )
                .then( ClientCommandManager.literal("+clip" )
                        .executes(context -> {
                            String err = Texts.run(true);
                            if(err.startsWith("> ")) {
                                context.getSource().sendFeedback(Text.literal("JustHelper " + err).setStyle(Style.EMPTY.withColor(Formatting.RED)));
                                return 0;
                            }
                            context.getSource().sendFeedback(
                                    Text.literal("JustHelper > Получен текст из буфера обмена").setStyle(Style.EMPTY.withColor(Formatting.YELLOW))
                            );
                            return 1;
                        })

                )
                .executes(context -> {
                    String err = Texts.run(false);
                    if(err.startsWith("> ")) {
                        context.getSource().sendFeedback(Text.literal("JustHelper " + err).setStyle(Style.EMPTY.withColor(Formatting.RED)));
                        return 0;
                    }
                    context.getSource().sendFeedback(
                            Text.literal("JustHelper > Получен текст из буфера обмена").setStyle(Style.EMPTY.withColor(Formatting.YELLOW))
                    );
                    return 1;
                })
        ;
        JustCommand.registerInDispacher(manager);
    }
}
