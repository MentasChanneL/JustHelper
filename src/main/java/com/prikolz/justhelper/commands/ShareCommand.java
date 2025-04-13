package com.prikolz.justhelper.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.devdata.DevComment;
import com.prikolz.justhelper.devdata.Share;
import com.prikolz.justhelper.util.ClientUtils;
import com.prikolz.justhelper.util.DevWorld;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class ShareCommand {
    public static void register() {
        LiteralArgumentBuilder<FabricClientCommandSource> manager =
                ClientCommandManager.literal( Config.getCommandName("share") )
                        .executes(context -> {
                            if (!ClientUtils.InDev()) {
                                JustCommand.send(context, Text.literal("Команда доступна только в мире кода!").setStyle(JustCommand.error));
                                return 0;
                            }

                            var player = ClientUtils.getPlayer();

                            int i = -1;
                            for(int slot = 0; slot < 36; slot++) {
                                if(player.getInventory().getStack(slot).isEmpty()) {
                                    i = slot;
                                    break;
                                }
                            }
                            if(i == -1) {
                                JustCommand.send(context, Text.literal("Нет свободных слотов в инвентаре!").setStyle(JustCommand.error));
                                return 0;
                            }
                            var item = Share.getTemplate();
                            ClientUtils.getPlayer().getInventory().setStack(i, item);
                            JustCommand.send(context, Text.literal("Установите этот шаблон в любом месте кода, тогда другие игроки с Just Helper смогут видеть комментарии и описания этажей!").setStyle(JustCommand.success));
                            return 1;
                        })
                ;
        JustCommand.registerInDispacher(manager);
    }
}
