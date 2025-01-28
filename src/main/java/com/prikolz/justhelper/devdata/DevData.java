package com.prikolz.justhelper.devdata;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;
import com.prikolz.justhelper.util.ClientUtils;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class DevData {

    public static HashMap<String, DevData> data = new HashMap<>();

    public static void Initialise() throws IOException {
        String directoryName = FabricLoader.getInstance().getGameDir().toString() + "/config/justhelper";
        Path filePath = Paths.get(directoryName, "dev_data.json");
        if (!Files.exists(filePath)) return;
        JsonObject json = (JsonObject) JsonParser.parseReader(new FileReader(directoryName + "/dev_data.json"));
        for(String k : json.keySet()) {
            HashMap<Integer, String> describes = new HashMap<>();
            JsonObject o = json.getAsJsonObject(k);
            if(o.has("describes")) {
                JsonObject jsonDesc = o.getAsJsonObject("describes");
                for(String kd : jsonDesc.keySet()) {
                    describes.put(Integer.parseInt(kd), jsonDesc.get(kd).getAsString());
                }
            }
            data.put(k, new DevData(k, describes));
        }
    }

    public static void Write() throws IOException {
        JsonObject main = new JsonObject();
        for(String k : data.keySet()) {
            DevData devData = data.get(k);
            JsonObject devJson = new JsonObject();
            JsonObject describesJson = new JsonObject();
            for(int kd : devData.describes.keySet()) {
                describesJson.add( String.valueOf(kd), new JsonPrimitive(devData.describes.get(kd)) );
            }
            devJson.add("describes", describesJson);
            main.add(k, devJson);
        }
        String directoryName = FabricLoader.getInstance().getGameDir().toString() + "/config/justhelper";
        Path filePath = Paths.get(directoryName, "dev_data.json");
        if (!Files.exists(filePath)) {
            File file = new File(directoryName + "/dev_data.json");
            try (OutputStream outputStream = new FileOutputStream(file)) {
                outputStream.write("{}".getBytes());
            }
        }
        try (FileWriter writer = new FileWriter(directoryName + "/dev_data.json")) {
            writer.write(main.toString());
        } catch (IOException e) {
            ClientUtils.send("Ошибка записи dev_data.json: " + e.getMessage());
        }
    }

    public String worldName;
    public HashMap<Integer, String> describes;

    public DevData(String worldName, HashMap<Integer, String> describes) {
        this.describes = describes;
        this.worldName = worldName;
    }
}
