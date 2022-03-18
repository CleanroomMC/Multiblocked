package io.github.cleanroommc.multiblocked.api.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.block.BlockComponent;
import io.github.cleanroommc.multiblocked.api.block.ItemComponent;
import io.github.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import io.github.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import io.github.cleanroommc.multiblocked.api.definition.PartDefinition;
import io.github.cleanroommc.multiblocked.api.tile.DummyComponentTileEntity;
import io.github.cleanroommc.multiblocked.jei.multipage.MultiblockInfoCategory;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashSet;
import java.util.Set;

public class MultiblockComponents {
    public static final Set<Class <? extends TileEntity>> CLASS_SET = new HashSet<>();
    public static final BiMap<ResourceLocation, ComponentDefinition> DEFINITION_REGISTRY = HashBiMap.create();
    public static final BiMap<ResourceLocation, BlockComponent> COMPONENT_BLOCKS_REGISTRY = HashBiMap.create();
    public static final BiMap<ResourceLocation, ItemComponent> COMPONENT_ITEMS_REGISTRY = HashBiMap.create();
    public static final BlockComponent DummyComponentBlock;
    public static final ItemComponent DummyComponentItem;

    static {
        PartDefinition definition = new PartDefinition(new ResourceLocation(Multiblocked.MODID, "dummy_component"), DummyComponentTileEntity.class);
        definition.isOpaqueCube = false;
        definition.showInJei = false;
        registerComponent(definition);
        DummyComponentBlock = COMPONENT_BLOCKS_REGISTRY.get(definition.location);
        DummyComponentItem = COMPONENT_ITEMS_REGISTRY.get(definition.location);
    }

    public static void registerComponent(ComponentDefinition definition) {
        if (DEFINITION_REGISTRY.containsKey(definition.location)) return;
        DEFINITION_REGISTRY.put(definition.location, definition);
        COMPONENT_ITEMS_REGISTRY.computeIfAbsent(definition.location, x ->
                new ItemComponent(COMPONENT_BLOCKS_REGISTRY.computeIfAbsent(definition.location, X ->
                        new BlockComponent(definition))));
        if (definition instanceof ControllerDefinition && Multiblocked.isModLoaded(Multiblocked.MODID_JEI)) {
            MultiblockInfoCategory.registerMultiblock((ControllerDefinition) definition);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        COMPONENT_BLOCKS_REGISTRY.values().forEach(BlockComponent::onModelRegister);
    }

    public static void registerBlocks(IForgeRegistry<Block> registry) {
        COMPONENT_BLOCKS_REGISTRY.values().forEach(registry::register);

        // register Tile Entity
        for (ComponentDefinition definition : DEFINITION_REGISTRY.values()) {
            if (!CLASS_SET.contains(definition.clazz)) {
                GameRegistry.registerTileEntity(definition.clazz, definition.location);
                CLASS_SET.add(definition.clazz);
            }
        }
    }
}
