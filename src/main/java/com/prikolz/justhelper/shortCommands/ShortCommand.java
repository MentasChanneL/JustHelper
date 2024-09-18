package com.prikolz.justhelper.shortCommands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.prikolz.justhelper.CommandBuffer;
import com.prikolz.justhelper.commands.JustCommand;
import com.prikolz.justhelper.shortCommands.arguments.SCGreedyArgument;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShortCommand {

    public static HashMap<String, ShortCommand> shortCommands = new HashMap<>();

    private final HashMap<String, SCArgument> arguments;
    public final String run;
    public final String[] structure;

    private final List<String> names;
    private final List<CommandElement> runElements;

    public ShortCommand(HashMap<String, SCArgument> arguments, String[] structure, String run) {
        this.arguments = arguments;
        this.structure = structure;
        this.run = run;
        this.names = new ArrayList<>();
        this.runElements = new ArrayList<>();
        for(String key : arguments.keySet()) {
            int i = 1;
            String name = key;
            while(names.contains(name)) {
                name = key + i;
                i++;
            }
            names.add(name);
        }

        boolean readVar = false;
        boolean hasInter = false;
        StringBuilder element = new StringBuilder();
        int indexInNames;
        for(char c : this.run.toCharArray()) {
            if(c == '$') {
                hasInter = true;
                continue;
            }
            if(hasInter) {
                hasInter = false;
                if (c != '{') continue;
                readVar = true;
                this.runElements.add(new CommandElement(CommandElementType.TEXT, element.toString()));
                element = new StringBuilder();
                continue;
            }
            if(readVar) {
                if(c == '}') {
                    readVar = false;
                    indexInNames = Integer.parseInt( element.toString() ) - 1;
                    this.runElements.add( new CommandElement(CommandElementType.VAR, this.names.get(indexInNames) ));
                    element = new StringBuilder();
                    continue;
                }
            }
            element.append(c);
        }
        this.runElements.add(new CommandElement(CommandElementType.TEXT, element.toString()));

    }

    public void register(String myKey) {
        LiteralArgumentBuilder<FabricClientCommandSource> manager = ClientCommandManager.literal(myKey);
        RequiredArgumentBuilder<FabricClientCommandSource, String> mladshy = null;
        for(int i = 0; i < this.structure.length; i++) {
            int index = structure.length - 1 - i;
            String name = this.names.get(index);
            String key = this.structure[index];
            SCArgument arg = this.arguments.get( key );
            if(arg == null) continue;
            if(mladshy == null) {
                mladshy = ClientCommandManager.argument(name, arg).executes(context -> run(context));
                continue;
            }
            mladshy = ClientCommandManager.argument(key, arg).then(mladshy);
            JustCommand.registerInDispacher(manager);
        }
        manager.then(mladshy);
    }

    private int run(CommandContext<FabricClientCommandSource> context) {

        StringBuilder command = new StringBuilder();

        for(CommandElement el : this.runElements) {
            if(el.type == CommandElementType.TEXT) {
                command.append(el.data);
                continue;
            }
            if(el.type == CommandElementType.VAR) {
                String key = this.structure[this.names.indexOf(el.data)];
                SCArgument arg = this.arguments.get(key);
                String var = StringArgumentType.getString(context, el.data);
                if(arg instanceof SCGreedyArgument ga) {
                    if(ga.split != null) {
                        String[] values = var.split(ga.split);
                        for(String value : values) {
                            CommandBuffer.sendCommand(command.append(value).toString());
                        }
                        return 1;
                    }
                    command.append(var);
                    break;
                }
                command.append(var);
            }
        }

        CommandBuffer.sendCommand(command.toString());

        return 1;
    }

    public static ShortCommand fromJson(JsonObject json) {
        JsonObject arguments = json.getAsJsonObject("arguments");
        HashMap<String, SCArgument> args = new HashMap<>();
        for(String key : arguments.keySet()) {
            args.put(key, SCArgument.getFromJson( arguments.getAsJsonObject(key) ));
        }
        JsonArray arr = json.getAsJsonArray("structure");
        String[] structure = new String[arr.size()];
        int i = 0;
        for(JsonElement el : arr) {
            structure[i] = el.getAsJsonPrimitive().getAsString();
            i++;
        }
        String run = json.getAsJsonPrimitive("run").getAsString();
        return new ShortCommand(args, structure, run);
    }

    private enum CommandElementType{ TEXT, VAR }
    private record CommandElement(CommandElementType type, String data) {}

}
