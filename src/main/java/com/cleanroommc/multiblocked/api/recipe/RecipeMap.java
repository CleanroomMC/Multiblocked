package com.cleanroommc.multiblocked.api.recipe;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.capability.ICapabilityProxyHolder;
import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.util.FileUtility;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import crafttweaker.annotations.ZenRegister;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 */
@ZenClass("mods.multiblocked.recipe.RecipeMap")
@ZenRegister
public class RecipeMap {
    public static final RecipeMap EMPTY = new RecipeMap("empty");
    public static final Map<String, RecipeMap> RECIPE_MAP_REGISTRY = new HashMap<>();
    @ZenProperty
    public String name;
    @ZenProperty
    public Set<MultiblockCapability<?>> inputCapabilities = new ObjectOpenHashSet<>();
    @ZenProperty
    public Set<MultiblockCapability<?>> outputCapabilities = new ObjectOpenHashSet<>();
    @ZenProperty
    public RecipeBuilder recipeBuilder = new RecipeBuilder(this);
    @ZenProperty
    public ResourceTexture progressTexture = new ResourceTexture("multiblocked:textures/gui/progress_bar_arrow.png");
    @ZenProperty
    public IGuiTexture categoryTexture;
    
    static {
        register(EMPTY);
    }

    public HashMap<String, Recipe> recipes = new HashMap<>();

    public RecipeMap(String name) {
        this.name = name;
    }


    public RecipeMap copy() {
        RecipeMap copy = new RecipeMap(name);
        copy.inputCapabilities.addAll(inputCapabilities);
        copy.outputCapabilities.addAll(outputCapabilities);
        copy.progressTexture = progressTexture;
        copy.categoryTexture = categoryTexture;
        copy.recipes.putAll(recipes);
        return copy;
    }

    public static void register(RecipeMap recipeMap) {
        RECIPE_MAP_REGISTRY.put(recipeMap.name, recipeMap);
    }

    public static void registerRecipeFromFile(Gson gson, File location) {
        for (File file : Optional.ofNullable(location.listFiles((f, n) -> n.endsWith(".json"))).orElse(new File[0])) {
            try {
                JsonObject config = (JsonObject) FileUtility.loadJson(file);
                RecipeMap recipeMap = gson.fromJson(config, RecipeMap.class);
                if (recipeMap != null && !recipeMap.name.equals("empty")) {
                    register(recipeMap);
                }
            } catch (Exception e) {
                Multiblocked.LOGGER.error("error while loading the recipe map file {}", file.toString());
            }
        }
    }

    public String getUnlocalizedName() {
        return Multiblocked.MODID + ".recupe_map." + name;
    }

    @ZenMethod
    public RecipeBuilder start() {
        return recipeBuilder.copy();
    }

    public boolean hasCapability(IO io, MultiblockCapability<?> capability) {
        switch (io) {
            case IN: return inputCapabilities.contains(capability);
            case OUT: return outputCapabilities.contains(capability);
            case BOTH: return inputCapabilities.contains(capability) && outputCapabilities.contains(capability);
        }
        return false;
    }

    public void addRecipe(Recipe recipe) {
        recipes.put(recipe.uid, recipe);
        inputCapabilities.addAll(recipe.inputs.keySet());
        inputCapabilities.addAll(recipe.tickInputs.keySet());
        outputCapabilities.addAll(recipe.outputs.keySet());
        outputCapabilities.addAll(recipe.tickOutputs.keySet());
    }

    @ZenMethod
    public List<Recipe> searchRecipe(ICapabilityProxyHolder holder) {
        if (!holder.hasProxies()) return Collections.emptyList();
        List<Recipe> matches = new ArrayList<>();
        for (Recipe recipe : recipes.values()) {
            if (recipe.matchRecipe(holder) && recipe.matchTickRecipe(holder)) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    @ZenMethod
    public Recipe getRecipe(String uid) {
        return recipes.get(uid);
    }

    @ZenMethod
    public List<Recipe> allRecipes() {
        return new ArrayList<>(recipes.values());
    }

}
