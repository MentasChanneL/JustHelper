package com.prikolz.justhelper.shortCommands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.prikolz.justhelper.shortCommands.SCArgument;
import com.prikolz.justhelper.vars.VarHistory;
import net.minecraft.command.CommandSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class SCHistoryArgument extends SCArgument {

    private final StringArgumentType sat;
    private final byte type;

    public SCHistoryArgument(Set<String> suggestions, byte type) {
        this.suggestions = suggestions;
        if(this.suggestions == null) this.suggestions = Set.of();
        this.type = type;
        this.sat = StringArgumentType.string();
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return this.sat.parse(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Set<String> sug = new HashSet<>(this.suggestions);
        switch (type) {
            case 1 : sug.addAll(VarHistory.getVarListGame());
            case 2 : sug.addAll(VarHistory.getVarListSave());
            case 3 : sug.addAll(VarHistory.getVarListLocal());
        };
        return CommandSource.suggestMatching(sug, builder);
    }

    @Override
    public String toString() {
        return "SCVariantArgument( type=" + this.type + " )";
    }
}
