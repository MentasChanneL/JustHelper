package com.prikolz.justhelper.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.Config;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public abstract class JusthelperCommand {
    public static void register() {
        LiteralArgumentBuilder<FabricClientCommandSource> manager =
        ClientCommandManager.literal("justhelper")
                .then( ClientCommandManager.literal("reload_config" )
                        .executes(context -> {
                            context.getSource().sendFeedback(Text.literal("JustHelper > Перезагрузка конфига..."));
                            try {
                                Config.initialize();
                                if(Config.useCustomOutputClass) {
                                    if(Config.compileCustomOutputClass) Config.compileJava();
                                    Config.loadCustomOutClass();
                                }
                            }catch (Exception e) {
                                context.getSource().sendFeedback(
                                        Text.literal("Ошибка: ")
                                                .append(Text.literal(e.getMessage()))
                                                .setStyle(JustCommand.error)
                                );
                                e.printStackTrace();
                                return 0;
                            }
                            for(String msg : Config.messages) {
                                context.getSource().sendFeedback( Text.literal(msg) );
                            }
                            context.getSource().sendFeedback(Text.literal("JustHelper > Перезагрузка конфига выполнена!")
                                    .setStyle(JustCommand.sucsess));
                            return 1;
                        })
                )
                .then( ClientCommandManager.literal("save_default_config" )
                        .executes(context -> {
                            context.getSource().sendFeedback(Text.literal("JustHelper > Сохранение стандартного конфига..."));
                            try { Config.saveDefaultConfig(); } catch (Exception e) {
                                context.getSource().sendFeedback(Text.literal("JustHelper > Ошибка сохранения стандартного конфига! " + e.getMessage()).setStyle(JustCommand.error));
                                return 0;
                            }
                            context.getSource().sendFeedback(Text.literal("JustHelper > Стандартный конфиг сохранен!").setStyle(JustCommand.sucsess));
                            return 1;
                        })

                )
                .then( ClientCommandManager.literal("open_config")
                        .executes(context -> {
                            try{
                                Config.openConfig();
                            }catch (Exception e) {
                                context.getSource().sendFeedback(
                                        Text.literal("JustHelper > Ошибка открытия папки: " + e.getMessage()).setStyle(JustCommand.error)
                                );
                                return 0;
                            }
                            return 1;
                        })
                )
                .executes(context -> {
                    context.getSource().sendFeedback(
                            Text.literal("JustHelper > Аргументы:").setStyle(Style.EMPTY.withColor(Formatting.YELLOW))
                                    .append( Text.literal("\n\nreload_config - Перезагрузить конфиг.").setStyle(Style.EMPTY.withColor(Formatting.GOLD)) )
                                    .append( Text.literal("\n\nsave_default_config - Перезаписывает текущий конфиг на стандартный.").setStyle(Style.EMPTY.withColor(Formatting.GOLD)) )
                                    .append( Text.literal("\n\nopen_config - Открывает папку мода.").setStyle(Style.EMPTY.withColor(Formatting.GOLD)) )
                    );
                    return 1;
                })
                ;
        JustCommand.registerInDispacher(manager);
    }
}
