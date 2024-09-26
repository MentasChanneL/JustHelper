package com.prikolz.justhelper.shortCommands;

import com.google.gson.JsonObject;
import com.prikolz.justhelper.Config;

import java.util.HashMap;

public class SCConfig {

    public static boolean useShortCommands = false;

    public static void parse(JsonObject main) throws Exception{
        JsonObject shortSector = (JsonObject) Config.getParamJson("short-commands", main, JsonObject.class.getName(), new JsonObject());
        useShortCommands = (boolean) Config.getParamJson("enable", shortSector, Boolean.class.getName(), false);
        if (!useShortCommands) return;
        JsonObject constructor = shortSector.getAsJsonObject("constructor");
        HashMap<String, ShortCommand> commands = new HashMap<>();
        for(String key : constructor.keySet()) {
            ShortCommand cmd = ShortCommand.fromJson( constructor.getAsJsonObject(key) );
            commands.put(key, cmd);
        }
        ShortCommand.shortCommands = commands;
        for(String key : commands.keySet()) {
            commands.get(key).register(key);
        }
    }
}
