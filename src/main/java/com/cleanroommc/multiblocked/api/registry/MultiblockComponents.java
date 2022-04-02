package com.cleanroommc.multiblocked.api.registry;

import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import com.cleanroommc.multiblocked.jei.multipage.MultiblockInfoCategory;
import com.cleanroommc.multiblocked.util.FileUtility;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.block.BlockComponent;
import com.cleanroommc.multiblocked.api.block.ItemComponent;
import com.cleanroommc.multiblocked.api.tile.DummyComponentTileEntity;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

public class MultiblockComponents {
    public static final Set<Class <? extends TileEntity>> CLASS_SET = new HashSet<>();
    public static final BiMap<ResourceLocation, ComponentDefinition> DEFINITION_REGISTRY = HashBiMap.create();
    public static final BiMap<ResourceLocation, BlockComponent> COMPONENT_BLOCKS_REGISTRY = HashBiMap.create();
    public static final BiMap<ResourceLocation, ItemComponent> COMPONENT_ITEMS_REGISTRY = HashBiMap.create();
    public static final BlockComponent DummyComponentBlock;
    public static final ItemComponent DummyComponentItem;

    static {
        ComponentDefinition definition = new ComponentDefinition(new ResourceLocation(Multiblocked.MODID, "dummy_component"), DummyComponentTileEntity.class);
        definition.isOpaqueCube = false;
        definition.showInJei = false;
        registerComponent(definition);
        DummyComponentBlock = COMPONENT_BLOCKS_REGISTRY.get(definition.location);
        DummyComponentItem = COMPONENT_ITEMS_REGISTRY.get(definition.location);
        DummyComponentBlock.setCreativeTab(null);
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

    public static List<Runnable> handlers = new ArrayList<>();

    public static <T extends ComponentDefinition> void registerComponentFromFile(Gson gson, File location, Class<T> clazz, BiConsumer<T, JsonObject> postHandler) {
        for (File file : Optional.ofNullable(location.listFiles((f, n) -> n.endsWith(".json"))).orElse(new File[0])) {
            try {
                JsonObject config = (JsonObject) FileUtility.loadJson(file);
                T definition = gson.fromJson(config, clazz);
                if (definition != null) {
                    registerComponent(definition);
                    if (postHandler != null) {
                        handlers.add(()->postHandler.accept(definition, config));
                    }
                }
            } catch (Exception e) {
                Multiblocked.LOGGER.error("error while loading the definition file {}", file.toString());
            }
        }

    }

    public static void executeInitHandler() {
        handlers.forEach(Runnable::run);
        handlers.clear();
    }
}
