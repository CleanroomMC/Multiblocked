package com.cleanroommc.multiblocked.api.registry;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import com.cleanroommc.multiblocked.client.renderer.impl.CycleBlockStateRenderer;
import com.google.common.collect.Maps;
import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.common.capability.AspectThaumcraftCapability;
import com.cleanroommc.multiblocked.common.capability.FEMultiblockCapability;
import com.cleanroommc.multiblocked.common.capability.FluidMultiblockCapability;
import com.cleanroommc.multiblocked.common.capability.GasMekanismCapability;
import com.cleanroommc.multiblocked.common.capability.HeatMekanismCapability;
import com.cleanroommc.multiblocked.common.capability.ItemMultiblockCapability;
import com.cleanroommc.multiblocked.common.capability.ManaBotainaCapability;
import com.cleanroommc.multiblocked.common.capability.ParticleQMDCapability;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public class MultiblockCapabilities {
    public static FEMultiblockCapability FE;

    public static ItemMultiblockCapability ITEM;

    public static FluidMultiblockCapability FLUID;

    public static final Map<String, MultiblockCapability<?>> CAPABILITY_REGISTRY = Maps.newHashMap();

    public static void registerCapability(MultiblockCapability<?> capability) {
        CAPABILITY_REGISTRY.put(capability.name, capability);
    }

    public static void registerCapabilities() {
        registerCapability(FE = new FEMultiblockCapability());
        registerCapability(ITEM = new ItemMultiblockCapability());
        registerCapability(FLUID = new FluidMultiblockCapability());
        if (Multiblocked.isModLoaded(Multiblocked.MODID_BOT)) {
            registerCapability(ManaBotainaCapability.CAP);
        }
        if (Multiblocked.isModLoaded(Multiblocked.MODID_TC6)) {
            registerCapability(AspectThaumcraftCapability.CAP);
        }
        if (Multiblocked.isModLoaded(Multiblocked.MODID_MEK)) {
            registerCapability(HeatMekanismCapability.CAP);
            registerCapability(GasMekanismCapability.CAP);
        }
        if (Multiblocked.isModLoaded(Multiblocked.MODID_QMD)) {
            registerCapability(ParticleQMDCapability.CAP);
        }
    }

    public static MultiblockCapability<?> get(String s) {
        return CAPABILITY_REGISTRY.get(s);
    }

    public static void registerAnyCapabilityBlocks() {
        for (MultiblockCapability<?> capability : MultiblockCapabilities.CAPABILITY_REGISTRY.values()) {
            for (IO io : IO.values()) {
                ComponentDefinition definition = new PartDefinition(new ResourceLocation(Multiblocked.MODID, capability.name + "." + io.name()));
                definition.baseRenderer = new CycleBlockStateRenderer(capability.getCandidates(io));
                definition.isOpaqueCube = false;
                definition.allowRotate = false;
                definition.showInJei = false;
                MultiblockComponents.registerComponent(definition);
                MultiblockComponents.COMPONENT_BLOCKS_REGISTRY.get(definition.location).setCreativeTab(null);
            }
        }
    }
}
