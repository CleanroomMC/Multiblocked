package com.cleanroommc.multiblocked.jei.recipeppage;

import com.cleanroommc.multiblocked.common.capability.AspectThaumcraftCapability;
import com.cleanroommc.multiblocked.common.capability.GasMekanismCapability;
import com.cleanroommc.multiblocked.common.capability.ParticleQMDCapability;
import com.cleanroommc.multiblocked.common.recipe.content.AspectStack;
import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.RecipeWidget;
import com.cleanroommc.multiblocked.api.recipe.ItemsIngredient;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.api.registry.MultiblockCapabilities;
import com.cleanroommc.multiblocked.jei.ModularWrapper;
import com.cleanroommc.multiblocked.jei.ingredient.AspectListIngredient;
import lach_01298.qmd.jei.ingredient.ParticleType;
import lach_01298.qmd.particle.ParticleStack;
import mekanism.api.gas.GasStack;
import mekanism.client.jei.MekanismJEI;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.util.Tuple;
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
                    .map(Tuple::getFirst)
                    .map(ItemsIngredient.class::cast)
                    .flatMap(r-> Arrays.stream(r.getMatchingStacks()))
                    .collect(Collectors.toList()));
        }
        if (recipe.outputs.containsKey(MultiblockCapabilities.ITEM)) {
            ingredients.setOutputs(VanillaTypes.ITEM, recipe.outputs.get(MultiblockCapabilities.ITEM).stream()
                    .map(Tuple::getFirst)
                    .map(ItemsIngredient.class::cast)
                    .flatMap(r -> Arrays.stream(r.getMatchingStacks()))
                    .collect(Collectors.toList()));
        }

        if (recipe.inputs.containsKey(MultiblockCapabilities.FLUID)) {

            ingredients.setInputs(VanillaTypes.FLUID, recipe.inputs.get(MultiblockCapabilities.FLUID).stream()
                    .map(Tuple::getFirst)
                    .map(FluidStack.class::cast)
                    .collect(Collectors.toList()));
        }
        if (recipe.outputs.containsKey(MultiblockCapabilities.FLUID)) {
            ingredients.setOutputs(VanillaTypes.FLUID, recipe.outputs.get(MultiblockCapabilities.FLUID).stream()
                    .map(Tuple::getFirst)
                    .map(FluidStack.class::cast)
                    .collect(Collectors.toList()));
        }

        if (Multiblocked.isModLoaded(Multiblocked.MODID_TC6)) {
            if (recipe.inputs.containsKey(AspectThaumcraftCapability.CAP)) {
                ingredients.setInputs(AspectListIngredient.INSTANCE, recipe.inputs.get(AspectThaumcraftCapability.CAP).stream()
                        .map(Tuple::getFirst)
                        .map(AspectStack.class::cast)
                        .map(AspectStack::toAspectList)
                        .collect(Collectors.toList()));
            }
            if (recipe.outputs.containsKey(AspectThaumcraftCapability.CAP)) {
                ingredients.setOutputs(AspectListIngredient.INSTANCE, recipe.outputs.get(AspectThaumcraftCapability.CAP).stream()
                        .map(Tuple::getFirst)
                        .map(AspectStack.class::cast)
                        .map(AspectStack::toAspectList)
                        .collect(Collectors.toList()));
            }
        }

        if (Multiblocked.isModLoaded(Multiblocked.MODID_MEK)) {
            if (recipe.inputs.containsKey(GasMekanismCapability.CAP)) {
                ingredients.setInputs(MekanismJEI.TYPE_GAS, recipe.inputs.get(GasMekanismCapability.CAP).stream()
                        .map(Tuple::getFirst)
                        .map(GasStack.class::cast)
                        .collect(Collectors.toList()));
            }
            if (recipe.outputs.containsKey(GasMekanismCapability.CAP)) {
                ingredients.setOutputs(MekanismJEI.TYPE_GAS, recipe.outputs.get(GasMekanismCapability.CAP).stream()
                        .map(Tuple::getFirst)
                        .map(GasStack.class::cast)
                        .collect(Collectors.toList()));
            }
        }

        if (Multiblocked.isModLoaded(Multiblocked.MODID_QMD)) {
            if (recipe.inputs.containsKey(ParticleQMDCapability.CAP)) {
                ingredients.setInputs(ParticleType.Particle, recipe.inputs.get(ParticleQMDCapability.CAP).stream()
                        .map(Tuple::getFirst)
                        .map(ParticleStack.class::cast)
                        .collect(Collectors.toList()));
            }
            if (recipe.outputs.containsKey(ParticleQMDCapability.CAP)) {
                ingredients.setOutputs(ParticleType.Particle, recipe.outputs.get(ParticleQMDCapability.CAP).stream()
                        .map(Tuple::getFirst)
                        .map(ParticleStack.class::cast)
                        .collect(Collectors.toList()));
            }
        }
    }
}
