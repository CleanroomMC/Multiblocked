package com.cleanroommc.multiblocked.api.crafttweaker.expanders;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.recipe.ItemsIngredient;
import com.cleanroommc.multiblocked.api.recipe.RecipeBuilder;
import com.cleanroommc.multiblocked.common.recipe.content.AspectStack;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import lach_01298.qmd.crafttweaker.QMDCTHelper;
import lach_01298.qmd.crafttweaker.particle.IParticleStack;
import lach_01298.qmd.particle.ParticleStack;
import mekanism.api.gas.GasStack;
import mekanism.common.integration.crafttweaker.gas.IGasStack;
import mekanism.common.integration.crafttweaker.helpers.GasHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Arrays;

@ZenRegister
@ZenExpansion("mods.multiblocked.recipe.RecipeBuilder")
public class ExpandRecipeBuilder {

    @ZenMethod
    public static RecipeBuilder inputItems(RecipeBuilder builder, IIngredient... inputs) {
        return inputItems(builder, 1, inputs);
    }

    @ZenMethod
    public static RecipeBuilder outputItems(RecipeBuilder builder, IItemStack... outputs) {
        return outputItems(builder, 1, outputs);
    }

    @ZenMethod
    public static RecipeBuilder inputItems(RecipeBuilder builder, float chance, IIngredient... inputs) {
        return builder.inputItems(chance, Arrays.stream(inputs).map(in -> new ItemsIngredient(in.getAmount(), CraftTweakerMC.getIngredient(in).getMatchingStacks())).toArray(ItemsIngredient[]::new));
    }

    @ZenMethod
    public static RecipeBuilder outputItems(RecipeBuilder builder, float chance, IItemStack... outputs) {
        return builder.outputItems(chance, Arrays.stream(outputs).map(CraftTweakerMC::getItemStack).toArray(ItemStack[]::new));
    }

    @ZenMethod
    public static RecipeBuilder inputFluids(RecipeBuilder builder, ILiquidStack... inputs) {
        return inputFluids(builder, 1, inputs);
    }

    @ZenMethod
    public static RecipeBuilder outputFluids(RecipeBuilder builder, ILiquidStack... outputs) {
        return outputFluids(builder, 1, outputs);
    }

    @ZenMethod
    public static RecipeBuilder inputFluids(RecipeBuilder builder, float chance, ILiquidStack... inputs) {
        return builder.inputFluids(chance, Arrays.stream(inputs).map(CraftTweakerMC::getLiquidStack).toArray(FluidStack[]::new));
    }

    @ZenMethod
    public static RecipeBuilder outputFluids(RecipeBuilder builder, float chance, ILiquidStack... outputs) {
        return builder.outputFluids(chance, Arrays.stream(outputs).map(CraftTweakerMC::getLiquidStack).toArray(FluidStack[]::new));
    }

    @ZenMethod
    @Optional.Method(modid = Multiblocked.MODID_TC6)
    public static RecipeBuilder inputAspects(RecipeBuilder builder, AspectStack... inputs) {
        return inputAspects(builder, 1, inputs);
    }

    @ZenMethod
    @Optional.Method(modid = Multiblocked.MODID_TC6)
    public static RecipeBuilder outputAspects(RecipeBuilder builder, AspectStack... outputs) {
        return outputAspects(builder, 1, outputs);
    }

    @ZenMethod
    @Optional.Method(modid = Multiblocked.MODID_TC6)
    public static RecipeBuilder inputAspects(RecipeBuilder builder, float chance, AspectStack... inputs) {
        return builder.inputAspects(chance, inputs);
    }

    @ZenMethod
    @Optional.Method(modid = Multiblocked.MODID_TC6)
    public static RecipeBuilder outputAspects(RecipeBuilder builder, float chance, AspectStack... outputs) {
        return builder.outputAspects(chance, outputs);
    }

    @ModOnly(Multiblocked.MODID_MEK)
    @ZenRegister
    @ZenExpansion("mods.multiblocked.recipe.RecipeBuilder")
    public static class Gas {
        @ZenMethod
        @Optional.Method(modid = Multiblocked.MODID_MEK)
        public static RecipeBuilder inputGas(RecipeBuilder builder, IGasStack... inputs) {
            return inputGas(builder, 1, inputs);
        }

        @ZenMethod
        @Optional.Method(modid = Multiblocked.MODID_MEK)
        public static RecipeBuilder outputGas(RecipeBuilder builder, IGasStack... outputs) {
            return outputGas(builder, 1, outputs);
        }

        @ZenMethod
        @Optional.Method(modid = Multiblocked.MODID_MEK)
        public static RecipeBuilder inputGas(RecipeBuilder builder, float chance, IGasStack... inputs) {
            return builder.inputGas(chance, Arrays.stream(inputs).map(GasHelper::toGas).toArray(GasStack[]::new));
        }

        @ZenMethod
        @Optional.Method(modid = Multiblocked.MODID_MEK)
        public static RecipeBuilder outputGas(RecipeBuilder builder, float chance, IGasStack... outputs) {
            return builder.outputGas(chance, Arrays.stream(outputs).map(GasHelper::toGas).toArray(GasStack[]::new));
        }
    }

    @ModOnly(Multiblocked.MODID_QMD)
    @ZenRegister
    @ZenExpansion("mods.multiblocked.recipe.RecipeBuilder")
    public static class Particle {

        @ZenMethod
        @Optional.Method(modid = Multiblocked.MODID_QMD)
        public static RecipeBuilder inputParticles(RecipeBuilder builder, IParticleStack... inputs) {
            return inputParticles(builder, 1, inputs);
        }

        @ZenMethod
        @Optional.Method(modid = Multiblocked.MODID_QMD)
        public static RecipeBuilder outputParticles(RecipeBuilder builder, IParticleStack... outputs) {
            return outputParticles(builder, 1, outputs);
        }

        @ZenMethod
        @Optional.Method(modid = Multiblocked.MODID_QMD)
        public static RecipeBuilder inputParticles(RecipeBuilder builder, float chance, IParticleStack... inputs) {
            return builder.inputParticles(chance, Arrays.stream(inputs).map(QMDCTHelper::getParticleStack).toArray(ParticleStack[]::new));
        }

        @ZenMethod
        @Optional.Method(modid = Multiblocked.MODID_QMD)
        public static RecipeBuilder outputParticles(RecipeBuilder builder, float chance, IParticleStack... outputs) {
            return builder.outputParticles(chance, Arrays.stream(outputs).map(QMDCTHelper::getParticleStack).toArray(ParticleStack[]::new));
        }
    }

}
