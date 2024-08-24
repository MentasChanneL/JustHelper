package com.prikolz.justhelper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.prikolz.justhelper.Justhelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Config {

    public static Method signGenerateMethod = null;
    public static Object signGenerateInstance = null;
    public static boolean useCustomOutputClass = false;
    public static boolean compileCustomOutputClass = false;
    public static boolean enableBackTeleport = true;
    public static List<String> messages = new ArrayList<>();
    public static int commandBufferCD = 700;

    public static void initialize() throws Exception {

        messages.clear();

        String directoryName = FabricLoader.getInstance().getGameDir().toString() + "/justhelper/utils";

        File directory = new File(directoryName);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        directoryName = FabricLoader.getInstance().getGameDir().toString() + "/justhelper";

        Path filePath = Paths.get(directoryName, "SignOutput.java");

        if (!Files.exists(filePath)) {
            InputStream stream = Config.class.getClassLoader().getResourceAsStream("SignOutput.java");
            if(stream == null) throw new Exception("RESOURCE SignOutput.java NOT FOUND");
            File file = new File(directoryName + "/SignOutput.java");
            try (OutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = stream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }

        filePath = Paths.get(directoryName, "config.json");

        if (!Files.exists(filePath)) {
            saveDefaultConfig();
        }

        JsonObject main = (JsonObject) JsonParser.parseReader(new FileReader(filePath.toFile()));
        JsonObject customClass = (JsonObject) getParamJson("custom_class_for_sign_output", main, new JsonObject());
        useCustomOutputClass = (boolean) getParamJson("enable", customClass, false);
        compileCustomOutputClass = (boolean) getParamJson("compile", customClass, false);
        enableBackTeleport = (boolean) getParamJson("enable_back_teleport", main, true);
        commandBufferCD = (int) getParamJson("command_buffer_cooldown", main, 700);
    }

    public static void compileJava() throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticCollector, null, StandardCharsets.UTF_8);
        List<String> options = Arrays.asList("-d", FabricLoader.getInstance().getGameDir().toString() + "/justhelper/utils");
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(List.of(FabricLoader.getInstance().getGameDir().toString() + "/justhelper/SignOutput.java"));
        JavaCompiler.CompilationTask task = compiler.getTask(new PrintWriter(System.out), fileManager, diagnosticCollector, options, null, compilationUnits);
        boolean success = task.call();
        if (!success) {
            throw new Exception("Compilation error");
        }
    }

    public static void loadCustomOutClass() throws Exception {
        JustClassLoader classLoader = new JustClassLoader(FabricLoader.getInstance().getGameDir().toString() + "/justhelper/utils", Text.class.getClassLoader());

        Class<?> signOutputClass = classLoader.loadClass("SignOutput");
        signGenerateInstance = signOutputClass.getDeclaredConstructor().newInstance();
        signGenerateMethod = null;
        for( Method method : signOutputClass.getMethods()) {
            if(method.getName().equals("generate")) {
                signGenerateMethod = method;
            }
        }
        if(signGenerateMethod == null) throw new Exception("Method 'generate' not found!");
    }

    private static Object getParamJson(String key, JsonObject object, Object defaultValue) {
        JsonElement result = object.get(key);
        if(result == null) {
            messages.add("КОНФИГ: Параметр " + key + " не найден!");
            return defaultValue;
        }
        try {
            if (defaultValue.getClass().getName().equals(Boolean.class.getName())) {
                if(!result.isJsonPrimitive() || !result.getAsJsonPrimitive().isBoolean()) {
                    messages.add("КОНФИГ: Неверное значение " + key + ", ожидался true/false!");
                    return defaultValue;
                }
                return result.getAsBoolean();
            }
            if (defaultValue.getClass().getName().equals(JsonObject.class.getName())) {
                if(!result.isJsonObject()) {
                    messages.add("КОНФИГ: Неверное значение " + key + ", ожидался объект в фигурных скобках!");
                    return defaultValue;
                }
                return result.getAsJsonObject();
            }
            if (defaultValue.getClass().getName().equals(String.class.getName())) {
                if(!result.isJsonPrimitive() || !result.getAsJsonPrimitive().isString()) {
                    messages.add("КОНФИГ: Неверное значение " + key + ", ожидалась строка!");
                    return defaultValue;
                }
                return result.getAsString();
            }
            if (defaultValue.getClass().getName().equals(Integer.class.getName())) {
                if(!result.isJsonPrimitive() || !result.getAsJsonPrimitive().isNumber()) {
                    messages.add("КОНФИГ: Неверное значение " + key + ", ожидалось целое число!");
                    return defaultValue;
                }
                return result.getAsNumber().intValue();
            }
        }catch (Exception e) {
            messages.add("КОНФИГ: ошибка чтения: " + e.getMessage());
        }
        messages.add("КОНФИГ: К параметру " + key + " применено стандартное значение: " + defaultValue);
        return defaultValue;
    }

    public static void saveDefaultConfig() throws Exception {
        String directoryName = FabricLoader.getInstance().getGameDir().toString() + "/justhelper";
        InputStream stream = Config.class.getClassLoader().getResourceAsStream("config.json");
        if(stream == null) throw new Exception("RESOURCE config.json NOT FOUND");
        File file = new File(directoryName + "/config.json");
        try (OutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = stream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

}
