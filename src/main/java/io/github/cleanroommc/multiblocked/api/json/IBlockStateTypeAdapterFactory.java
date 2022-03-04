package io.github.cleanroommc.multiblocked.api.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

public class IBlockStateTypeAdapterFactory implements TypeAdapterFactory {
    public static final IBlockStateTypeAdapterFactory INSTANCE = new IBlockStateTypeAdapterFactory();

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!IBlockState.class.equals(type.getRawType())) return null;
        return (TypeAdapter<T>) new IBlockStateTypeAdapter(gson);
    }

    private static final class IBlockStateTypeAdapter extends TypeAdapter<IBlockState> {

        private final Gson gson;

        private IBlockStateTypeAdapter(final Gson gson) {
            this.gson = gson;
        }

        @Override
        public void write(final JsonWriter out, final IBlockState value) {
            if (value == null || value.getBlock().getRegistryName() == null) {
                gson.toJson(JsonNull.INSTANCE, out);
                return;
            }
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", value.getBlock().getRegistryName().toString());
            jsonObject.addProperty("meta", value.getBlock().getMetaFromState(value));
            gson.toJson(jsonObject, out);
        }

        @Override
        public IBlockState read(final JsonReader in) {
            final JsonElement jsonElement = gson.fromJson(in, JsonElement.class);
            if (jsonElement.isJsonNull()) return null;
            final Block block = Block.getBlockFromName(jsonElement.getAsJsonObject().get("id").getAsString());
            if (block == null) return null;
            if (jsonElement.getAsJsonObject().has("meta")) {
                final int meta = jsonElement.getAsJsonObject().get("meta").getAsInt();
                return block.getStateFromMeta(meta);
            }
            return block.getDefaultState();
        }

    }
}
