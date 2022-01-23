package io.github.cleanroommc.multiblocked.api.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.block.BlockComponent;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import io.github.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import io.github.cleanroommc.multiblocked.api.definition.PartDefinition;
import io.github.cleanroommc.multiblocked.client.renderer.impl.CycleBlockStateRenderer;
import io.github.cleanroommc.multiblocked.jei.multipage.MultiblockInfoCategory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.Set;

public class MultiblockComponents {
    public static final Set<Class <? extends TileEntity>> CLASS_SET = new HashSet<>();
    public static final BiMap<ResourceLocation, ComponentDefinition> DEFINITION_REGISTRY = HashBiMap.create();
    public static final BiMap<ResourceLocation, BlockComponent> COMPONENT_BLOCKS_REGISTRY = HashBiMap.create();

    public static void registerComponent(ComponentDefinition definition) {
        DEFINITION_REGISTRY.put(definition.location, definition);
        COMPONENT_BLOCKS_REGISTRY.computeIfAbsent(definition.location, loc -> new BlockComponent(definition)).setRegistryName(definition.location);
        if (definition instanceof ControllerDefinition && Multiblocked.isModLoaded(Multiblocked.MODID_JEI)) {
            MultiblockInfoCategory.registerMultiblock((ControllerDefinition) definition);
        }
    }

    public static void registerTileEntity() {
        for (ComponentDefinition definition : DEFINITION_REGISTRY.values()) {
            if (!CLASS_SET.contains(definition.clazz)) {
                GameRegistry.registerTileEntity(definition.clazz, definition.location);
                CLASS_SET.add(definition.clazz);
            }
        }
        CLASS_SET.clear();
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        COMPONENT_BLOCKS_REGISTRY.values().forEach(BlockComponent::onModelRegister);
    }

    public static BlockComponent getOrRegisterAnyCapabilityBlock(IO io, MultiblockCapability capability) {
        ResourceLocation location = new ResourceLocation(Multiblocked.MODID, capability.name + "." + io.name());
        if (!DEFINITION_REGISTRY.containsKey(location)) {
            ComponentDefinition definition = new PartDefinition(location);
            definition.baseRenderer = new CycleBlockStateRenderer(capability.getCandidates(io));
            definition.isOpaqueCube = false;
            definition.allowRotate = false;
            registerComponent(definition);
            COMPONENT_BLOCKS_REGISTRY.get(location).setCreativeTab(null);
        }
        return COMPONENT_BLOCKS_REGISTRY.get(location);
    }
}
