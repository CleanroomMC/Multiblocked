package io.github.cleanroommc.multiblocked.api.recipe;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import crafttweaker.annotations.ZenRegister;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockCapabilities;
import io.github.cleanroommc.multiblocked.common.capability.AspectThaumcraftCapability;
import io.github.cleanroommc.multiblocked.common.capability.ManaBotainaCapability;
import io.github.cleanroommc.multiblocked.common.recipe.content.AspectStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@ZenClass("mods.multiblocked.recipe.RecipeBuilder")
@ZenRegister
public class RecipeBuilder {

    private final RecipeMap recipeMap;
    private final Map<MultiblockCapability, ImmutableList.Builder<Object>> inputBuilder = new HashMap<>();
    private final Map<MultiblockCapability, ImmutableList.Builder<Object>> outputBuilder = new HashMap<>();
    int duration;
    private StringBuilder keyBuilder = new StringBuilder(); // to make each recipe has a unique identifier and no need to set name himself.

    public RecipeBuilder(RecipeMap recipeMap) {
        this.recipeMap = recipeMap;
    }

    public RecipeBuilder copy() {
        RecipeBuilder copy = new RecipeBuilder(recipeMap);
        copy.inputBuilder.putAll(this.inputBuilder);
        copy.outputBuilder.putAll(this.outputBuilder);
        copy.duration = this.duration;
        copy.keyBuilder = new StringBuilder(keyBuilder.toString());
        return copy;
    }

    public RecipeBuilder input(MultiblockCapability capability, Object... obj) {
        inputBuilder.computeIfAbsent(capability, c -> ImmutableList.builder()).add(obj);
        return this;
    }

    public RecipeBuilder output(MultiblockCapability capability, Object... obj) {
        outputBuilder.computeIfAbsent(capability, c -> ImmutableList.builder()).add(obj);
        return this;
    }

    public RecipeBuilder inputFE(int forgeEnergy) {
        keyBuilder.append(MultiblockCapabilities.FE.name).append(forgeEnergy);
        return input(MultiblockCapabilities.FE, forgeEnergy);
    }

    public RecipeBuilder outputFE(int forgeEnergy) {
        keyBuilder.append(MultiblockCapabilities.FE.name).append(forgeEnergy);
        return output(MultiblockCapabilities.FE, forgeEnergy);
    }

    public RecipeBuilder inputItems(ItemsIngredient... inputs) {
        keyBuilder.append(MultiblockCapabilities.ITEM.name);
        for (ItemsIngredient input : inputs) {
            keyBuilder.append(input.hashCode());
        }
        return input(MultiblockCapabilities.ITEM, (Object[]) inputs);
    }

    public RecipeBuilder outputItems(ItemStack... outputs) {
        keyBuilder.append(MultiblockCapabilities.ITEM.name);
        for (ItemStack output : outputs) {
            keyBuilder.append(output.getCount());
            ResourceLocation name = output.getItem().getRegistryName();
            keyBuilder.append(name == null ? "" : name.hashCode());
        }
        return output(MultiblockCapabilities.ITEM, Arrays.stream(outputs).map(ItemsIngredient::new).toArray());
    }

    public RecipeBuilder inputFluids(FluidStack... inputs) {
        keyBuilder.append(MultiblockCapabilities.FLUID.name);
        for (FluidStack input : inputs) {
            keyBuilder.append(input.amount);
            keyBuilder.append(input.getUnlocalizedName());
        }
        return input(MultiblockCapabilities.FLUID, (Object[]) inputs);
    }

    public RecipeBuilder outputFluids(FluidStack... outputs) {
        keyBuilder.append(MultiblockCapabilities.FLUID.name);
        for (FluidStack output : outputs) {
            keyBuilder.append(output.amount);
            keyBuilder.append(output.getUnlocalizedName());
        }
        return output(MultiblockCapabilities.FLUID, (Object[]) outputs);
    }

    public RecipeBuilder duration(int duration) {
        this.duration = duration;
        return this;
    }

    public RecipeBuilder inputMana(int mana) {
        keyBuilder.append(ManaBotainaCapability.CAP.name).append(mana);
        return input(ManaBotainaCapability.CAP, mana);
    }

    public RecipeBuilder outputMana(int mana) {
        keyBuilder.append(ManaBotainaCapability.CAP.name).append(mana);
        return output(ManaBotainaCapability.CAP, mana);
    }

    public RecipeBuilder inputAspects(AspectStack... inputs) {
        keyBuilder.append(AspectThaumcraftCapability.CAP.name);
        for (AspectStack input : inputs) {
            keyBuilder.append(input.amount);
            keyBuilder.append(input.aspect.getName());
        }
        return input(AspectThaumcraftCapability.CAP, (Object[]) inputs);
    }

    public RecipeBuilder outputAspects(AspectStack... outputs) {
        keyBuilder.append(AspectThaumcraftCapability.CAP.name);
        for (AspectStack output : outputs) {
            keyBuilder.append(output.amount);
            keyBuilder.append(output.aspect.getName());
        }
        return output(AspectThaumcraftCapability.CAP, (Object[]) outputs);
    }

    public Recipe build() {
        ImmutableMap.Builder<MultiblockCapability, ImmutableList<Object>> inputBuilder = new ImmutableMap.Builder<>();
        for (Map.Entry<MultiblockCapability, ImmutableList.Builder<Object>> entry : this.inputBuilder.entrySet()) {
            inputBuilder.put(entry.getKey(), entry.getValue().build());
        }
        ImmutableMap.Builder<MultiblockCapability, ImmutableList<Object>> outputBuilder = new ImmutableMap.Builder<>();
        for (Map.Entry<MultiblockCapability, ImmutableList.Builder<Object>> entry : this.outputBuilder.entrySet()) {
            outputBuilder.put(entry.getKey(), entry.getValue().build());
        }

        return new Recipe(keyBuilder.toString(), inputBuilder.build(), outputBuilder.build(), duration);
    }

    public void buildAndRegister(){
        recipeMap.addRecipe(build());
    }
}
