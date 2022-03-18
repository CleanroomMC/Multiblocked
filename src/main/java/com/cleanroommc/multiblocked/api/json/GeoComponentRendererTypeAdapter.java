package com.cleanroommc.multiblocked.api.json;

import com.cleanroommc.multiblocked.client.renderer.impl.GeoComponentRenderer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class GeoComponentRendererTypeAdapter implements JsonSerializer<GeoComponentRenderer> {
    public static final GeoComponentRendererTypeAdapter INSTANCE = new GeoComponentRendererTypeAdapter();

    private GeoComponentRendererTypeAdapter() { }

    @Override
    public JsonElement serialize(GeoComponentRenderer src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "Geo");
        jsonObject.addProperty("modelName", src.modelName);
        return jsonObject;
    }
}
