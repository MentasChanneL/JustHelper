package com.prikolz.justhelper.vars;

import com.google.gson.*;
import com.prikolz.justhelper.Justhelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class VarHistory {

    public static HashMap<String, VarHistory> worldsHistory = new HashMap<>();

    public static void analyseItemStack(ItemStack item) {
        NbtComponent c = item.get(DataComponentTypes.CUSTOM_DATA);
        if(c == null) return;
        try {
            NbtCompound customs = c.copyNbt();
            NbtCompound value = customs.getCompound("creative_plus").getCompound("value");
            if(!value.getString("type").equals("variable")) return;
            String type = value.getString("scope");
            String name = value.getString("variable");
            VarHistory history = getHistory();
            if(history == null) {
                history = new VarHistory(worldName(), new HashSet<>(), new HashSet<>(), new HashSet<>());
                worldsHistory.put(worldName(), history);
            }
            switch (type) {
                case "game":
                    System.out.println("+GAME name=" + name + " type=" + type);
                    history.game.add(name);
                    break;
                case "local":
                    System.out.println("+LOCAL name=" + name + " type=" + type);
                    history.local.add(name);
                    break;
                case "save":
                    System.out.println("+SAVE name=" + name + " type=" + type);
                    history.save.add(name);
            }
        }catch (Exception ignore) {}
    }

    private static VarHistory getHistory() {
        World world = MinecraftClient.getInstance().world;
        if(world == null) return null;
        return worldsHistory.get( world.getRegistryKey().getValue().getPath() );
    }

    private static String worldName() {
        return MinecraftClient.getInstance().world.getRegistryKey().getValue().getPath();
    }

    public static Set<String> getVarListGame() {
        VarHistory history = getHistory();
        if(history == null) return Set.of();
        return history.game;
    }

    public static Set<String> getVarListSave() {
        VarHistory history = getHistory();
        if(history == null) return Set.of();
        return history.save;
    }

    public static Set<String> getVarListLocal() {
        VarHistory history = getHistory();
        if(history == null) return Set.of();
        return history.local;
    }

    private static Set<String> getValues(JsonArray array) {
        Set<String> result = new HashSet<>();
        for(JsonElement obj : array) {
            if(!obj.isJsonPrimitive() || !obj.getAsJsonPrimitive().isString()) continue;
            result.add( obj.getAsJsonPrimitive().getAsString() );
        }
        return result;
    }

    private static JsonArray getArray(Set<String> vars) {
        JsonArray arr = new JsonArray();
        for(String str : vars) {
            arr.add(str);
        }
        return arr;
    }

    public static void loadFromFile() throws IOException {
        String directoryName = FabricLoader.getInstance().getGameDir().toString() + "/config/justhelper";
        Path filePath = Paths.get(directoryName, "history.json");
        if (!Files.exists(filePath)) {
            File file = new File(directoryName + "/history.json");
            if (!file.createNewFile()) Justhelper.LOGGER.error("Ошибка создания файла history.json");
            FileWriter writer = new FileWriter(file);
            writer.write("{}");
            writer.close();
        }
        JsonObject main = (JsonObject) JsonParser.parseReader( new FileReader(filePath.toFile()) );
        HashMap<String, VarHistory> newWorldsHistory = new HashMap<>();
        for(String key : main.keySet()) {
            JsonObject worldSector = main.getAsJsonObject(key);
            JsonArray game = worldSector.getAsJsonArray("game");
            JsonArray save = worldSector.getAsJsonArray("save");
            JsonArray local = worldSector.getAsJsonArray("local");
            VarHistory vh = new VarHistory(key, getValues(game), getValues(save), getValues(local));
            newWorldsHistory.put(key, vh);
        }
        worldsHistory = newWorldsHistory;
    }

    public static void saveJson() throws IOException {
        String directoryName = FabricLoader.getInstance().getGameDir().toString() + "/config/justhelper";
        Path filePath = Paths.get(directoryName, "history.json");
        if (!Files.exists(filePath)) {
            File file = new File(directoryName + "/history.json");
            if (!file.createNewFile()) { Justhelper.LOGGER.error("Ошибка создания файла history.json"); return; }
        }
        JsonObject json = new JsonObject();
        for(String world : worldsHistory.keySet()) {
            VarHistory history = worldsHistory.get(world);
            JsonObject worldSector = new JsonObject();
            worldSector.add("game", getArray(history.game) );
            worldSector.add("save", getArray(history.save) );
            worldSector.add("local", getArray(history.local) );
            json.add(world, worldSector);
        }
        try (FileWriter writer = new FileWriter(directoryName + "/history.json")) {
            Gson gson = new Gson();
            gson.toJson(json, writer);
        } catch (IOException e) {
            Justhelper.LOGGER.error("Ошибка записи данных в history.json");
            e.printStackTrace();
        }
    }

    public String worldId;
    public Set<String> game;
    public Set<String> save;
    public Set<String> local;

    public VarHistory(String world, Set<String> game, Set<String> save, Set<String> local) {
        this.worldId = world;
        this.game = game;
        this.save = save;
        this.local = local;
    }

}
