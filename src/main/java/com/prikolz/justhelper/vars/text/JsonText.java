package com.prikolz.justhelper.vars.text;

import com.google.gson.JsonParser;
import net.minecraft.item.ItemStack;

public class JsonText implements VarText {

    private String json;

    public JsonText(String string) {
        JsonParser.parseString(string);
        this.json = string;
    }

    public void setJson(String json) {
        JsonParser.parseString(json);
        this.json = json;
    }

    @Override
    public String toJson() {
        return json;
    }

    @Override
    public String getOriginal() {
        return this.json;
    }

    @Override
    public ItemStack getExemplar() {
        return VarText.getTextExemplar(this.json, "json");
    }
}
