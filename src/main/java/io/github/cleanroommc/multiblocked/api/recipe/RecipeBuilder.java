package io.github.cleanroommc.multiblocked.api.recipe;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import crafttweaker.annotations.ZenRegister;
import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockCapabilities;
import io.github.cleanroommc.multiblocked.common.capability.AspectThaumcraftCapability;
import io.github.cleanroommc.multiblocked.common.capability.GasMekanismCapability;
import io.github.cleanroommc.multiblocked.common.capability.HeatMekanismCapability;
import io.github.cleanroommc.multiblocked.common.capability.ManaBotainaCapability;
import io.github.cleanroommc.multiblocked.common.capability.ParticleQMDCapability;
import io.github.cleanroommc.multiblocked.common.recipe.content.AspectStack;
import lach_01298.qmd.particle.ParticleStack;
import mekanism.api.gas.GasStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
@ZenClass("mods.multiblocked.recipe.RecipeBuilder")
@ZenRegister
public class RecipeBuilder {

    public final RecipeMap recipeMap;
    public final Map<MultiblockCapability<?>, ImmutableList.Builder<Tuple<Object, Float>>> inputBuilder = new HashMap<>();
    public final Map<MultiblockCapability<?>, ImmutableList.Builder<Tuple<Object, Float>>> outputBuilder = new HashMap<>();
    protected int duration;
    protected StringBuilder keyBuilder = new StringBuilder(); // to make each recipe has a unique identifier and no need to set name himself.

    public RecipeBuilder(RecipeMap recipeMap) {
        this.recipeMap = recipeMap;
    }

    public RecipeBuilder copy() {
        RecipeBuilder copy = new RecipeBuilder(recipeMap);
        inputBuilder.forEach((k, v)->{
            ImmutableList.Builder<Tuple<Object, Float>> builder = ImmutableList.builder();
            copy.inputBuilder.put(k, builder.addAll(v.build()));
        });
        outputBuilder.forEach((k, v)->{
            ImmutableList.Builder<Tuple<Object, Float>> builder = ImmutableList.builder();
            copy.outputBuilder.put(k, builder.addAll(v.build()));
        });
        copy.duration = this.duration;
        copy.keyBuilder = new StringBuilder(keyBuilder.toString());
        return copy;
    }

    public RecipeBuilder duration(int duration) {
        this.duration = duration;
        return this;
    }

    public RecipeBuilder input(MultiblockCapability<?> capability, float chance, Object... obj) {
        keyBuilder.append(chance);
        inputBuilder.computeIfAbsent(capability, c -> ImmutableList.builder()).add(Arrays.stream(obj).map(o->new Tuple<>(o, chance)).toArray(Tuple[]::new));
        return this;
    }

    public RecipeBuilder output(MultiblockCapability<?> capability, float chance, Object... obj) {
        keyBuilder.append(chance);
        outputBuilder.computeIfAbsent(capability, c -> ImmutableList.builder()).add(Arrays.stream(obj).map(o->new Tuple<>(o, chance)).toArray(Tuple[]::new));
        return this;
    }

    public RecipeBuilder inputFE(int forgeEnergy) {
        return inputFE(1, forgeEnergy);
    }

    public RecipeBuilder outputFE(int forgeEnergy) {
        return outputFE(1, forgeEnergy);
    }

    public RecipeBuilder inputFE(float chance, int forgeEnergy) {
        keyBuilder.append(MultiblockCapabilities.FE.name).append(forgeEnergy);
        return input(MultiblockCapabilities.FE, chance, forgeEnergy);
    }

    public RecipeBuilder outputFE(float chance, int forgeEnergy) {
        keyBuilder.append(MultiblockCapabilities.FE.name).append(forgeEnergy);
        return output(MultiblockCapabilities.FE, chance, forgeEnergy);
    }

    public RecipeBuilder inputItems(ItemsIngredient... inputs) {
        return inputItems(1, inputs);
    }

    public RecipeBuilder outputItems(ItemStack... outputs) {
        return outputItems(1, outputs);
    }

    public RecipeBuilder inputItems(float chance, ItemsIngredient... inputs) {
        keyBuilder.append(MultiblockCapabilities.ITEM.name);
        for (ItemsIngredient input : inputs) {
            keyBuilder.append(input.hashCode());
        }
        return input(MultiblockCapabilities.ITEM, chance, (Object[]) inputs);
    }

