package com.prikolz.justhelper.commands.argumens;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.text.Text;

public class ColorArgumentType implements ArgumentType<String> {

    private static DynamicCommandExceptionType IILEGAL_SYNTAX = new DynamicCommandExceptionType((name) -> {
        return Text.literal("Недопустимый символ в цвете " + name);
    });

    private static DynamicCommandExceptionType COLOR_SIZE = new DynamicCommandExceptionType((name) -> {
        return Text.literal("Длинна цвета должна составлять 7 символов!");
    });

    public static int getParameter(final CommandContext<?> context, final String name) {
        String color = context.getArgument(name, String.class);
        return Integer.parseInt(color, 16);
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        if (!reader.canRead() || reader.peek() != '#') {
            throw COLOR_SIZE.create(reader.read());
        }
        reader.skip();
        if (!reader.canRead(6)) {
            throw COLOR_SIZE.create(reader.read());
        }
        StringBuilder color = new StringBuilder();
        for (int i = 0; i < 6; ++i) {
            char ch = Character.toLowerCase(reader.read());
            if ((ch < '0' || ch > '9') && (ch < 'a' || ch > 'f')) {
                throw IILEGAL_SYNTAX.create(ch);
            }
            color.append(ch);
        }
        return color.toString();
    }

}
