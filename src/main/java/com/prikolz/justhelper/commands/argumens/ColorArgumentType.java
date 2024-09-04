package com.prikolz.justhelper.commands.argumens;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Set;

public class ColorArgumentType implements ArgumentType<String> {

    private static Set<Character> colorWL = initColorWL();
    private static Set<Character> initColorWL() {
        Set<Character> result = new HashSet<>();

        result.add('a');
        result.add('b');
        result.add('c');
        result.add('d');
        result.add('e');
        result.add('f');

        result.add('0');
        result.add('1');
        result.add('2');
        result.add('3');
        result.add('4');
        result.add('5');
        result.add('6');
        result.add('7');
        result.add('8');
        result.add('9');

        return result;
    }

    private static DynamicCommandExceptionType IILEGAL_SYMVOL = new DynamicCommandExceptionType((name) -> {
        return Text.literal("Недопустимый символ в цвете " + name);
    });

    private static DynamicCommandExceptionType COLOR_SIZE = new DynamicCommandExceptionType((name) -> {
        return Text.literal("Длинна цветового кода должна составлять 6 символов!");
    });

    public static int getParameter(final CommandContext<?> context, final String name) {
        String color = context.getArgument(name, String.class);
        return Integer.parseInt(color, 16);
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String string = reader.readUnquotedString().toLowerCase();
        for(char c : string.toCharArray()) {
            if(!colorWL.contains(c)) {
                throw IILEGAL_SYMVOL.create(string);
            }
        }
        if(string.length() != 6) throw COLOR_SIZE.create(string);
        return string;
    }


}
