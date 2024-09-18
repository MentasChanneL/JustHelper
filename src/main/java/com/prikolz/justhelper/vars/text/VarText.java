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
}
