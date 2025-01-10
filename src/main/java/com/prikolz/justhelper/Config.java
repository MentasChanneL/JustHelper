package com.prikolz.justhelper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.prikolz.justhelper.commands.ClipboardCommand;
import com.prikolz.justhelper.commands.EditItemCommand;
import com.prikolz.justhelper.commands.MultiMsgCommand;
import com.prikolz.justhelper.commands.SignsCommand;
import com.prikolz.justhelper.shortCommands.SCConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
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
import java.util.HashMap;
import java.util.List;

public class Config {

    public static Method signGenerateMethod = null;
    public static Object signGenerateInstance = null;
    public static boolean useCustomOutputClass = false;
    public static boolean compileCustomOutputClass = false;
    public static boolean enableBackTeleport = true;
    public static List<String> messages = new ArrayList<>();
    public static int commandBufferCD = 700;
    public static HashMap<String, ConfiguredCommand> commands = new HashMap<>();
    public static ClickMessageConfig clickMessageConfig = null;

    public static void initialize() throws Exception {

        messages.clear();

        String directoryName = FabricLoader.getInstance().getGameDir().toString() + "/config/justhelper/utils";

        File directory = new File(directoryName);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        directoryName = FabricLoader.getInstance().getGameDir().toString() + "/config/justhelper";

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
        JsonObject customClass = (JsonObject) getParamJson("custom_class_for_sign_output", main, JsonObject.class.getName(), new JsonObject());
        useCustomOutputClass = (boolean) getParamJson("enable", customClass, Boolean.class.getName(), false);
        compileCustomOutputClass = (boolean) getParamJson("compile", customClass, Boolean.class.getName(), false);
        enableBackTeleport = (boolean) getParamJson("enable_back_teleport", main, Boolean.class.getName(), true);
        commandBufferCD = (int) getParamJson("command_buffer_cooldown", main, Integer.class.getName(), 700);
        JsonObject commandsSector = (JsonObject) getParamJson("commands", main, JsonObject.class.getName(), new JsonObject());
        commands.clear();
        commands.put("signs", ConfiguredCommand.fromJson(commandsSector, "signs", new RequiredCommandArgument("name", "signs"), new RequiredCommandArgument("flip", true)));
        commands.put("edit", ConfiguredCommand.fromJson(commandsSector, "edit", new RequiredCommandArgument("name", "edit")));
        commands.put("clipboard", ConfiguredCommand.fromJson(commandsSector, "clipboard", new RequiredCommandArgument("name", "clipboard"), new RequiredCommandArgument("clip_limit", 5000.0)));
        commands.put("multimsg", ConfiguredCommand.fromJson(commandsSector, "multimsg", new RequiredCommandArgument("name", "multimsg")));
        clickMessageConfig = ClickMessageConfig.parse(main);

        SignsCommand.register();
        ClipboardCommand.register();
        EditItemCommand.register();
        MultiMsgCommand.register();

        try {
            SCConfig.parse(main);
        }catch (Exception e) {
            messages.add("КОНФИГ: Ошибка чтения секции short-commands: " + e.getMessage());
        }

    }

    public static String getCommandName(String key) {
        return commands.get(key).getStrParameter("name");
    }

    public static ConfiguredCommand getCommand(String key) {
        return commands.get(key);
    }

