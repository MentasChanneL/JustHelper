package com.prikolz.justhelper.commands.argumens;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.prikolz.justhelper.vars.text.VarText;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

public class TextFormattingArgumentType implements ArgumentType<VarText.TextType> {

    private static DynamicCommandExceptionType UNKNOW_FORMAT = new DynamicCommandExceptionType((name) -> {
        return Text.literal("Неизвестный формат текста: " + name);
    });

    private static final String[] enumList = initEnums();
    private static String[] initEnums() {
        String[] result = new String[VarText.TextType.values().length];

        int i = 0;
        for(VarText.TextType type : VarText.TextType.values()) {
            result[i] = type.name().toLowerCase();
            i++;
        }

        return result;
    }

    public static VarText.TextType getFormatType(final CommandContext<?> context, final String name) {
        return context.getArgument(name, VarText.TextType.class);
    }

    @Override
    public VarText.TextType parse(StringReader reader) throws CommandSyntaxException {
        String string = reader.readUnquotedString();
        try {
            return VarText.TextType.valueOf(string);
        }catch (Exception e) {
            throw UNKNOW_FORMAT.create(string);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(enumList, builder);
    }
}
