package com.cleanroommc.multiblocked.api.registry;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.block.BlockComponent;
import com.cleanroommc.multiblocked.api.block.ItemComponent;
import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import com.cleanroommc.multiblocked.api.tile.DummyComponentTileEntity;
import com.cleanroommc.multiblocked.jei.multipage.MultiblockInfoCategory;
import com.cleanroommc.multiblocked.util.FileUtility;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class MbdComponents {
    public static final Set<Class <? extends TileEntity>> CLASS_SET = new HashSet<>();
    public static final BiMap<ResourceLocation, ComponentDefinition> DEFINITION_REGISTRY = HashBiMap.create();
    public static final BiMap<ResourceLocation, BlockComponent> COMPONENT_BLOCKS_REGISTRY = HashBiMap.create();
    public static final BiMap<ResourceLocation, ItemComponent> COMPONENT_ITEMS_REGISTRY = HashBiMap.create();
    public static final Map<ItemStack, ControllerDefinition[]> NO_NEED_CONTROLLER_MB = new HashMap<>();
    public static final Set<Item> CATALYST_SET = new HashSet<>();
    public static final BlockComponent DummyComponentBlock;
    public static final ItemComponent DummyComponentItem;

    static {
        ComponentDefinition definition = new ComponentDefinition(new ResourceLocation(Multiblocked.MODID, "dummy_component"), DummyComponentTileEntity.class);
        definition.properties.isOpaque = false;
        definition.properties.showInJei = false;
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
        if (definition instanceof ControllerDefinition && Loader.isModLoaded(Multiblocked.MODID_JEI)) {
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

    @SuppressWarnings("unchecked")
    public static <T extends ComponentDefinition> void registerComponentFromFile(File location, Class<T> clazz, BiConsumer<T, JsonObject> postHandler) {
        for (File file : Optional.ofNullable(location.listFiles((f, n) -> n.endsWith(".json"))).orElse(new File[0])) {
            try {
                JsonObject config = (JsonObject) FileUtility.loadJson(file);
                Constructor<?> constructor = Arrays.stream(clazz.getDeclaredConstructors())
                        .filter(c -> {
                            if (c.getParameterCount() != 1) return false;
                            Class<?>[] classes = c.getParameterTypes();
                            return ResourceLocation.class.isAssignableFrom(classes[0]);
                        }).findFirst().orElseThrow(() -> new IllegalArgumentException("cant find the constructor with the parameters(resourcelocation)"));
                T definition = (T) constructor.newInstance(new ResourceLocation(config.get("location").getAsString()));
                definition.fromJson(config);
                registerComponent(definition);
                if (postHandler != null) {
                    handlers.add(()->postHandler.accept(definition, config));
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


    public static void registerNoNeedController(ItemStack catalyst, ControllerDefinition definition) {
        CATALYST_SET.add(catalyst.getItem());
        ItemStack key = catalyst;
        for (ItemStack itemStack : NO_NEED_CONTROLLER_MB.keySet()) {
            if (ItemStack.areItemsEqual(itemStack, catalyst) && ItemStack.areItemStackTagsEqual(itemStack, catalyst)) {
                key = itemStack;
                break;
            }
        }
        NO_NEED_CONTROLLER_MB.put(key, ArrayUtils.add(NO_NEED_CONTROLLER_MB.get(catalyst), definition));
    }

    public static ControllerDefinition[] checkNoNeedController(ItemStack catalyst) {
        if (catalyst == null) return new ControllerDefinition[0];
        if (CATALYST_SET.contains(catalyst.getItem())) {
            for (ItemStack itemStack : NO_NEED_CONTROLLER_MB.keySet()) {
                if (ItemStack.areItemStackTagsEqual(itemStack, catalyst) && ItemStack.areItemStackTagsEqual(itemStack, catalyst)) {
                    return NO_NEED_CONTROLLER_MB.get(itemStack);
                }
            }
        }
        return new ControllerDefinition[0];
    }

    @SuppressWarnings("unchecked")
    public static <T extends ComponentDefinition> void registerComponentFromResource(Class<?> source, ResourceLocation location, Class<T> clazz, BiConsumer<T, JsonObject> postHandler) {
        try {
            InputStream inputstream = source.getResourceAsStream(String.format("/assets/%s/definition/%s.json", location.getNamespace(), location.getPath()));
            JsonObject config = FileUtility.jsonParser.parse(new InputStreamReader(inputstream)).getAsJsonObject();
            Constructor<?> constructor = Arrays.stream(clazz.getDeclaredConstructors())
                    .filter(c -> {
                        if (c.getParameterCount() != 1) return false;
                        Class<?>[] classes = c.getParameterTypes();
                        return ResourceLocation.class.isAssignableFrom(classes[0]);
                    }).findFirst().orElseThrow(() -> new IllegalArgumentException("cant find the constructor with the parameters(resourcelocation)"));
            T definition = (T) constructor.newInstance(new ResourceLocation(config.get("location").getAsString()));
            definition.fromJson(config);
            registerComponent(definition);
            if (postHandler != null) {
                handlers.add(()->postHandler.accept(definition, config));
            }
        } catch (Exception e) {
            Multiblocked.LOGGER.error("error while loading the definition resource {}", location.toString());
        }
    }

}
