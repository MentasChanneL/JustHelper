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

public class EICDamage {
    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        var result = ClientCommandManager.literal("damage")
                .then(ClientCommandManager.literal("set")
                        .then(ClientCommandManager.argument("amount", IntegerArgumentType.integer(0))
                                .executes(context -> {
                                    if (EditItemCommand.msgItemIsNull(context)) return 0;
                                    ItemStack item = EditItemCommand.getItemMainHand();
                                    int amount = IntegerArgumentType.getInteger(context, "amount");
                                    setDamage(item, amount);
                                    context.getSource().sendFeedback(
                                            Text.literal("Установлено повреждение предмета: ").setStyle(JustCommand.warn)
                                                    .append(Text.literal("" + amount).setStyle(JustCommand.white))
                                    );
                                    return 1;
                                })
                        )
                )
                .then(ClientCommandManager.literal("set_max")
                        .then(ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    if (EditItemCommand.msgItemIsNull(context)) return 0;
                                    ItemStack item = EditItemCommand.getItemMainHand();
                                    int amount = IntegerArgumentType.getInteger(context, "amount");
                                    setMaxDamage(item, amount);
                                    context.getSource().sendFeedback(
                                            Text.literal("Установлена максимальная прочность: ").setStyle(JustCommand.warn)
                                                    .append(Text.literal("" + amount).setStyle(JustCommand.white))
                                    );
                                    return 1;
                                })
                        )
                )
                .executes(context -> {
                    if (EditItemCommand.msgItemIsNull(context)) return 0;
                    ItemStack item = EditItemCommand.getItemMainHand();
                    int has1 = getDamage(item);
                    int has2 = getMaxDamage(item);
                    context.getSource().sendFeedback(
                            Text.literal("Повреждение: ").setStyle(JustCommand.warn)
                                    .append(Text.literal(has1 + "").setStyle(JustCommand.white))
                    );
                    context.getSource().sendFeedback(
                            Text.literal("Максимальная прочность: ").setStyle(JustCommand.warn)
                                    .append(Text.literal(has2 + "").setStyle(JustCommand.white))
                    );
                    return 1;
                });
        return result;
    }

    private static int getDamage(ItemStack item) {
        Integer amount = item.get(DataComponentTypes.DAMAGE);
        if(amount == null) return 0;
        return amount;
    }

    private static int getMaxDamage(ItemStack item) {
        Integer amount = item.get(DataComponentTypes.MAX_DAMAGE);
        if(amount == null) return 0;
        return amount;
    }

    private static void setDamage(ItemStack item, int amount) {
        item.set(DataComponentTypes.DAMAGE, amount);
    }

    private static void setMaxDamage(ItemStack item, int amount) {
        item.set(DataComponentTypes.MAX_DAMAGE, amount);
    }
}