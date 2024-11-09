package com.prikolz.justhelper.commands.argumens;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.prikolz.justhelper.Sign;
import com.prikolz.justhelper.SignInfo;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SignsArgumentType implements ArgumentType<String> {

    private final StringArgumentType parser;

    public SignsArgumentType() {
        this.parser = StringArgumentType.greedyString();
    }

    public static String getParameter(final CommandContext<?> context, final String name) {
        return context.getArgument(name, String.class);
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return parser.parse(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        List<String> content = new LinkedList<>();
        for(Sign s : Sign.signs.values()) {
            SignInfo info = Sign.getInfo(s);
            if(info == null) continue;
            for(Text t : info.lines) {
                content.add(t.getString());
            }
        }
        return CommandSource.suggestMatching(content, builder);
    }
}
