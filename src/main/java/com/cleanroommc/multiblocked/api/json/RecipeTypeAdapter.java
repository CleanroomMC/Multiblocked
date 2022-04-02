package com.cleanroommc.multiblocked.api.json;

import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.api.registry.MultiblockCapabilities;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.util.Tuple;

import java.lang.reflect.Type;
import java.util.Map;

public class RecipeTypeAdapter implements JsonSerializer<Recipe>, JsonDeserializer<Recipe> {
    public static final RecipeTypeAdapter INSTANCE = new RecipeTypeAdapter();

    @Override
    public Recipe deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject json = (JsonObject) jsonElement;
        return new Recipe(
                json.get("uid").getAsString(),
                deserializeIO(json.get("inputs")),
                deserializeIO(json.get("outputs")),
                json.get("duration").getAsInt());
    }

    @Override
    public JsonElement serialize(Recipe recipe, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject json = new JsonObject();
        json.addProperty("uid", recipe.uid);
        json.addProperty("duration", recipe.duration);
        json.add("inputs", serializeIO(recipe.inputs));
        json.add("outputs", serializeIO(recipe.outputs));
        return json;
    }

    private ImmutableMap<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> deserializeIO(JsonElement jsonElement) {
        JsonObject json = jsonElement.getAsJsonObject();
        ImmutableMap.Builder<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> builder = new ImmutableMap.Builder<>();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            MultiblockCapability<?> capability = MultiblockCapabilities.get(entry.getKey());
            if (capability != null) {
                ImmutableList.Builder<Tuple<Object, Float>> listBuilder = new ImmutableList.Builder<>();
                for (JsonElement element : entry.getValue().getAsJsonArray()) {
                    JsonObject recipe = element.getAsJsonObject();
                    listBuilder.add(new Tuple<>(capability.deserialize(recipe.get("content")), recipe.get("chance").getAsFloat()));
                }
                builder.put(capability, listBuilder.build());
            }
        }
        return builder.build();
    }

    private JsonObject serializeIO(ImmutableMap<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> recipe) {
        JsonObject results = new JsonObject();
        recipe.forEach((capability, tuples) -> {
            JsonArray jsonArray = new JsonArray();
            results.add(capability.name, jsonArray);
            for (Tuple<Object, Float> tuple : tuples) {
                JsonObject result = new JsonObject();
                jsonArray.add(result);
                result.add("content", capability.serialize(tuple.getFirst()));
                result.addProperty("chance", tuple.getSecond());
            }
        });
        return results;
    }
}
