package com.prikolz.justhelper.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.CommandBuffer;
import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.commands.argumens.FloorArgumentType;
import com.prikolz.justhelper.devdata.DescribeFloor;
import com.prikolz.justhelper.util.ClientUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class DescribeCommand {
    public static void register() {
        LiteralArgumentBuilder<FabricClientCommandSource> manager =
                ClientCommandManager.literal( Config.getCommandName("describe") )
                        .then(ClientCommandManager.argument("floor", new FloorArgumentType())
                                .then(ClientCommandManager.argument("desc", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            if(!ClientUtils.InDev()) {
                                                context.getSource().sendFeedback(
                                                        Text.literal("JustHelper > /describe доступен только в мире кодинга!").setStyle(JustCommand.error)
                                                );
                                                return 0;
                                            }
                                            int pos = FloorArgumentType.getParameter(context, "floor");
                                            String desc = StringArgumentType.getString(context, "desc");
                                            DescribeFloor.addDescribe(pos, desc);
                                            context.getSource().sendFeedback(
                                                    Text.literal("Добавлено описание для ").setStyle(JustCommand.success)
                                                            .append(Text.literal(String.valueOf(pos)).setStyle(JustCommand.white))
                                                            .append(Text.literal(" этажа: ").setStyle(JustCommand.success))
                                                            .append(Text.literal(desc.replaceAll("&", "§")).setStyle(JustCommand.white))
                                            );
                                            return 1;
                                        })
                                )
                                .executes(context -> {
                                    if(!ClientUtils.InDev()) {
                                        context.getSource().sendFeedback(
                                                Text.literal("JustHelper > /describe доступен только в мире кодинга!").setStyle(JustCommand.error)
                                        );
                                        return 0;
                                    }
                                    int pos = FloorArgumentType.getParameter(context, "floor");
                                    DescribeFloor.addDescribe(pos, "");
                                    context.getSource().sendFeedback(
                                            Text.literal("Удалено описание для ").setStyle(JustCommand.warn)
                                                    .append(Text.literal(String.valueOf(pos)).setStyle(JustCommand.white))
                                                    .append(Text.literal("этажа!").setStyle(JustCommand.warn))
                                    );
                                    return 1;
                                })
                        )
                        .executes(context -> {
                            context.getSource().sendFeedback(
                                    Text.literal("JustHelper > /describe [номер этажа] [Описание этажа ...]").setStyle(JustCommand.warn)
                            );
                            return 1;
                        })
                ;
        JustCommand.registerInDispacher(manager);
    }
}
