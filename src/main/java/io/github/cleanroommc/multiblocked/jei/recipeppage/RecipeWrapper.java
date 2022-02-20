package io.github.cleanroommc.multiblocked.jei.recipeppage;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe.RecipeWidget;
import io.github.cleanroommc.multiblocked.api.recipe.ItemsIngredient;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockCapabilities;
import io.github.cleanroommc.multiblocked.common.capability.AspectThaumcraftCapability;
import io.github.cleanroommc.multiblocked.common.capability.GasMekanismCapability;
import io.github.cleanroommc.multiblocked.common.capability.ParticleQMDCapability;
import io.github.cleanroommc.multiblocked.common.recipe.content.AspectStack;
import io.github.cleanroommc.multiblocked.jei.ModularWrapper;
import io.github.cleanroommc.multiblocked.jei.ingredient.AspectListIngredient;
import lach_01298.qmd.jei.ingredient.ParticleType;
import lach_01298.qmd.particle.ParticleStack;
import mekanism.api.gas.GasStack;
import mekanism.client.jei.MekanismJEI;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.stream.Collectors;

public class RecipeWrapper extends ModularWrapper {

    public final Recipe recipe;

    public RecipeWrapper(RecipeWidget widget) {
        super(widget, widget.getSize().width, widget.getSize().height);
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

        if (Multiblocked.isModLoaded("thaumcraft")) {
            if (recipe.inputs.containsKey(AspectThaumcraftCapability.CAP)) {
                ingredients.setInputs(AspectListIngredient.INSTANCE, recipe.inputs.get(AspectThaumcraftCapability.CAP).stream()
                        .map(AspectStack.class::cast)
                        .map(AspectStack::toAspectList)
                        .collect(Collectors.toList()));
            }
            if (recipe.outputs.containsKey(AspectThaumcraftCapability.CAP)) {
                ingredients.setOutputs(AspectListIngredient.INSTANCE, recipe.outputs.get(AspectThaumcraftCapability.CAP).stream()
                        .map(AspectStack.class::cast)
                        .map(AspectStack::toAspectList)
                        .collect(Collectors.toList()));
            }
        }

        if (Multiblocked.isModLoaded("mekanism")) {
            if (recipe.inputs.containsKey(GasMekanismCapability.CAP)) {
                ingredients.setInputs(MekanismJEI.TYPE_GAS, recipe.inputs.get(GasMekanismCapability.CAP).stream()
                        .map(GasStack.class::cast)
                        .collect(Collectors.toList()));
            }
            if (recipe.outputs.containsKey(GasMekanismCapability.CAP)) {
                ingredients.setOutputs(MekanismJEI.TYPE_GAS, recipe.outputs.get(GasMekanismCapability.CAP).stream()
                        .map(GasStack.class::cast)
                        .collect(Collectors.toList()));
            }
        }

        if (Multiblocked.isModLoaded("qmd")) {
            if (recipe.inputs.containsKey(ParticleQMDCapability.CAP)) {
                ingredients.setInputs(ParticleType.Particle, recipe.inputs.get(ParticleQMDCapability.CAP).stream()
                        .map(ParticleStack.class::cast)
                        .collect(Collectors.toList()));
            }
            if (recipe.outputs.containsKey(ParticleQMDCapability.CAP)) {
                ingredients.setOutputs(ParticleType.Particle, recipe.outputs.get(ParticleQMDCapability.CAP).stream()
                        .map(ParticleStack.class::cast)
                        .collect(Collectors.toList()));
            }
        }
    }
}
