package com.prikolz.justhelper.devdata;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.prikolz.justhelper.commands.JustCommand;
import com.prikolz.justhelper.util.ClientUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public abstract class Share {

    public static ItemStack getTemplate() {
        JsonObject o = DevData.getJsonWorld( ClientUtils.worldName() );
        JsonObject template = new JsonObject();
        template.add("type", new JsonPrimitive("function"));
        template.add("position", new JsonPrimitive(0));
        template.add("operations", new JsonArray());
        template.add("values", new JsonArray());
        template.add("name", new JsonPrimitive("$dev.data" + o));
        String compress = zlibCompress( template.toString() );
        var item = new ItemStack(Items.ENDER_CHEST);
        NbtCompound nbt = new NbtCompound();
        NbtCompound bukkitValues = new NbtCompound();
        bukkitValues.put("justmc:template", NbtString.of(compress));
        nbt.put("PublicBukkitValues", bukkitValues);
        item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        item.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Шаблон (Данные мира кода)").setStyle(JustCommand.warn.withItalic(false)));
        return item;
    }

    public static void readShare(String text) {
        if (!text.startsWith("$dev.data")) return;
        ClientUtils.getPlayer().sendMessage(
                Text.literal("JustHelper > Загруженны сторонние данные мира кода").setStyle(JustCommand.success)
        );
        String jsonStr = text.substring(9);
        JsonObject json = (new Gson()).fromJson(jsonStr, JsonObject.class);
        System.out.println(json);
        System.out.println(jsonStr);
        for(String k : json.keySet()) {
            if(k.equals("describes")) {
                JsonObject jsonDesc = json.getAsJsonObject(k);
                for(String kd : jsonDesc.keySet()) DescribeFloor.addDescribe(false, Integer.parseInt(kd), jsonDesc.get(kd).getAsString());
            }
            if(k.equals("comments")) {
                JsonObject jsonCom = json.getAsJsonObject(k);
                for (String pos : jsonCom.keySet()) {
                    String[] argsPos = pos.split("_");
                    int floor = Integer.parseInt(argsPos[0]);
                    int line = Integer.parseInt(argsPos[1]);
                    int x = Integer.parseInt(argsPos[2]);
                    JsonObject co = jsonCom.getAsJsonObject(pos);
                    float scale = co.getAsJsonPrimitive("scale").getAsFloat();
                    String t = co.getAsJsonPrimitive("text").getAsString();
                    ( new DevComment(t, floor, line, x, scale) ).create();
                }
            }
        }
    }

    public static String zlibCompress(String str) {
        var input = str.getBytes();
        var output = new byte[input.length * 4];
        var compressor = new Deflater();
        compressor.setInput(input);
        compressor.finish();
        int l = compressor.deflate(output);
        byte[] arr = new byte[l];
        System.arraycopy(output, 0, arr, 0, l);
        return Base64.getEncoder().encodeToString(arr);
    }

    public static String zlibDecompress(byte[] arr) throws UnsupportedEncodingException, DataFormatException {
        var inflater = new Inflater();
        var outStream = new ByteArrayOutputStream();
        var buffer = new byte[1024];
        inflater.setInput(arr);
        int c = -1;
        while (c != 0) {
            c = inflater.inflate(buffer);
            outStream.write(buffer, 0, c);
        }
        inflater.end();
        return outStream.toString(StandardCharsets.UTF_8);
    }
}
