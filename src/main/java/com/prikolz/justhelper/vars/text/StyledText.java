package com.prikolz.justhelper.vars.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

public class StyledText implements VarText {

    private Component component;
    private String original;

    public StyledText(String string) {
        setStyledText(string);
    }

    public void setStyledText(String string) {
        this.component = MiniMessage.miniMessage().deserialize(string);
        this.original = string;
    }

    public Component getStyled() {
        return this.component.asComponent();
    }

    @Override
    public String toJson() {
        return JSONComponentSerializer.json().serialize(this.component);
    }

    @Override
    public String getOriginal() {
        return original;
    }

    @Override
    public ItemStack getExemplar() {
        return VarText.getTextExemplar(this.original, "minimessage");
    }
}
