package com.prikolz.justhelper.shortCommands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.prikolz.justhelper.shortCommands.SCArgument;
import com.prikolz.justhelper.shortCommands.arguments.suggestions.MultiSuggestions;
import net.minecraft.command.CommandSource;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class SCGreedyArgument extends SCArgument {

    public SCArgument.Type parseType;
    public String split;

    public SCGreedyArgument(MultiSuggestions suggestions, SCArgument.Type parseType, String split) {
        this.suggestions = suggestions;
        this.parseType = parseType == null ? Type.STRING : parseType;
        this.split = split;
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String current = reader.getRemaining();
        reader.setCursor(reader.getTotalLength());
        if(this.parseType == Type.GREEDY || this.parseType == Type.STRING) return current;
        String[] els = this.split == null ? new String[]{current} : current.split(this.split);
        for(String el : els) {
            SCArgumentParser.parse(el, this.parseType, this.suggestions);
        }
        return current;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(this.suggestions, builder);
    }

    @Override
    public String toString() {
        return "SCGreedyArgument( split=" + this.split + " parsing=" + this.parseType.name() + " suggestions=" + this.suggestions + " )";
    }
}
