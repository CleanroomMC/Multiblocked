package io.github.cleanroommc.multiblocked.api.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.common.capability.FEMultiblockCapability;
import io.github.cleanroommc.multiblocked.common.capability.FluidMultiblockCapability;
import io.github.cleanroommc.multiblocked.common.capability.ItemMultiblockCapability;

import java.util.Map;

public class MultiblockCapabilities {
    public static MultiblockCapability<?> FE;

    public static MultiblockCapability<?> ITEM;

    public static MultiblockCapability<?> FLUID;

    public static final Map<String, MultiblockCapability<?>> CAPABILITY_REGISTRY = Maps.newHashMap();

    public static void registerCapability(MultiblockCapability<?> capability) {
        CAPABILITY_REGISTRY.put(capability.name, capability);
    }

    public static void registerCapabilities() {
        registerCapability(FE = new FEMultiblockCapability("forge_energy"));
        registerCapability(ITEM = new ItemMultiblockCapability("item"));
        registerCapability(FLUID = new FluidMultiblockCapability("fluid"));
    }
}
