package com.prikolz.justhelper.devdata;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.prikolz.justhelper.util.ClientUtils;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;

public class DevData {

    public static HashMap<String, DevData> data = new HashMap<>();

    public static void Initialise() throws IOException {
        String directoryName = FabricLoader.getInstance().getGameDir().toString() + "/config/justhelper";
        Path filePath = Paths.get(directoryName, "dev_data.json");
        if (!Files.exists(filePath)) return;
        JsonObject json = (JsonObject) JsonParser.parseReader(new FileReader(directoryName + "/dev_data.json"));
        readJson(json);
    }

    public static JsonObject getJsonWorld(String worldName) {
        JsonObject result = new JsonObject();
        DevData devData = data.get(worldName);
        if (devData == null) return result;
        JsonObject describesJson = new JsonObject();
        JsonObject commentsJson = new JsonObject();
        for(int kd : devData.describes.keySet()) {
            describesJson.add( String.valueOf(kd), new JsonPrimitive(devData.describes.get(kd)) );
        }
        for (DevComment c : devData.comments) {
            JsonObject commentJson = new JsonObject();
            commentJson.add("text", new JsonPrimitive(c.comment));
            commentJson.add("scale", new JsonPrimitive(c.scale));
            commentsJson.add(c.floor + "_" + c.line + "_" + c.x, commentJson);
        }
        result.add("describes", describesJson);
        result.add("comments", commentsJson);
        return result;
    }

    public static JsonObject createJson() {
        JsonObject main = new JsonObject();
        for(String k : data.keySet()) main.add(k, getJsonWorld(k));
        return main;
    }

    public static void readJson(JsonObject json) {
        for(String k : json.keySet()) {
            HashMap<Integer, String> describes = new HashMap<>();
            HashSet<DevComment> comments = new HashSet<>();
            JsonObject o = json.getAsJsonObject(k);
            if(o.has("describes")) {
                JsonObject jsonDesc = o.getAsJsonObject("describes");
                for(String kd : jsonDesc.keySet()) {
                    describes.put(Integer.parseInt(kd), jsonDesc.get(kd).getAsString());
                }
            }
            if(o.has("comments")) {
                JsonObject jsonCom = o.getAsJsonObject("comments");
                for (String pos : jsonCom.keySet()) {
                    String[] argsPos = pos.split("_");
                    int floor = Integer.parseInt(argsPos[0]);
                    int line = Integer.parseInt(argsPos[1]);
                    int x = Integer.parseInt(argsPos[2]);
                    JsonObject co = jsonCom.getAsJsonObject(pos);
                    float scale = co.getAsJsonPrimitive("scale").getAsFloat();
                    String text = co.getAsJsonPrimitive("text").getAsString();
                    comments.add( new DevComment(text, floor, line, x, scale) );
                }
            }
            data.put(k, new DevData(k, describes, comments));
        }
    }

    public static void Write() throws IOException {
        JsonObject main = createJson();
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

    public static DevData get() {
        DevData data = DevData.data.get(ClientUtils.worldName());
        if(data == null) {
            data = new DevData(ClientUtils.worldName(), new HashMap<>(), new HashSet<>());
            DevData.data.put(ClientUtils.worldName(), data);
        }
        return data;
    }

    public String worldName;
    public HashMap<Integer, String> describes;
    public HashSet<DevComment> comments;

    public DevData(String worldName, HashMap<Integer, String> describes, HashSet<DevComment> comments) {
        this.describes = describes;
        this.worldName = worldName;
        this.comments = comments;
    }
}
