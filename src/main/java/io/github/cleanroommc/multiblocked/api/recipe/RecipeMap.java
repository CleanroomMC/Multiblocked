package io.github.cleanroommc.multiblocked.api.recipe;

import com.google.common.collect.Table;
import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 */
public class RecipeMap {

    public List<Recipe> recipes = new ArrayList<>();

    public Set<MultiblockCapability<?>> capabilities = new ObjectOpenHashSet<>();

    public boolean hasCapability(MultiblockCapability<?> capability) {
        return capabilities.contains(capability);
    }

    public void addRecipe(Recipe recipe) {
        recipes.add(recipe);
        capabilities.addAll(recipe.inputs.keySet());
        capabilities.addAll(recipe.outputs.keySet());
    }

    public Recipe searchRecipe(Table<IO, MultiblockCapability<?>, List<CapabilityProxy<?>>> capabilityProxies) {
        for (Recipe recipe : recipes) {
            if (recipe.match(capabilityProxies)) {
                return recipe;
            }
        }
        return null;
    }
}
