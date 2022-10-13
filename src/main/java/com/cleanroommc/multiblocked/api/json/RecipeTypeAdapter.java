package com.cleanroommc.multiblocked.api.json;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.recipe.Content;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.api.recipe.RecipeCondition;
import com.cleanroommc.multiblocked.api.registry.MbdCapabilities;
import com.cleanroommc.multiblocked.api.registry.MbdRecipeConditions;
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

import java.lang.reflect.Type;
import java.util.Map;

public class RecipeTypeAdapter implements JsonSerializer<Recipe>, JsonDeserializer<Recipe> {
    public static final RecipeTypeAdapter INSTANCE = new RecipeTypeAdapter();

    @Override
    public Recipe deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject json = (JsonObject) jsonElement;
        return new Recipe(
                json.get("uid").getAsString(),
                deserializeIO(json.has("inputs") ? json.get("inputs") : new JsonObject()),
                deserializeIO(json.has("outputs") ? json.get("outputs") : new JsonObject()),
                deserializeIO(json.has("tickInputs") ? json.get("tickInputs") : new JsonObject()),
                deserializeIO(json.has("tickOutputs") ? json.get("tickOutputs") : new JsonObject()),
                deserializeConditions(json.has("conditions") ? json.get("conditions") : new JsonArray()),
                json.get("duration").getAsInt());
    }

    @Override
    public JsonElement serialize(Recipe recipe, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject json = new JsonObject();
        json.addProperty("uid", recipe.uid);
        json.addProperty("duration", recipe.duration);
        if (!recipe.inputs.isEmpty()) {
            json.add("inputs", serializeIO(recipe.inputs));
        }
        if (!recipe.outputs.isEmpty()) {
            json.add("outputs", serializeIO(recipe.outputs));
        }
        if (!recipe.tickInputs.isEmpty()) {
            json.add("tickInputs", serializeIO(recipe.tickInputs));
        }
        if (!recipe.tickOutputs.isEmpty()) {
            json.add("tickOutputs", serializeIO(recipe.tickOutputs));
        }
        if (!recipe.conditions.isEmpty()) {
            JsonArray conditions = new JsonArray();
            for (RecipeCondition condition : recipe.conditions) {
                JsonObject object = new JsonObject();
                object.addProperty("type", condition.getType());
                object.add("condition", condition.serialize());
                conditions.add(object);
            }
            json.add("conditions", conditions);
        }
        return json;
    }

    private ImmutableMap<MultiblockCapability<?>, ImmutableList<Content>> deserializeIO(JsonElement jsonElement) {
        JsonObject json = jsonElement.getAsJsonObject();
        ImmutableMap.Builder<MultiblockCapability<?>, ImmutableList<Content>> builder = new ImmutableMap.Builder<>();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            MultiblockCapability<?> capability = MbdCapabilities.get(entry.getKey());
            if (capability != null) {
                ImmutableList.Builder<Content> listBuilder = new ImmutableList.Builder<>();
                for (JsonElement element : entry.getValue().getAsJsonArray()) {
                    JsonObject recipe = element.getAsJsonObject();
                    Object content = capability.deserialize(recipe.get("content"));
                    if (content != null) {
                        Content c = Multiblocked.GSON.fromJson(recipe, Content.class);
                        c.content = content;
                        listBuilder.add(c);
                    }
                }
                builder.put(capability, listBuilder.build());
            }
        }
        return builder.build();
    }

    private ImmutableList<RecipeCondition> deserializeConditions(JsonElement json) {
        if (json.isJsonObject()) {
            return json.getAsJsonObject().entrySet().stream()
                    .map(entry -> MbdRecipeConditions.getCondition(entry.getKey()).createTemplate().deserialize(entry.getValue().getAsJsonObject()))
                    .collect(ImmutableList.toImmutableList());
        } else if (json.isJsonArray() && json.getAsJsonArray().size() > 0){
            ImmutableList.Builder<RecipeCondition> builder = new ImmutableList.Builder<>();
            for (JsonElement condition : json.getAsJsonArray()) {
                JsonObject object = (JsonObject) condition;
                builder.add(MbdRecipeConditions.getCondition(object.get("type").getAsString()).createTemplate().deserialize(object.get("condition").getAsJsonObject()));
            }
            return builder.build();
        }
        return ImmutableList.of();
    }

    private JsonObject serializeIO(ImmutableMap<MultiblockCapability<?>, ImmutableList<Content>> recipe) {
        JsonObject results = new JsonObject();
        recipe.forEach((capability, tuples) -> {
            JsonArray jsonArray = new JsonArray();
            results.add(capability.name, jsonArray);
            for (Content content : tuples) {
                JsonObject result = Multiblocked.GSON.toJsonTree(content).getAsJsonObject();
                jsonArray.add(result);
                result.add("content", capability.serialize(content.content));
            }
        });
        return results;
    }
}
