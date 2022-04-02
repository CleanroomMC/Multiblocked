package com.cleanroommc.multiblocked.api.json;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.api.recipe.RecipeMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class RecipeMapTypeAdapter implements JsonSerializer<RecipeMap>, JsonDeserializer<RecipeMap> {
    public static final RecipeMapTypeAdapter INSTANCE = new RecipeMapTypeAdapter();

    @Override
    public RecipeMap deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject json = (JsonObject) jsonElement;
        RecipeMap recipeMap = new RecipeMap(json.get("name").getAsString());
        recipeMap.progressTexture = new ResourceTexture(json.get("progressTexture").getAsString());
        for (JsonElement recipe : json.get("recipes").getAsJsonArray()) {
            recipeMap.addRecipe(Multiblocked.GSON.fromJson(recipe, Recipe.class));
        }
        return recipeMap;
    }

    @Override
    public JsonElement serialize(RecipeMap recipeMap, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject json = new JsonObject();
        json.addProperty("name", recipeMap.name);
        json.addProperty("progressTexture", recipeMap.progressTexture.imageLocation.toString());
        JsonArray recipes = new JsonArray();
        recipeMap.recipes.values().forEach(v -> recipes.add(Multiblocked.GSON.toJsonTree(v)));
        json.add("recipes", recipes);
        return json;
    }
}