    public RecipeBuilder outputItems(float chance, ItemStack... outputs) {
        keyBuilder.append(MultiblockCapabilities.ITEM.name);
        for (ItemStack output : outputs) {
            keyBuilder.append(output.getCount());
            ResourceLocation name = output.getItem().getRegistryName();
            keyBuilder.append(name == null ? "" : name.hashCode());
        }
        return output(MultiblockCapabilities.ITEM, chance, Arrays.stream(outputs).map(ItemsIngredient::new).toArray());
    }

    public RecipeBuilder inputFluids(FluidStack... inputs) {
        return inputFluids(1, inputs);
    }

    public RecipeBuilder outputFluids(FluidStack... outputs) {
        return outputFluids(1, outputs);
    }

    public RecipeBuilder inputFluids(float chance, FluidStack... inputs) {
        keyBuilder.append(MultiblockCapabilities.FLUID.name);
        for (FluidStack input : inputs) {
            keyBuilder.append(input.amount);
            keyBuilder.append(input.getUnlocalizedName());
        }
        return input(MultiblockCapabilities.FLUID, chance, (Object[]) inputs);
    }

    public RecipeBuilder outputFluids(float chance, FluidStack... outputs) {
        keyBuilder.append(MultiblockCapabilities.FLUID.name);
        for (FluidStack output : outputs) {
            keyBuilder.append(output.amount);
            keyBuilder.append(output.getUnlocalizedName());
        }
        return output(MultiblockCapabilities.FLUID, chance, (Object[]) outputs);
    }

    @Optional.Method(modid = Multiblocked.MODID_BOT)
    public RecipeBuilder inputMana(int mana) {
        return inputMana(1, mana);
    }

    @Optional.Method(modid = Multiblocked.MODID_BOT)
    public RecipeBuilder outputMana(int mana) {
        return outputMana(1, mana);
    }

    @Optional.Method(modid = Multiblocked.MODID_BOT)
    public RecipeBuilder inputMana(float chance, int mana) {
        keyBuilder.append(ManaBotainaCapability.CAP.name).append(mana);
        return input(ManaBotainaCapability.CAP, chance, mana);
    }

    @Optional.Method(modid = Multiblocked.MODID_BOT)
    public RecipeBuilder outputMana(float chance, int mana) {
        keyBuilder.append(ManaBotainaCapability.CAP.name).append(mana);
        return output(ManaBotainaCapability.CAP, chance, mana);
    }

    @Optional.Method(modid = Multiblocked.MODID_TC6)
    public RecipeBuilder inputAspects(AspectStack... inputs) {
        return inputAspects(1, inputs);
    }

    @Optional.Method(modid = Multiblocked.MODID_TC6)
    public RecipeBuilder outputAspects(AspectStack... outputs) {
        return outputAspects(1, outputs);
    }

    @Optional.Method(modid = Multiblocked.MODID_TC6)
    public RecipeBuilder inputAspects(float chance, AspectStack... inputs) {
        keyBuilder.append(AspectThaumcraftCapability.CAP.name);
        for (AspectStack input : inputs) {
            keyBuilder.append(input.amount);
            keyBuilder.append(input.aspect.getName());
        }
        return input(AspectThaumcraftCapability.CAP, chance, (Object[]) inputs);
    }

    @Optional.Method(modid = Multiblocked.MODID_TC6)
    public RecipeBuilder outputAspects(float chance, AspectStack... outputs) {
        keyBuilder.append(AspectThaumcraftCapability.CAP.name);
        for (AspectStack output : outputs) {
            keyBuilder.append(output.amount);
            keyBuilder.append(output.aspect.getName());
        }
        return output(AspectThaumcraftCapability.CAP, chance, (Object[]) outputs);
    }

    @Optional.Method(modid = Multiblocked.MODID_MEK)
    public RecipeBuilder inputHeat(double heat) {
        return inputHeat(1, heat);
    }

    @Optional.Method(modid = Multiblocked.MODID_MEK)
    public RecipeBuilder outputHeat(double heat) {
        return outputHeat(1, heat);
    }

