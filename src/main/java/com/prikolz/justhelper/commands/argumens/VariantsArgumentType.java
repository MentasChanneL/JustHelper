package com.prikolz.justhelper.commands.argumens;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class VariantsArgumentType implements ArgumentType<String> {

    private final DynamicCommandExceptionType UNKNOWN_PARAMETER_EXCEPTION;
    private final Set<String> strings;

    public VariantsArgumentType(String parameterError, boolean errIsTranslate, Set<String> strs) {
        this.strings = strs;
        this.UNKNOWN_PARAMETER_EXCEPTION = new DynamicCommandExceptionType((name) -> {
            if(errIsTranslate) {
                return Text.translatable(parameterError, name);
            }
            return Text.literal(parameterError.replaceAll("%s", "" + name));
        });
    }

    public VariantsArgumentType(String parameterError, boolean errIsTranslate, String ... strs) {
        this.strings = new HashSet<>();
        this.strings.addAll( Arrays.asList(strs) );
        this.UNKNOWN_PARAMETER_EXCEPTION = new DynamicCommandExceptionType((name) -> {
            if(errIsTranslate) {
                return Text.translatable(parameterError, name);
            }
            return Text.literal(parameterError.replaceAll("%s", "" + name));
        });
    }

    public static String getParameter(final CommandContext<?> context, final String name) {
        return context.getArgument(name, String.class);
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String string = reader.readUnquotedString();
        if(!this.strings.contains(string)) {
            throw UNKNOWN_PARAMETER_EXCEPTION.create(string);
        }
        return string;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(strings, builder);
    }
}
