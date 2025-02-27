package com.prikolz.justhelper.vars.text;

import com.prikolz.justhelper.commands.JustCommand;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;

public interface VarText {

    String toJson();
    String getOriginal();
    ItemStack getExemplar();

    static ItemStack getTextExemplar(String text, String parsing) {
        ItemStack result = new ItemStack(Items.BOOK);
        NbtCompound nbt = new NbtCompound();

        NbtCompound creativePlus = new NbtCompound();
        NbtCompound value = new NbtCompound();
        value.putString("type", "text");
        value.putString("text", text);
        value.putString("parsing", parsing);
        creativePlus.put("value", value);
        nbt.put("creative_plus", creativePlus);

        NbtCompound display = new NbtCompound();
        display.putString("Name", text);
        nbt.put("display", display);

        result.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt) );
        result.set(DataComponentTypes.CUSTOM_NAME, Text.literal(text)
                .setStyle(Style.EMPTY.withItalic(false))
        );
        result.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("Нажмите шифт + пкм, чтобы").setStyle(JustCommand.white),
                Text.literal("книга стала стандартного оформления.").setStyle(JustCommand.white)
        )));
        return result;
    }

    static VarText getText(String text, TextType type) {
        return switch (type) {
            case JSON -> new JsonText(text);
            case STYLED -> new StyledText(text);
            case COLORED -> new ColoredText(text);
            case PLAIN -> new PlainText(text);
        };
    }
    static VarText[] getTexts(String[] texts, TextType type) {
        VarText[] result = new VarText[texts.length];
        int i = 0;
        for(String el : texts) {
            result[i] = getText(el, type);
            i++;
        }
        return result;
    }

    enum TextType{ STYLED, PLAIN, JSON, COLORED }

}