    public static void compileJava() throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticCollector, null, StandardCharsets.UTF_8);
        List<String> options = Arrays.asList("-d", FabricLoader.getInstance().getGameDir().toString() + "/config/justhelper/utils");
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(List.of(FabricLoader.getInstance().getGameDir().toString() + "/justhelper/SignOutput.java"));
        JavaCompiler.CompilationTask task = compiler.getTask(new PrintWriter(System.out), fileManager, diagnosticCollector, options, null, compilationUnits);
        boolean success = task.call();
        if (!success) {
            throw new Exception("Compilation error");
        }
    }

    public static void loadCustomOutClass() throws Exception {
        JustClassLoader classLoader = new JustClassLoader(FabricLoader.getInstance().getGameDir().toString() + "/config/justhelper/utils", Text.class.getClassLoader());

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

    public static Object getParamJson(String key, JsonObject object, String className, Object defaultValue) {
        JsonElement result = object.get(key);
        if(result == null) {
            messages.add("КОНФИГ: Параметр " + key + " не найден!");
            return defaultValue;
        }
        try {
            if (className.equals(Boolean.class.getName())) {
                if(!result.isJsonPrimitive() || !result.getAsJsonPrimitive().isBoolean()) {
                    messages.add("КОНФИГ: Неверное значение " + key + ", ожидался true/false!");
                    return defaultValue;
                }
                return result.getAsBoolean();
            }
            if (className.equals(JsonObject.class.getName())) {
                if(!result.isJsonObject()) {
                    messages.add("КОНФИГ: Неверное значение " + key + ", ожидался объект в фигурных скобках!");
                    return defaultValue;
                }
                return result.getAsJsonObject();
            }
            if (className.equals(String.class.getName())) {
                if(!result.isJsonPrimitive() || !result.getAsJsonPrimitive().isString()) {
                    messages.add("КОНФИГ: Неверное значение " + key + ", ожидалась строка!");
                    return defaultValue;
                }
                return result.getAsString();
            }
            if (className.equals(Integer.class.getName())) {
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
        String directoryName = FabricLoader.getInstance().getGameDir().toString() + "/config/justhelper";
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

    public static void openConfig() throws Exception {
        String way = FabricLoader.getInstance().getGameDir().toString() + "\\config\\justhelper";
        Runtime.getRuntime().exec("explorer.exe \"" + way + "\"");
    }

    public static class ConfiguredCommand {
        private final HashMap<String, Object> parameters;
        private final String name;

        private ConfiguredCommand(HashMap<String, Object> parameters, String name) {
            this.parameters = parameters;
            this.name = name;
        }

        public String getStrParameter(String key) {
            return (String) this.parameters.get(key);
        }

        public double getDoubleParameter(String key, double defaultValue) {
            return (Double) this.parameters.get(key);
        }

        public boolean getBooleanParameter(String key, boolean defaultValue) {
            return (Boolean) this.parameters.get(key);
        }

        private static void log(String message) {
            if(MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(Text.literal(message));
            }
        }

        public static ConfiguredCommand fromJson(JsonObject sector, String name, RequiredCommandArgument ... args) {
            HashMap<String, Object> parameters = new HashMap<>();
            for(RequiredCommandArgument a : args) {
                parameters.put(a.key, a.defaultValue);
            }
            ConfiguredCommand result = new ConfiguredCommand(parameters, name);
            if(sector == null) { log("КОНФИГ: Секция commands не найдена!"); return result; }
            JsonObject commandSector = sector.getAsJsonObject(name);
            if(commandSector == null) { log("КОНФИГ: Секция " + name + " в секции commands не задана!"); return result; }

            for(String key : commandSector.keySet()) {
                Object put = null;
                Object d = parameters.get(key);
                if(d == null) {
                    log("КОНФИГ: Аргумент " + key + "не используется в команде " + name);
                    continue;
                }
                JsonElement el = commandSector.get(key);
                if( el.isJsonPrimitive() ) {
                    JsonPrimitive primitive = el.getAsJsonPrimitive();
                    if(primitive.isNumber()) { put = primitive.getAsDouble(); }
                    if(primitive.isString()) { put = primitive.getAsString(); }
                    if(primitive.isBoolean()) { put = primitive.getAsBoolean(); }
                }
                if(put == null || !put.getClass().getName().equals( d.getClass().getName() )) {
                    log("КОНФИГ: Аргумент " + key + " указан не верно! Применено стандартное значение: " + d);
                    continue;
                }
                parameters.put(key, put);
            }
            return result;
        }
    }

    public record RequiredCommandArgument(String key, Object defaultValue) {}
    public static class ClickMessageConfig {
        public final String clickRight;
        public final String clickMiddle;

        public ClickMessageConfig(String clickRight, String clickMiddle) {
            this.clickRight = clickRight;
            this.clickMiddle = clickMiddle;
        }

        public static ClickMessageConfig parse(JsonObject main) {
            JsonObject sector = (JsonObject) getParamJson("click_message", main, JsonObject.class.getName(), new JsonObject());
            String r = (String) getParamJson("right", sector, String.class.getName(), "<run>txt <message>");
            String m = (String) getParamJson("middle", sector, String.class.getName(), "");
            return new ClickMessageConfig(r, m);
        }
    }

}
