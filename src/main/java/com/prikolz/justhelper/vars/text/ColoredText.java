package com.prikolz.justhelper.vars.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.item.ItemStack;

public class ColoredText implements VarText {

    private String text;
    private Component component;

    public ColoredText(String string) {
        setColoredText(string);
    }

    private String format(String str) {
        return str.replaceAll("&","§").replaceAll("%space%", " ").replaceAll("%empty%", "");
    }

    public void setColoredText(String string) {
        this.text = string;
        this.component = Component.empty().decoration(TextDecoration.ITALIC, false).append( LegacyComponentSerializer.legacySection().deserialize(format(string)) );
    }

    @Override
    public String toJson() {
        return JSONComponentSerializer.json().serialize(this.component);
    }

    @Override
    public String getOriginal() {
        return this.text;
    }

    @Override
    public ItemStack getExemplar() {
        return VarText.getTextExemplar(this.text, "legacy");
    }
}
