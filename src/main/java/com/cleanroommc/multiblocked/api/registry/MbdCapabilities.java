package com.cleanroommc.multiblocked.api.registry;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import com.cleanroommc.multiblocked.common.capability.*;
import com.google.common.collect.Maps;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;

import java.util.Map;

public class MbdCapabilities {
    public static FEMultiblockCapability FE;

    public static ItemMultiblockCapability ITEM;

    public static FluidMultiblockCapability FLUID;

    public static final Map<String, MultiblockCapability<?>> CAPABILITY_REGISTRY = Maps.newHashMap();

    public static void registerCapability(MultiblockCapability<?> capability) {
        CAPABILITY_REGISTRY.put(capability.name, capability);
    }

    public static void registerCapabilities() {
        registerCapability(FE = FEMultiblockCapability.CAP);
        registerCapability(ITEM = ItemMultiblockCapability.CAP);
        registerCapability(FLUID = FluidMultiblockCapability.CAP);
        if (Loader.isModLoaded(Multiblocked.MODID_BOT)) {
            registerCapability(ManaBotaniaCapability.CAP);
        }
        if (Loader.isModLoaded(Multiblocked.MODID_TC6)) {
            registerCapability(AspectThaumcraftCapability.CAP);
        }
        if (Loader.isModLoaded(Multiblocked.MODID_MEK)) {
            registerCapability(HeatMekanismCapability.CAP);
            registerCapability(GasMekanismCapability.CAP);
        }
        if (Loader.isModLoaded(Multiblocked.MODID_QMD)) {
            registerCapability(ParticleQMDCapability.CAP);
        }
        if (Loader.isModLoaded(Multiblocked.MODID_GTCE)) {
            registerCapability(EnergyGTCECapability.CAP);
        }
        if (Loader.isModLoaded(Multiblocked.MODID_LC)) {
            registerCapability(LEMultiblockCapability.CAP);
        }
        if (Loader.isModLoaded(Multiblocked.MODID_PRODIGY)) {
            registerCapability(HotAirProdigyCapability.CAP);
        }
        if (Loader.isModLoaded(Multiblocked.MODID_TA)) {
            registerCapability(ImpetusThaumicAugmentationCapability.CAP);
        }
    }

    public static MultiblockCapability<?> get(String s) {
        return CAPABILITY_REGISTRY.get(s);
    }

    public static void registerAnyCapabilityBlocks() {
        for (MultiblockCapability<?> capability : MbdCapabilities.CAPABILITY_REGISTRY.values()) {
            ComponentDefinition definition = new PartDefinition(new ResourceLocation(Multiblocked.MODID, capability.name + ".any"));
            definition.isOpaqueCube = false;
            definition.allowRotate = false;
            definition.showInJei = false;
            MbdComponents.registerComponent(definition);
            MbdComponents.COMPONENT_BLOCKS_REGISTRY.get(definition.location).setCreativeTab(null);
        }
    }
}
