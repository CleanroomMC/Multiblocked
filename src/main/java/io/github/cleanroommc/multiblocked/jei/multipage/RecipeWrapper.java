package io.github.cleanroommc.multiblocked.jei.multipage;

import io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe.RecipeWidget;
import io.github.cleanroommc.multiblocked.api.recipe.ItemsIngredient;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockCapabilities;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.stream.Collectors;

public class RecipeWrapper extends ModularWrapper{

    public final Recipe recipe;

    public RecipeWrapper(RecipeWidget widget) {
        super(widget, 176, 166);
        recipe = widget.recipe;
    }

    @Override
    public void getIngredients(@Nonnull IIngredients ingredients) {
        if (recipe.inputs.containsKey(MultiblockCapabilities.ITEM)) {
            ingredients.setInputs(VanillaTypes.ITEM, recipe.inputs.get(MultiblockCapabilities.ITEM).stream()
                    .map(ItemsIngredient.class::cast)
                    .flatMap(r-> Arrays.stream(r.matchingStacks))
                    .collect(Collectors.toList()));
        }
        if (recipe.outputs.containsKey(MultiblockCapabilities.ITEM)) {
            ingredients.setOutputs(VanillaTypes.ITEM, recipe.outputs.get(MultiblockCapabilities.ITEM).stream()
                    .map(ItemsIngredient.class::cast)
                    .flatMap(r -> Arrays.stream(r.matchingStacks))
                    .collect(Collectors.toList()));
        }

        if (recipe.inputs.containsKey(MultiblockCapabilities.FLUID)) {

            ingredients.setInputs(VanillaTypes.FLUID, recipe.inputs.get(MultiblockCapabilities.FLUID).stream()
                    .map(FluidStack.class::cast)
                    .collect(Collectors.toList()));
        }
        if (recipe.outputs.containsKey(MultiblockCapabilities.FLUID)) {
            ingredients.setOutputs(VanillaTypes.FLUID, recipe.outputs.get(MultiblockCapabilities.FLUID).stream()
                    .map(FluidStack.class::cast)
                    .collect(Collectors.toList()));
        }
    }
}
