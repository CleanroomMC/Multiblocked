package io.github.cleanroommc.multiblocked.api.recipe;

import com.google.common.collect.Table;
import crafttweaker.annotations.ZenRegister;
import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 */
@ZenClass("mods.multiblocked.recipe.RecipeMap")
@ZenRegister
public class RecipeMap {
    @ZenProperty
    public final String name;
    @ZenProperty
    public Set<MultiblockCapability<?>> inputCapabilities = new ObjectOpenHashSet<>();
    @ZenProperty
    public Set<MultiblockCapability<?>> outputCapabilities = new ObjectOpenHashSet<>();
    @ZenProperty
    RecipeBuilder recipeBuilder = new RecipeBuilder(this);

    public Int2ObjectMap<Recipe> recipes = new Int2ObjectOpenHashMap<>();

    public RecipeMap(String name) {
        this.name = name;
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
        outputCapabilities.addAll(recipe.outputs.keySet());
    }

    public Recipe searchRecipe(Table<IO, MultiblockCapability<?>, Long2ObjectOpenHashMap<CapabilityProxy<?>>> capabilityProxies) {
        if (capabilityProxies == null) return null;
        for (Recipe recipe : recipes.values()) {
            if (recipe.match(capabilityProxies)) {
                return recipe;
            }
        }
        return null;
    }
}
