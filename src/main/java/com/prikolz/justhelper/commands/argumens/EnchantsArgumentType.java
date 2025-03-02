package com.prikolz.justhelper.commands.argumens;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class EnchantsArgumentType implements ArgumentType<String> {

    public static Set<String> enchantList = new HashSet<>();

    private static DynamicCommandExceptionType UNKNOWN_PARAMETER_EXCEPTION = new DynamicCommandExceptionType((name) -> {
        return Text.literal("Неизвестное зачарование: " + name);
    });

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String string = reader.readUnquotedString();
        if(!enchantList.contains(string)) {
            throw UNKNOWN_PARAMETER_EXCEPTION.create(string);
        }
        return string;
    }
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        enchantList.clear();

        MinecraftClient client = MinecraftClient.getInstance();
        if(client.world == null) return CommandSource.suggestMatching(enchantList, builder);;

        Registry<Enchantment> reg = client.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
        for(RegistryKey<Enchantment> key : reg.getKeys() ) {
            enchantList.add(key.getValue().getPath());
        }
        return CommandSource.suggestMatching(enchantList, builder);
    }
}
