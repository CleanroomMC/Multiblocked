package io.github.cleanroommc.multiblocked.api.recipe;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockCapabilities;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RecipeBuilder {

    private final Map<MultiblockCapability<?>, ImmutableList.Builder<Object>> inputBuilder = new HashMap<>();
    private final Map<MultiblockCapability<?>, ImmutableList.Builder<Object>> outputBuilder = new HashMap<>();

    public static RecipeBuilder start() {
        return new RecipeBuilder();
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
        return input(MultiblockCapabilities.FE, forgeEnergy);
    }

    public RecipeBuilder outputFE(int forgeEnergy) {
        return output(MultiblockCapabilities.FE, forgeEnergy);
    }

    public RecipeBuilder inputItems(ItemsIngredient... inputs) {
        return input(MultiblockCapabilities.FE, (Object[]) inputs);
    }

    public RecipeBuilder outputItems(ItemStack... outputs) {
        return output(MultiblockCapabilities.FE, Arrays.stream(outputs).map(ItemsIngredient::new).toArray());
    }

    public RecipeBuilder inputFluids(FluidStack... inputs) {
        return input(MultiblockCapabilities.FE, (Object[]) inputs);
    }

    public RecipeBuilder outputFluids(FluidStack... outputs) {
        return output(MultiblockCapabilities.FE, (Object[]) outputs);
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
        return new Recipe(inputBuilder.build(), outputBuilder.build());
    }

}
