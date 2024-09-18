package com.prikolz.justhelper.commands.argumens;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import com.google.gson.JsonParser;
import net.minecraft.text.Text;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DisplayJSONArgumentType implements ArgumentType<String> {

    private static DynamicCommandExceptionType UNKNOW_ARGUMENT = new DynamicCommandExceptionType((name) -> {
        return Text.literal("Неизвестный параметр: " + name);
    });

    private static DynamicCommandExceptionType SYNTAX_ERROR = new DynamicCommandExceptionType((name) -> {
        return Text.literal("Ошибка чтения json: " + name);
    });


    private static Set<String> argumentsWhiteList = Set.of(
            "text",
            "font",
            "color",
            "bold",
            "italic",
            "underlined",
            "strikethrough",
            "obfuscated"
    );

    public static String getDisplay(CommandContext<?> context, String name) {
        return context.getArgument(name, String.class);
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String current = reader.getRemaining();
        reader.setCursor(reader.getTotalLength());
        try {
            JsonElement el = JsonParser.parseString(current);
            if(!el.isJsonObject() && !el.isJsonArray()) throw new JsonSyntaxException("Ожидалось [ или {, получил " + el);
            if(el.isJsonObject()) {
                for(String key : el.getAsJsonObject().keySet()) {
                    if(!argumentsWhiteList.contains(key)) throw UNKNOW_ARGUMENT.create(key);
                }
            }
            if(el.isJsonArray()) {
                for(JsonElement obj : el.getAsJsonArray().asList()) {
                    if(!obj.isJsonObject()) throw new JsonSyntaxException("Ожидалось {, получил " + obj);
                    for(String key : obj.getAsJsonObject().keySet()) {
                        if(!argumentsWhiteList.contains(key)) throw UNKNOW_ARGUMENT.create(key);
                    }
                }
            }
        }catch (JsonSyntaxException e) {
            throw SYNTAX_ERROR.create(e.getMessage());
        }
        return current;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(new String[]{"[{\"text\":\"ex.\", \"color\":\"#FFAABB\", \"italic\": false}]"}, builder);
    }
}
