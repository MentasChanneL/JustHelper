package com.prikolz.justhelper.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.devdata.DevComment;
import com.prikolz.justhelper.util.ClientUtils;
import com.prikolz.justhelper.util.DevWorld;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class CommentCommand {
    public static void register() {
        LiteralArgumentBuilder<FabricClientCommandSource> manager =
                ClientCommandManager.literal( Config.getCommandName("comment") )
                        .then(ClientCommandManager.literal("remove")
                                .executes(context -> {
                                    var hit =  ClientUtils.crosshairHit();
                                    if (hit == null) {
                                        JustCommand.send(context, Text.literal("Строчка на перекрестье не найдена!").setStyle(JustCommand.warn));
                                        return 0;
                                    }
                                    int floor = DevWorld.mathFloor( (int) hit.y );
                                    int line = DevWorld.mathLine( (int) hit.z );
                                    int x = (int) hit.x;
                                    if (DevComment.remove(true, floor, line, x)) {
                                        JustCommand.send(context, Text.literal("Комментарий удален.").setStyle(JustCommand.success));
                                        return 1;
                                    }
                                    JustCommand.send(context, Text.literal("Комментарий на перекрестьи не найден!").setStyle(JustCommand.warn));
                                    return 0;
                                })
                        )
                        .then(ClientCommandManager.argument("scale", FloatArgumentType.floatArg(0.01f, 2f))
                                .then(ClientCommandManager.argument("text", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            var hit =  ClientUtils.crosshairHit();
                                            if (hit == null) {
                                                JustCommand.send(context, Text.literal("Строчка на перекрестье не найдена!").setStyle(JustCommand.warn));
                                                return 0;
                                            }
                                            float scale = FloatArgumentType.getFloat(context, "scale");
                                            String text = StringArgumentType.getString(context, "text");
                                            int floor = DevWorld.mathFloor( (int) hit.y );
                                            int line = DevWorld.mathLine( (int) hit.z );
                                            int x = (int) hit.x;
                                            DevComment.addComment(true, text, floor, line, x, scale);
                                            return 1;
                                        })
                                )
                        )
                        .executes(context -> {
                            context.getSource().sendFeedback(
                                    Text.literal("JustHelper > /comment [размер текста] [Комментарий ...]").setStyle(JustCommand.warn)
                            );
                            return 1;
                        })
                ;
        JustCommand.registerInDispacher(manager);
    }
}
