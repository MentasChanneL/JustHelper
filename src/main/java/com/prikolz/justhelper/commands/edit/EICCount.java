package com.prikolz.justhelper.commands.edit;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.commands.EditItemCommand;
import com.prikolz.justhelper.commands.JustCommand;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class EICCount {
    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        var result = ClientCommandManager.literal("count")
                .then(ClientCommandManager.literal("set")
                        .then(ClientCommandManager.argument("count", IntegerArgumentType.integer(1, 99))
                                .executes(context -> {
                                    if (EditItemCommand.msgItemIsNull(context)) return 0;
                                    ItemStack item = EditItemCommand.getItemMainHand();
                                    int amount = IntegerArgumentType.getInteger(context, "count");
                                    setCount(item, amount);
                                    context.getSource().sendFeedback(
                                            Text.literal("Установлено количество: ").setStyle(JustCommand.warn)
                                                    .append(Text.literal("" + amount).setStyle(JustCommand.white))
                                    );
                                    return 1;
                                })
                        )
                )
                .then(ClientCommandManager.literal("set_max")
                        .then(ClientCommandManager.argument("count", IntegerArgumentType.integer(1, 99))
                                .executes(context -> {
                                    if (EditItemCommand.msgItemIsNull(context)) return 0;
                                    ItemStack item = EditItemCommand.getItemMainHand();
                                    int amount = IntegerArgumentType.getInteger(context, "count");
                                    setMaxCount(item, amount);
                                    context.getSource().sendFeedback(
                                            Text.literal("Установлено максимальное количество: ").setStyle(JustCommand.warn)
                                                    .append(Text.literal("" + amount).setStyle(JustCommand.white))
                                    );
                                    return 1;
                                })
                        )
                );
        return result;
    }

    private static void setCount(ItemStack item, int amount) {
        item.setCount(amount);
    }

    private static void setMaxCount(ItemStack item, int amount) {
        item.set(DataComponentTypes.MAX_STACK_SIZE, amount);
    }
}
