package com.prikolz.justhelper.vars.text;

import net.minecraft.item.ItemStack;

public class FormattedText implements VarText {

    private String text;

    public FormattedText(String string) {
        this.text = string;
    }

    public void setFormattedText(String string) {
        this.text = string;
    }

    @Override
    public String toJson() {
        return "{\"text\":\"" + this.text.replaceAll("&", "§").replaceAll("%space%", " ").replaceAll("%empty%", "") + "\"}";
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
