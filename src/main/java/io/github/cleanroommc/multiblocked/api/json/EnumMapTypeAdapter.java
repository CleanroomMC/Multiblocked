package io.github.cleanroommc.multiblocked.api.json;

import com.google.gson.InstanceCreator;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import io.github.cleanroommc.multiblocked.Multiblocked;
import net.minecraft.util.BlockRenderLayer;

import java.lang.reflect.Type;
import java.util.EnumMap;

public class EnumMapTypeAdapter {
    public static final Type BlockRenderLayerBooleanType = new TypeToken<EnumMap<BlockRenderLayer, Boolean>>(){}.getType();
    public static final EnumMapInstanceCreator<BlockRenderLayer, Boolean> BlockRenderLayerBooleanCreator = new EnumMapInstanceCreator<>(BlockRenderLayer.class);
    
    public static class EnumMapInstanceCreator<K extends Enum<K>, V> implements InstanceCreator<EnumMap<K, V>>, JsonSerializer<EnumMap<K, V>> {
        private final Class<K> enumClazz;

        public EnumMapInstanceCreator(final Class<K> enumClazz) {
            super();
            this.enumClazz = enumClazz;
        }

        @Override
        public EnumMap<K, V> createInstance(final Type type) {
            return new EnumMap<>(enumClazz);
        }

        @Override
        public JsonElement serialize(EnumMap<K, V> src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == null) return JsonNull.INSTANCE;
            JsonObject jsonObj = new JsonObject();
            src.forEach((k, v) -> jsonObj.add(k.name(), Multiblocked.GSON.toJsonTree(v)));
            return jsonObj;
        }
    }
}
