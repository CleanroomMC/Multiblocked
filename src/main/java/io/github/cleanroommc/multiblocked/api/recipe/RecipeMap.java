package io.github.cleanroommc.multiblocked.api.recipe;

import com.google.common.collect.Table;
import crafttweaker.annotations.ZenRegister;
import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import scala.collection.immutable.IntMap;
import stanhebben.zenscript.annotations.ZenClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 */
@ZenClass("mods.multiblocked.recipe.RecipeMap")
@ZenRegister
public class RecipeMap {

    public Int2ObjectMap<Recipe> recipes = new Int2ObjectOpenHashMap<>();
    public Set<MultiblockCapability<?>> capabilities = new ObjectOpenHashSet<>();

    public boolean hasCapability(MultiblockCapability<?> capability) {
        return capabilities.contains(capability);
    }

    public void addRecipe(Recipe recipe) {
        recipes.put(recipe.uid, recipe);
        capabilities.addAll(recipe.inputs.keySet());
        capabilities.addAll(recipe.outputs.keySet());
    }

    public Recipe searchRecipe(Table<IO, MultiblockCapability<?>, List<CapabilityProxy<?>>> capabilityProxies) {
        for (Recipe recipe : recipes.values()) {
            if (recipe.match(capabilityProxies)) {
                return recipe;
            }
        }
        return null;
    }
}
