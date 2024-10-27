package com.prikolz.justhelper.shortCommands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.prikolz.justhelper.shortCommands.SCArgument;
import com.prikolz.justhelper.shortCommands.arguments.suggestions.MultiSuggestions;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class SCDoubleArgument extends SCArgument {

    private static final DynamicCommandExceptionType NUM_MAX = new DynamicCommandExceptionType((name) -> {
        return Text.literal("Число " + name + " слишком большое!");
    });
    private static final DynamicCommandExceptionType NUM_MIN = new DynamicCommandExceptionType((name) -> {
        return Text.literal("Число " + name + " слишком маленькое!");
    });

    public Double max;
    public Double min;
    private final StringArgumentType sat;

    public SCDoubleArgument(MultiSuggestions suggestions, Double max, Double min) {
        this.suggestions = suggestions;
        this.max = max;
        this.min = min;
        this.sat = StringArgumentType.string();
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String current = sat.parse(reader);
        double num = SCArgumentParser.parseDouble(current);
        if(this.max != null && num > this.max) throw NUM_MAX.create(current);
        if(this.min != null && num < this.min) throw NUM_MIN.create(current);
        return current;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(this.suggestions, builder);
    }

    @Override
    public String toString() {
        return "SCDoubleArgument( max=" + this.max + " min=" + this.min + " suggestions=" + this.suggestions + " )";
    }
}
