package com.prikolz.justhelper.shortCommands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.prikolz.justhelper.shortCommands.arguments.*;
import com.prikolz.justhelper.shortCommands.arguments.suggestions.HistorySuggestions;
import com.prikolz.justhelper.shortCommands.arguments.suggestions.JustSuggestions;
import com.prikolz.justhelper.shortCommands.arguments.suggestions.MultiSuggestions;
import com.prikolz.justhelper.shortCommands.arguments.suggestions.ShortSuggestions;
import com.prikolz.justhelper.vars.VarHistory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class SCArgument implements ArgumentType<String> {

    public MultiSuggestions suggestions;

    private static ShortSuggestions getSuggestions(String id) {
        return switch (id) {
            case "@history.game" -> new HistorySuggestions((byte) 1);
            case "@history.save" -> new HistorySuggestions((byte) 2);
            case "@history.local" -> new HistorySuggestions((byte) 3);
            default -> null;
        };
    }

    static SCArgument getFromJson(JsonObject json) {
        Type type = getType(json.getAsJsonPrimitive("type").getAsString());
        MultiSuggestions suggestions = new MultiSuggestions();
        try {
            JustSuggestions justSuggestion = new JustSuggestions(new HashSet<>());
            for (JsonElement el : json.getAsJsonArray("suggestions").asList()) {
                String sEl = el.getAsString();
                ShortSuggestions s = getSuggestions(sEl);
                if(s != null) {
                    suggestions.add(justSuggestion);
                    suggestions.add(s);
                    justSuggestion = new JustSuggestions(new HashSet<>());
                    continue;
                }
                justSuggestion.add(sEl);
            }
            suggestions.add(justSuggestion);
        }catch (Exception ignore) {}
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
        }
        return null;
    }

    public static Type getType(String name) {
        return Type.valueOf(name.toUpperCase());
    }

    public enum Type { GREEDY, INT, DOUBLE, STRING, VARIANT }
}
