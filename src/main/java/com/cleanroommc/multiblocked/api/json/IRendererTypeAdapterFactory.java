package com.cleanroommc.multiblocked.api.json;

import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.client.renderer.impl.B3DRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.BlockStateRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.GeoComponentRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.IModelRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.OBJRenderer;

public class IRendererTypeAdapterFactory implements TypeAdapterFactory {
    public static final IRendererTypeAdapterFactory INSTANCE = new IRendererTypeAdapterFactory();

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!IRenderer.class.equals(type.getRawType())) return null;
        return (TypeAdapter<T>) new IRendererTypeAdapter(gson);
    }

    private static final class IRendererTypeAdapter extends TypeAdapter<IRenderer> {

        private final Gson gson;

        private IRendererTypeAdapter(final Gson gson) {
            this.gson = gson;
        }

        @Override
        public void write(final JsonWriter out, final IRenderer value) {
            JsonElement jsonElement = gson.toJsonTree(value);
            if (value instanceof BlockStateRenderer) {
                jsonElement.getAsJsonObject().addProperty("type", "BlockState");
            } else if (value instanceof B3DRenderer) {
                jsonElement.getAsJsonObject().addProperty("type", "B3D");
            } else if (value instanceof OBJRenderer) {
                jsonElement.getAsJsonObject().addProperty("type", "OBJ");
            } else if (value instanceof IModelRenderer) {
                jsonElement.getAsJsonObject().addProperty("type", "IModel");
            }
            gson.toJson(jsonElement, out);
        }

        @Override
        public IRenderer read(final JsonReader in) {
            final JsonElement jsonElement = gson.fromJson(in, JsonElement.class);
            if (jsonElement.isJsonNull()) return null;
            JsonObject jsonObj = jsonElement.getAsJsonObject();
            final String className = jsonObj.get("type").getAsString();
            switch ( className ) {
                case "BlockState":
                    return gson.fromJson(jsonElement, BlockStateRenderer.class);
                case "B3D":
                    return gson.fromJson(jsonElement, B3DRenderer.class).checkRegister();
                case "IModel":
                    return gson.fromJson(jsonElement, IModelRenderer.class).checkRegister();
                case "OBJ":
                    return gson.fromJson(jsonElement, OBJRenderer.class).checkRegister();
                case "Geo":
                    return Multiblocked.isModLoaded(Multiblocked.MODID_GEO) ? new GeoComponentRenderer(jsonObj.get("modelName").getAsString()) : null;
                default:
                    return null;
            }
        }

    }
}
