package io.github.cleanroommc.multiblocked.api.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.github.cleanroommc.multiblocked.api.block.BlockComponent;
import io.github.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MultiblockComponents {
    public static final BiMap<ResourceLocation, ComponentDefinition> DEFINITION_REGISTRY = HashBiMap.create();
    public static final BiMap<ResourceLocation, BlockComponent> COMPONENT_BLOCKS_REGISTRY = HashBiMap.create();

    public static void registerComponent(ComponentDefinition definition) {
        DEFINITION_REGISTRY.put(definition.location, definition);
        COMPONENT_BLOCKS_REGISTRY.computeIfAbsent(definition.location, loc -> new BlockComponent(definition)).setRegistryName(definition.location);
    }

    public static void registerTileEntity() {
        for (ComponentDefinition definition : DEFINITION_REGISTRY.values()) {
            GameRegistry.registerTileEntity(definition.clazz, definition.location);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        COMPONENT_BLOCKS_REGISTRY.values().forEach(BlockComponent::onModelRegister);
    }
}