    @Optional.Method(modid = Multiblocked.MODID_MEK)
    public RecipeBuilder inputHeat(float chance, double heat) {
        keyBuilder.append(HeatMekanismCapability.CAP.name).append(heat);
        return input(HeatMekanismCapability.CAP, chance, heat);
    }

    @Optional.Method(modid = Multiblocked.MODID_MEK)
    public RecipeBuilder outputHeat(float chance, double heat) {
        keyBuilder.append(HeatMekanismCapability.CAP.name).append(heat);
        return output(HeatMekanismCapability.CAP, chance, heat);
    }

    @Optional.Method(modid = Multiblocked.MODID_MEK)
    public RecipeBuilder inputGas(GasStack... inputs) {
        return inputGas(1, inputs);
    }

    @Optional.Method(modid = Multiblocked.MODID_MEK)
    public RecipeBuilder outputGas(GasStack... outputs) {
        return outputGas(1, outputs);
    }

    @Optional.Method(modid = Multiblocked.MODID_MEK)
    public RecipeBuilder inputGas(float chance, GasStack... inputs) {
        keyBuilder.append(GasMekanismCapability.CAP.name);
        for (GasStack input : inputs) {
            keyBuilder.append(input.amount);
            keyBuilder.append(input.getGas().getID());
        }
        return input(GasMekanismCapability.CAP, chance, (Object[]) inputs);
    }

    @Optional.Method(modid = Multiblocked.MODID_MEK)
    public RecipeBuilder outputGas(float chance, GasStack... outputs) {
        keyBuilder.append(GasMekanismCapability.CAP.name);
        for (GasStack output : outputs) {
            keyBuilder.append(output.amount);
            keyBuilder.append(output.getGas().getID());
        }
        return output(GasMekanismCapability.CAP, chance, (Object[]) outputs);
    }

    @Optional.Method(modid = Multiblocked.MODID_QMD)
    public RecipeBuilder inputParticles(ParticleStack... inputs) {
        return inputParticles(1, inputs);
    }

    @Optional.Method(modid = Multiblocked.MODID_QMD)
    public RecipeBuilder outputParticles(ParticleStack... outputs) {
        return outputParticles(1, outputs);
    }

    @Optional.Method(modid = Multiblocked.MODID_QMD)
    public RecipeBuilder inputParticles(float chance, ParticleStack... inputs) {
        keyBuilder.append(ParticleQMDCapability.CAP.name);
        for (ParticleStack input : inputs) {
            keyBuilder.append(input.getAmount());
            keyBuilder.append(input.getParticle().getName());
            keyBuilder.append(input.getMeanEnergy());
            keyBuilder.append(input.getFocus());
        }
        return input(ParticleQMDCapability.CAP, chance, (Object[]) inputs);
    }

    @Optional.Method(modid = Multiblocked.MODID_QMD)
    public RecipeBuilder outputParticles(float chance, ParticleStack... outputs) {
        keyBuilder.append(ParticleQMDCapability.CAP.name);
        for (ParticleStack output : outputs) {
            keyBuilder.append(output.getAmount());
            keyBuilder.append(output.getParticle().getName());
            keyBuilder.append(output.getMeanEnergy());
            keyBuilder.append(output.getFocus());
        }
        return output(ParticleQMDCapability.CAP, chance, (Object[]) outputs);
    }

    public Recipe build() {
        ImmutableMap.Builder<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> inputBuilder = new ImmutableMap.Builder<>();
        for (Map.Entry<MultiblockCapability<?>, ImmutableList.Builder<Tuple<Object, Float>>> entry : this.inputBuilder.entrySet()) {
            inputBuilder.put(entry.getKey(), entry.getValue().build());
        }
        ImmutableMap.Builder<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> outputBuilder = new ImmutableMap.Builder<>();
        for (Map.Entry<MultiblockCapability<?>, ImmutableList.Builder<Tuple<Object, Float>>> entry : this.outputBuilder.entrySet()) {
            outputBuilder.put(entry.getKey(), entry.getValue().build());
        }

        return new Recipe(keyBuilder.toString(), inputBuilder.build(), outputBuilder.build(), duration);
    }

    public void buildAndRegister(){
        recipeMap.addRecipe(build());
    }
}
