package com.prikolz.justhelper.commands.edit;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.commands.EditItemCommand;
import com.prikolz.justhelper.commands.JustCommand;
import com.prikolz.justhelper.commands.argumens.VariantsArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;

public class EICFireResistance {
    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        var result = ClientCommandManager.literal("fire_resistant")
                .then(ClientCommandManager.argument("enable", new VariantsArgumentType("Ожидалось true или false", false, "true", "false"))
                        .executes(context -> {
                            if (EditItemCommand.msgItemIsNull(context)) return 0;
                            ItemStack item = EditItemCommand.getItemMainHand();
                            boolean enable = VariantsArgumentType.getParameter(context, "enable").equals("true");
                            setFireResistant(item, enable);
                            String msg = enable ? "Включена" : "Выключена";
                            context.getSource().sendFeedback(
                                    Text.literal(msg).setStyle(JustCommand.white)
                                            .append(Text.literal(" неуязвимость к огню").setStyle(JustCommand.warn))
                            );
                            return 1;
                        })
                )
                .executes(context -> {
                    if (EditItemCommand.msgItemIsNull(context)) return 0;
                    ItemStack item = EditItemCommand.getItemMainHand();
                    boolean enable = hasFireResistant(item);
                    String msg = enable ? "Включено" : "Выключено";
                    context.getSource().sendFeedback(
                            Text.literal("Неуязвимость к огню: ").setStyle(JustCommand.warn)
                                    .append(Text.literal(msg).setStyle(JustCommand.white))
                    );
                    return 1;
                })
                ;
        return result;
    }

    private static void setFireResistant(ItemStack item, boolean enable) {
        if(enable) {
            item.set(DataComponentTypes.FIRE_RESISTANT, Unit.INSTANCE);
            return;
        }
        item.remove(DataComponentTypes.FIRE_RESISTANT);
    }

    private static boolean hasFireResistant(ItemStack item) {
        return item.get(DataComponentTypes.FIRE_RESISTANT) != null;
    }
}
