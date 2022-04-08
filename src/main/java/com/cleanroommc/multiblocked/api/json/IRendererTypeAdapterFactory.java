package com.cleanroommc.multiblocked.api.json;

import com.cleanroommc.multiblocked.api.registry.MbdRenderers;
import com.cleanroommc.multiblocked.client.renderer.ICustomRenderer;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class IRendererTypeAdapterFactory implements TypeAdapterFactory {
    public static final IRendererTypeAdapterFactory INSTANCE = new IRendererTypeAdapterFactory();

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (IRenderer.class.isAssignableFrom(type.getRawType())) {
            return (TypeAdapter<T>) new IRendererTypeAdapter(gson);
        }
        return null;
    }

    private static final class IRendererTypeAdapter extends TypeAdapter<IRenderer> {

        private final Gson gson;

        private IRendererTypeAdapter(final Gson gson) {
            this.gson = gson;
        }

        @Override
        public void write(final JsonWriter out, final IRenderer value) {
            if (value instanceof ICustomRenderer) {
                JsonObject jsonObject = ((ICustomRenderer) value).toJson(gson, new JsonObject());
                jsonObject.addProperty("type", ((ICustomRenderer) value).getType());
                gson.toJson(jsonObject, out);
            } else {
                gson.toJson(JsonNull.INSTANCE, out);
            }
        }

        @Override
        public IRenderer read(final JsonReader in) {
            final JsonElement jsonElement = gson.fromJson(in, JsonElement.class);
            if (jsonElement.isJsonNull()) return null;
            JsonObject jsonObj = jsonElement.getAsJsonObject();
            final String type = jsonObj.get("type").getAsString();
            ICustomRenderer renderer = MbdRenderers.getRenderer(type);
            if (renderer != null) {
                return renderer.fromJson(gson, jsonObj);
            }
            return null;
        }

    }
}
