package io.github.cleanroommc.multiblocked.api.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.block.BlockComponent;
import io.github.cleanroommc.multiblocked.api.block.ItemComponent;
import io.github.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import io.github.cleanroommc.multiblocked.api.definition.ControllerDefinition;
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
    public static final BiMap<ResourceLocation, ItemComponent> COMPONENT_ITEMS_REGISTRY = HashBiMap.create();

    public static void registerComponent(ComponentDefinition definition) {
        if (DEFINITION_REGISTRY.containsKey(definition.location)) return;
        DEFINITION_REGISTRY.put(definition.location, definition);
        COMPONENT_ITEMS_REGISTRY.computeIfAbsent(definition.location, x ->
                new ItemComponent((BlockComponent) COMPONENT_BLOCKS_REGISTRY.computeIfAbsent(definition.location, X ->
                        new BlockComponent(definition)).setRegistryName(definition.location)))
                .setRegistryName(definition.location);
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
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        COMPONENT_BLOCKS_REGISTRY.values().forEach(BlockComponent::onModelRegister);
    }
    
}
