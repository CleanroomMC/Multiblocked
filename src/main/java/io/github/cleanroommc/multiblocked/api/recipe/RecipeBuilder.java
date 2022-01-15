package io.github.cleanroommc.multiblocked.api.recipe;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import crafttweaker.annotations.ZenRegister;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockCapabilities;
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
    private final Map<MultiblockCapability<?>, ImmutableList.Builder<Object>> inputBuilder = new HashMap<>();
    private final Map<MultiblockCapability<?>, ImmutableList.Builder<Object>> outputBuilder = new HashMap<>();
    int duration;
    int hash; // to make each recipe has a unique identifier and no need to set name himself.

    public RecipeBuilder(RecipeMap recipeMap) {
        this.recipeMap = recipeMap;
    }

    public RecipeBuilder copy() {
        RecipeBuilder copy = new RecipeBuilder(recipeMap);
        copy.inputBuilder.putAll(this.inputBuilder);
        copy.outputBuilder.putAll(this.outputBuilder);
        copy.duration = this.duration;
        copy.hash = this.hash;
        return copy;
    }

    public RecipeBuilder input(MultiblockCapability<?> capability, Object... obj) {
        inputBuilder.computeIfAbsent(capability, c -> ImmutableList.builder()).add(obj);
        return this;
    }

    public RecipeBuilder output(MultiblockCapability<?> capability, Object... obj) {
        outputBuilder.computeIfAbsent(capability, c -> ImmutableList.builder()).add(obj);
        return this;
    }

    public RecipeBuilder inputFE(int forgeEnergy) {
        hash += MultiblockCapabilities.FE.hashCode() + forgeEnergy;
        return input(MultiblockCapabilities.FE, forgeEnergy);
    }

    public RecipeBuilder outputFE(int forgeEnergy) {
        hash += MultiblockCapabilities.FE.hashCode() + forgeEnergy;
        return output(MultiblockCapabilities.FE, forgeEnergy);
    }

    public RecipeBuilder inputItems(ItemsIngredient... inputs) {
        hash += MultiblockCapabilities.ITEM.hashCode();
        for (ItemsIngredient input : inputs) {
            hash += input.hashCode();
        }
        return input(MultiblockCapabilities.ITEM, (Object[]) inputs);
    }

    public RecipeBuilder outputItems(ItemStack... outputs) {
        hash += MultiblockCapabilities.ITEM.hashCode();
        for (ItemStack output : outputs) {
            hash += output.getCount();
            ResourceLocation name = output.getItem().getRegistryName();
            hash += name == null ? 0 : name.hashCode();
        }
        return output(MultiblockCapabilities.ITEM, Arrays.stream(outputs).map(ItemsIngredient::new).toArray());
    }

    public RecipeBuilder inputFluids(FluidStack... inputs) {
        hash += MultiblockCapabilities.FLUID.hashCode();
        for (FluidStack input : inputs) {
            hash += input.amount;
            hash += input.getUnlocalizedName().hashCode();
        }
        return input(MultiblockCapabilities.FLUID, (Object[]) inputs);
    }

    public RecipeBuilder outputFluids(FluidStack... outputs) {
        hash += MultiblockCapabilities.FLUID.hashCode();
        for (FluidStack output : outputs) {
            hash += output.amount;
            hash += output.getUnlocalizedName().hashCode();
        }
        return output(MultiblockCapabilities.FLUID, (Object[]) outputs);
    }

    public RecipeBuilder duration(int duration) {
        this.duration = duration;
        return this;
    }

    public Recipe build() {
        ImmutableMap.Builder<MultiblockCapability<?>, ImmutableList<Object>> inputBuilder = new ImmutableMap.Builder<>();
        for (Map.Entry<MultiblockCapability<?>, ImmutableList.Builder<Object>> entry : this.inputBuilder.entrySet()) {
            inputBuilder.put(entry.getKey(), entry.getValue().build());
        }
        ImmutableMap.Builder<MultiblockCapability<?>, ImmutableList<Object>> outputBuilder = new ImmutableMap.Builder<>();
        for (Map.Entry<MultiblockCapability<?>, ImmutableList.Builder<Object>> entry : this.outputBuilder.entrySet()) {
            outputBuilder.put(entry.getKey(), entry.getValue().build());
        }

        return new Recipe(hash, inputBuilder.build(), outputBuilder.build(), duration);
    }

    public void buildAndRegister(){
        recipeMap.addRecipe(build());
    }
}
