package com.cleanroommc.multiblocked.api.registry;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.block.CustomProperties;
import com.cleanroommc.multiblocked.api.capability.GuiOnlyCapability;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import com.cleanroommc.multiblocked.common.capability.*;
import com.cleanroommc.multiblocked.common.capability.trait.FuelProgressTrait;
import com.cleanroommc.multiblocked.common.capability.trait.RecipeProgressTrait;
import com.google.common.collect.Maps;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MbdCapabilities {
    public static FEMultiblockCapability FE;

    public static ItemMultiblockCapability ITEM;

    public static FluidMultiblockCapability FLUID;

    public static final Map<String, MultiblockCapability<?>> CAPABILITY_REGISTRY = Maps.newHashMap();
    public static final Map<String, MultiblockCapability<?>> TRAIT_ONLY_CAPABILITY_REGISTRY = Maps.newHashMap();

    public static void registerCapability(MultiblockCapability<?> capability) {
        CAPABILITY_REGISTRY.put(capability.name, capability);
    }

    public static void registerTraitOnlyCapability(MultiblockCapability<?> capability) {
        if (capability.hasTrait()) {
            TRAIT_ONLY_CAPABILITY_REGISTRY.put(capability.name, capability);
        }
    }

    public static void registerCapabilities() {
        registerTraitOnlyCapability(new GuiOnlyCapability("recipe_progress", RecipeProgressTrait::new));
        registerTraitOnlyCapability(new GuiOnlyCapability("fuel_progress", FuelProgressTrait::new));
        registerCapability(FE = FEMultiblockCapability.CAP);
        registerCapability(ITEM = ItemMultiblockCapability.CAP);
        registerCapability(FLUID = FluidMultiblockCapability.CAP);
        registerCapability(EntityMultiblockCapability.CAP);
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
        if (Loader.isModLoaded(Multiblocked.MODID_NA)) {
            registerCapability(AuraMultiblockCapability.CAP);
        }
        if (Loader.isModLoaded(Multiblocked.MODID_EU2)) {
            registerCapability(GPExtraUtilities2Capability.CAP);
        }
        if (Loader.isModLoaded(Multiblocked.MODID_PE)) {
            registerCapability(EMCProjectECapability.CAP);
        }
        if (Loader.isModLoaded(Multiblocked.MODID_BG)) {
            registerCapability(LPBloodMagicCapability.CAP);
        }
        if (Loader.isModLoaded(Multiblocked.MODID_EMBERS)) {
            registerCapability(EmberEmbersCapability.CAP);
        }
        if (Loader.isModLoaded(Multiblocked.MODID_TA)) {
            registerCapability(ImpetusThaumicAugmentationCapability.CAP);
        }
        if (Loader.isModLoaded(Multiblocked.MODID_PNC)) {
            registerCapability(PneumaticPressureCapability.CAP);
        }
        if (Loader.isModLoaded(Multiblocked.MODID_AS)) {
            registerCapability(StarlightAstralCapability.CAP);
        }
        if (Loader.isModLoaded(Multiblocked.MODID_MM)) {
            registerCapability(MystMechPowerCapability.CAP);
        }
    }

    public static MultiblockCapability<?> get(String s) {
        return CAPABILITY_REGISTRY.getOrDefault(s, TRAIT_ONLY_CAPABILITY_REGISTRY.get(s));
    }

    public static void registerAnyCapabilityBlocks() {
        for (MultiblockCapability<?> capability : MbdCapabilities.CAPABILITY_REGISTRY.values()) {
            ComponentDefinition definition = new PartDefinition(new ResourceLocation(Multiblocked.MODID, capability.name + ".any"));
            definition.properties.isOpaque = false;
            definition.properties.rotationState = CustomProperties.RotationState.NONE;
            definition.properties.showInJei = false;
            MbdComponents.registerComponent(definition);
            MbdComponents.COMPONENT_BLOCKS_REGISTRY.get(definition.location).setCreativeTab(null);
        }
    }

    public static Collection<MultiblockCapability<?>> getTraitCaps() {
        List<MultiblockCapability<?>> result = new ArrayList<>(TRAIT_ONLY_CAPABILITY_REGISTRY.values());
        for (MultiblockCapability<?> cap : CAPABILITY_REGISTRY.values()) {
            if (cap.hasTrait()) {
                result.add(cap);
            }
        }
        return result;
    }

}
