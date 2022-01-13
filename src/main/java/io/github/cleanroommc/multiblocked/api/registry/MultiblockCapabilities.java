package io.github.cleanroommc.multiblocked.api.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.common.capability.FEMultiblockCapability;
import io.github.cleanroommc.multiblocked.common.capability.FluidMultiblockCapability;
import io.github.cleanroommc.multiblocked.common.capability.ItemMultiblockCapability;

public class MultiblockCapabilities {
    public static MultiblockCapability<?> FE;

    public static MultiblockCapability<?> ITEM;

    public static MultiblockCapability<?> FLUID;

    public static final BiMap<String, MultiblockCapability<?>> CAPABILITY_REGISTRY = HashBiMap.create();

    public static void registerCapability(String name, MultiblockCapability<?> capability) {
        CAPABILITY_REGISTRY.put(name, capability);
    }

    public static void registerCapabilities() {
        registerCapability("forge_energy", FE = new FEMultiblockCapability());
        registerCapability("item", ITEM = new ItemMultiblockCapability());
        registerCapability("fluid", FLUID = new FluidMultiblockCapability());
    }
}
