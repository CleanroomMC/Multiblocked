package io.github.cleanroommc.multiblocked.api.registry;

import com.google.common.collect.Maps;
import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.common.capability.AspectThaumcraftCapability;
import io.github.cleanroommc.multiblocked.common.capability.FEMultiblockCapability;
import io.github.cleanroommc.multiblocked.common.capability.FluidMultiblockCapability;
import io.github.cleanroommc.multiblocked.common.capability.GasMekanismCapability;
import io.github.cleanroommc.multiblocked.common.capability.HeatMekanismCapability;
import io.github.cleanroommc.multiblocked.common.capability.ItemMultiblockCapability;
import io.github.cleanroommc.multiblocked.common.capability.ManaBotainaCapability;

import java.util.Map;

public class MultiblockCapabilities {
    public static FEMultiblockCapability FE;

    public static ItemMultiblockCapability ITEM;

    public static FluidMultiblockCapability FLUID;

    private static final Map<String, MultiblockCapability<?>> CAPABILITY_REGISTRY = Maps.newHashMap();

    public static void registerCapability(MultiblockCapability<?> capability) {
        CAPABILITY_REGISTRY.put(capability.name, capability);
    }

    public static void registerCapabilities() {
        registerCapability(FE = new FEMultiblockCapability());
        registerCapability(ITEM = new ItemMultiblockCapability());
        registerCapability(FLUID = new FluidMultiblockCapability());
        if (Multiblocked.isModLoaded("botania")) {
            registerCapability(ManaBotainaCapability.CAP);
        }
        if (Multiblocked.isModLoaded("thaumcraft")) {
            registerCapability(AspectThaumcraftCapability.CAP);
        }
        if (Multiblocked.isModLoaded("mekanism")) {
            registerCapability(HeatMekanismCapability.CAP);
            registerCapability(GasMekanismCapability.CAP);
        }
    }

    public static MultiblockCapability<?> get(String s) {
        return CAPABILITY_REGISTRY.get(s);
    }
}
