package com.prikolz.justhelper.vars.text;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

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
        result.setNbt(nbt);
        return result;
    }

    static VarText getText(String text, TextType type) {
        return switch (type) {
            case JSON -> new JsonText(text);
            case STYLED -> new StyledText(text);
            case FORMATTED -> new FormattedText(text);
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

    enum TextType{ STYLED, PLAIN, JSON, FORMATTED }

}
