package com.prikolz.justhelper.commands.edit;

import com.mojang.authlib.properties.Property;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.commands.EditItemCommand;
import com.prikolz.justhelper.commands.JustCommand;
import com.prikolz.justhelper.commands.argumens.VariantsArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EICProfile {
    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        var result = ClientCommandManager.literal("profile")
                //.then(ClientCommandManager.literal("get")
                //        .executes(context -> {
                //            if (EditItemCommand.msgItemIsNull(context)) return 0;
                //            ItemStack item = EditItemCommand.getItemMainHand();
                //
                //            context.getSource().sendFeedback(
                //                    Text.literal(msg).setStyle(JustCommand.white)
                //                            .append(Text.literal(" неуязвимость к огню").setStyle(JustCommand.warn))
                //            );
                //            return 1;
                //        })
                //)
                .executes(context -> {
                    if (EditItemCommand.msgItemIsNull(context)) return 0;
                    ItemStack item = EditItemCommand.getItemMainHand();
                    context.getSource().sendFeedback( getProfile(item) );
                    DisplayEntity td = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, MinecraftClient.getInstance().world);
                    td.setPos(0, 0, 0);
                    MinecraftClient.getInstance().world.addEntity(td);
                    return 1;
                })
                ;
        return result;
    }

    private static Text getProfile(ItemStack item)  {
        ProfileComponent pc = item.get(DataComponentTypes.PROFILE);
        if(pc == null) return Text.literal("Предмет не имеет профиль").setStyle(JustCommand.error);
        MutableText text = Text.literal("Профиль предмета\n⏷").setStyle(JustCommand.aqua);
        var name = pc.name();
        if(name.isPresent()) text.append(Text.literal("\nИмя: " + name.get()).setStyle(JustCommand.white.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, name.get())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Скопировать " + name.get())))));
        var id = pc.id();
        if(id.isPresent()) text.append(Text.literal("\nID: " + id.get()).setStyle(JustCommand.white.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,"" + id.get())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Скопировать " + id.get())))));
        if(pc.properties().containsKey("textures")) {
            Object[] p = pc.properties().get("textures").toArray();
            if(p.length != 0) {
                Property pr = (Property) p[0];
                String base64 = pr.value();
                byte[] decoded = Base64.getDecoder().decode(base64);
                try {
                    String de = new String(decoded, StandardCharsets.UTF_8);
                    text.append(Text.literal("\nТекстуры: " + de).setStyle(JustCommand.white.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, de)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Скопировать " + de)))));
                }catch (Exception ignore) {}
            }
        }
        text.append(Text.literal("\n⏶"));
        return text;
    }
}
