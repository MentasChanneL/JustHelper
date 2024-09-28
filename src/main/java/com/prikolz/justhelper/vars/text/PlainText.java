package com.prikolz.justhelper.vars.text;

import net.minecraft.item.ItemStack;

public class PlainText implements VarText {

    private String text;

    public PlainText(String string) {
        this.text = string;
    }

    public void setText(String string) {
        this.text = string;
    }

    @Override
    public String toJson() {
        return "{\"text\":\"" + this.text + "\", \"italic\": false}";
    }

    @Override
    public String getOriginal() {
        return this.text;
    }

    @Override
    public ItemStack getExemplar() {
        return VarText.getTextExemplar(this.text, "plain");
    }
}
