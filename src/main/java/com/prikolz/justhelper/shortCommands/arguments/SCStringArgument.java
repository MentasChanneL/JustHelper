package com.prikolz.justhelper.shortCommands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.prikolz.justhelper.shortCommands.SCArgument;
import net.minecraft.command.CommandSource;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class SCStringArgument extends SCArgument {

    private final StringArgumentType sat;

    public SCStringArgument(Set<String> suggestions) {
        this.suggestions = suggestions;
        this.sat = StringArgumentType.string();
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return this.sat.parse(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(this.suggestions, builder);
    }
}
