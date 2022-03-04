package io.github.cleanroommc.multiblocked.api.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.cleanroommc.multiblocked.client.renderer.IRenderer;
import io.github.cleanroommc.multiblocked.client.renderer.impl.B3DRenderer;
import io.github.cleanroommc.multiblocked.client.renderer.impl.BlockStateRenderer;
import io.github.cleanroommc.multiblocked.client.renderer.impl.IModelRenderer;
import io.github.cleanroommc.multiblocked.client.renderer.impl.OBJRenderer;

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
            final JsonElement jsonElement = gson.toJsonTree(value);
            if (value instanceof B3DRenderer) {
                jsonElement.getAsJsonObject().addProperty("type", "B3D");
            } else if (value instanceof OBJRenderer) {
                jsonElement.getAsJsonObject().addProperty("type", "OBJ");
            } else if (value instanceof BlockStateRenderer) {
                jsonElement.getAsJsonObject().addProperty("type", "BlockState");
            } else if (value instanceof IModelRenderer) {
                jsonElement.getAsJsonObject().addProperty("type", "IModel");
            } 
            gson.toJson(jsonElement, out);
        }

        @Override
        public IRenderer read(final JsonReader in) {
            final JsonElement jsonElement = gson.fromJson(in, JsonElement.class);
            if (jsonElement.isJsonNull()) return null;
            final String className = jsonElement.getAsJsonObject().get("type").getAsString();
            switch ( className ) {
                case "B3D":
                    return gson.fromJson(jsonElement, B3DRenderer.class);
                case "BlockState":
                    return gson.fromJson(jsonElement, BlockStateRenderer.class);
                case "IModel":
                    return gson.fromJson(jsonElement, IModelRenderer.class);
                case "OBJ":
                    return gson.fromJson(jsonElement, OBJRenderer.class);
                default:
                    throw new IllegalArgumentException("no such renderer" + className);
            }
        }

    }
}
