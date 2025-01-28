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
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FloorArgumentType implements ArgumentType<Integer> {

    private static List<String> suggestions = null;

    public FloorArgumentType() {
        if(suggestions == null) {
            suggestions = new ArrayList<>();
            for(int i = 1; i < 16; i++) {
                suggestions.add(String.valueOf(i));
            }
        }
    }

    private static DynamicCommandExceptionType IILEGAL_SYNTAX = new DynamicCommandExceptionType((name) -> {
        return Text.literal("Ожидался номер этажа, получил: " + name);
    });

    public static int getParameter(final CommandContext<?> context, final String name) {
        return context.getArgument(name, Integer.class);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        String string = reader.readUnquotedString();
        int y;
        try {
            y = Integer.parseInt(string);
            if(y < 1) throw new IllegalStateException(":D");
        }catch (Throwable e) {
            throw IILEGAL_SYNTAX.create(string);
        }
        return y;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(suggestions, builder);
    }

}
