package com.lucas;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import net.runelite.client.config.Keybind;

public class KeybindAdapter implements JsonSerializer<Keybind>, JsonDeserializer<Keybind>
{
    @Override
    public JsonElement serialize(Keybind src, Type typeOfSrc, JsonSerializationContext context)
    {
        if (src == null)
        {
            return JsonNull.INSTANCE;
        }

        JsonObject obj = new JsonObject();
        obj.addProperty("keyCode", src.getKeyCode());
        obj.addProperty("modifiers", src.getModifiers());
        return obj;
    }

    @Override
    public Keybind deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        if (json == null || json.isJsonNull())
        {
            return null;
        }

        if (!json.isJsonObject())
        {
            return Keybind.NOT_SET;
        }

        JsonObject obj = json.getAsJsonObject();
        int keyCode = obj.has("keyCode") ? obj.get("keyCode").getAsInt() : 0;
        int modifiers = obj.has("modifiers") ? obj.get("modifiers").getAsInt() : 0;
        return new Keybind(keyCode, modifiers);
    }
}
