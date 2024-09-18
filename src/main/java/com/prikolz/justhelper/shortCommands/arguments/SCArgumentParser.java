package com.prikolz.justhelper.shortCommands.arguments;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.prikolz.justhelper.shortCommands.SCArgument;
import net.minecraft.text.Text;

import java.util.Set;

public abstract class SCArgumentParser {

    private static final DynamicCommandExceptionType INT_PARSE_ERROR = new DynamicCommandExceptionType((name) -> {
        return Text.literal("Ожидалось целое число, получил: " + name);
    });

    private static final DynamicCommandExceptionType DOUBLE_PARSE_ERROR = new DynamicCommandExceptionType((name) -> {
        return Text.literal("Ожидалось число, получил: " + name);
    });

    private static final DynamicCommandExceptionType VARIANT_NOT_FOUND = new DynamicCommandExceptionType((name) -> {
        return Text.translatable("argument.entity.options.unknown", name);
    });


    public static int parseInt(String element) throws CommandSyntaxException {
        try{
            return Integer.parseInt(element);
        }catch (Exception e) {
            throw INT_PARSE_ERROR.create(element);
        }
    }
    public static double parseDouble(String element) throws CommandSyntaxException {
        try{
            return Double.parseDouble(element);
        }catch (Exception e) {
            throw DOUBLE_PARSE_ERROR.create(element);
        }
    }
    public static void parseVariant(String element, Set<String> suggestions) throws CommandSyntaxException {
        if(suggestions == null) return;
        if(!suggestions.contains(element)) throw VARIANT_NOT_FOUND.create(element);
    }

    public static void parse(String element, SCArgument.Type type, Set<String> suggestions) throws CommandSyntaxException {
        switch (type) {
            case INT -> parseInt(element);
            case DOUBLE -> parseDouble(element);
            case VARIANT -> parseVariant(element, suggestions);
        }
    }
}
