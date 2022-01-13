package io.github.cleanroommc.multiblocked.api.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.common.capability.FEMultiblockCapability;
import io.github.cleanroommc.multiblocked.common.capability.FluidMultiblockCapability;
import io.github.cleanroommc.multiblocked.common.capability.ItemMultiblockCapability;

public class MultiblockCapabilities {
    public static MultiblockCapability<?> FE_IN;
    public static MultiblockCapability<?> FE_OUT;

    public static MultiblockCapability<?> ITEM_IN;
    public static MultiblockCapability<?> ITEM_OUT;

    public static MultiblockCapability<?> FLUID_IN;
    public static MultiblockCapability<?> FLUID_OUT;

    public static final BiMap<String, MultiblockCapability<?>> CAPABILITY_REGISTRY = HashBiMap.create();

    public static void registerCapability(String name, MultiblockCapability<?> capability) {
        CAPABILITY_REGISTRY.put(name, capability);
    }

    public static void registerCapabilities() {
        registerCapability("forge_energy_in", FE_IN = new FEMultiblockCapability(IO.IN));
        registerCapability("forge_energy_out", FE_OUT = new FEMultiblockCapability(IO.OUT));

        registerCapability("item_in", ITEM_IN = new ItemMultiblockCapability(IO.IN));
        registerCapability("item_out", ITEM_OUT = new ItemMultiblockCapability(IO.OUT));

        registerCapability("fluid_in", FLUID_IN = new FluidMultiblockCapability(IO.IN));
        registerCapability("fluid_out", FLUID_OUT = new FluidMultiblockCapability(IO.OUT));
    }
}
