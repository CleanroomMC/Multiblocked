package io.github.cleanroommc.multiblocked.api.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.cleanroommc.multiblocked.api.pattern.predicates.PredicateAnyCapability;
import io.github.cleanroommc.multiblocked.api.pattern.predicates.PredicateBlocks;
import io.github.cleanroommc.multiblocked.api.pattern.predicates.PredicateComponent;
import io.github.cleanroommc.multiblocked.api.pattern.predicates.PredicateStates;
import io.github.cleanroommc.multiblocked.api.pattern.predicates.SimplePredicate;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;

public class SimplePredicateTypeAdapter implements JsonDeserializer<SimplePredicate>, JsonSerializer<SimplePredicate> {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(IBlockStateTypeAdapterFactory.INSTANCE)
            .registerTypeAdapterFactory(BlockTypeAdapterFactory.INSTANCE)
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .setLenient()
            .create();
    public static final JsonDeserializer<SimplePredicate> INSTANCE = new SimplePredicateTypeAdapter();

    private SimplePredicateTypeAdapter() { }

    @Override
    public SimplePredicate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonNull()) {
            return null;
        }
        JsonObject jsonObj = json.getAsJsonObject();
        if (JsonUtils.hasField(jsonObj, "type")) {
            String type = JsonUtils.getString(jsonObj, "type");
            switch (type) {
                case "any":
                    return SimplePredicate.ANY;
                case "air":
                    return SimplePredicate.AIR;
                case "states":
                    return GSON.fromJson(jsonObj, PredicateStates.class).buildPredicate();
                case "blocks":
                    return GSON.fromJson(jsonObj, PredicateBlocks.class).buildPredicate();
                case "capability":
                    return GSON.fromJson(jsonObj, PredicateAnyCapability.class).buildPredicate();
                case "component":
                    return GSON.fromJson(jsonObj, PredicateComponent.class).buildPredicate();
            }
        }
        return new SimplePredicate();
    }

    @Override
    public JsonElement serialize(SimplePredicate src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return JsonNull.INSTANCE;
        }
        return GSON.toJsonTree(src);
    }
}
