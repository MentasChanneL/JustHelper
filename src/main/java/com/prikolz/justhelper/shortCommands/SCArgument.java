package com.prikolz.justhelper.shortCommands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.prikolz.justhelper.shortCommands.arguments.*;

import java.util.HashSet;
import java.util.Set;

public abstract class SCArgument implements ArgumentType<String> {

    public Set<String> suggestions;

    static SCArgument getFromJson(JsonObject json) {
        Type type = getType(json.getAsJsonPrimitive("type").getAsString());
        Set<String> suggestions;
        try {
            suggestions = new HashSet<>();
            for (JsonElement el : json.getAsJsonArray("suggestions").asList()) {
                suggestions.add(el.getAsString());
            }
        }catch (Exception e) { suggestions = Set.of(); }
        switch (type) {
            case STRING:
                return new SCStringArgument(suggestions);
            case INT:
                Integer max = null;
                Integer min = null;
                try{ max = json.getAsJsonPrimitive("max").getAsInt(); } catch (Exception ignore) {}
                try{ min = json.getAsJsonPrimitive("min").getAsInt(); } catch (Exception ignore) {}
                return new SCIntArgument(suggestions, max, min);
            case DOUBLE:
                Double maxD = null;
                Double minD = null;
                try{ maxD = json.getAsJsonPrimitive("max").getAsDouble(); } catch (Exception ignore) {}
                try{ minD = json.getAsJsonPrimitive("min").getAsDouble(); } catch (Exception ignore) {}
                return new SCDoubleArgument(suggestions, maxD, minD);
            case VARIANT:
                return new SCVariantArgument(suggestions);
            case GREEDY:
                Type parser = null;
                try { parser = getType(json.getAsJsonPrimitive("parser").getAsString()); } catch (Exception ignore) {}
                String split = null;
                try { split = json.getAsJsonPrimitive("split").getAsString(); } catch (Exception ignore) {}
                return new SCGreedyArgument(suggestions, parser, split);
            case HISTORY:
                byte historyType = 0;
                try {
                   String historyId = json.getAsJsonPrimitive("var").getAsString();
                   switch (historyId) {
                       case "game":
                           historyType = 1;
                       case "save":
                           historyType = 2;
                       case "local":
                           historyType = 3;
                   }
                } catch (Exception ignore) {}
                return new SCHistoryArgument(suggestions, historyType);
        }
        return null;
    }

    public static Type getType(String name) {
        return Type.valueOf(name.toUpperCase());
    }

    public enum Type { GREEDY, INT, DOUBLE, STRING, VARIANT, HISTORY }
}
