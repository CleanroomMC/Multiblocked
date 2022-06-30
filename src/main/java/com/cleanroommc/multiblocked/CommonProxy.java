package com.cleanroommc.multiblocked;

import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.capability.trait.CapabilityTrait;
import com.cleanroommc.multiblocked.api.capability.trait.InterfaceUser;
import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import com.cleanroommc.multiblocked.api.gui.factory.TileEntityUIFactory;
import com.cleanroommc.multiblocked.api.gui.factory.UIFactory;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs.JsonBlockPatternWidget;
import com.cleanroommc.multiblocked.api.item.ItemMultiblockBuilder.BuilderRecipeLogic;
import com.cleanroommc.multiblocked.api.pattern.JsonBlockPattern;
import com.cleanroommc.multiblocked.api.recipe.RecipeMap;
import com.cleanroommc.multiblocked.api.registry.*;
import com.cleanroommc.multiblocked.api.tile.BlueprintTableTileEntity;
import com.cleanroommc.multiblocked.api.tile.ControllerTileTesterEntity;
import com.cleanroommc.multiblocked.api.tile.part.PartTileTesterEntity;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.BlockStateRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.CycleBlockStateRenderer;
import com.cleanroommc.multiblocked.core.asm.DynamicTileEntityGenerator;
import com.cleanroommc.multiblocked.network.MultiblockedNetworking;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import crafttweaker.CraftTweakerAPI;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import software.bernie.geckolib3.GeckoLib;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Multiblocked.MODID)
public class CommonProxy {

    public void preInit() {
        Multiblocked.LOGGER.info("pre init");
        if (Loader.isModLoaded(Multiblocked.MODID_GEO)) {
            GeckoLib.initialize();
        }
        MinecraftForge.EVENT_BUS.register(Listeners.class);
        MultiblockedNetworking.init();
        MbdCapabilities.registerCapabilities();
        MbdRenderers.registerRenderers();
        MbdPredicates.registerPredicates();
        MbdRecipeConditions.registerConditions();
    }

    public void init() {
        Multiblocked.LOGGER.info("init");
        // register recipe map
        RecipeMap.registerRecipeFromFile(Multiblocked.GSON, new File(Multiblocked.location, "recipe_map"));
        // execute init handler
        MbdComponents.executeInitHandler();
        // register ui
        UIFactory.register(TileEntityUIFactory.INSTANCE);
        // loadCT
        if (Loader.isModLoaded(Multiblocked.MODID_CT)) {
            Multiblocked.LOGGER.info("ct loader multiblocked");
            CraftTweakerAPI.tweaker.loadScript(false, "multiblocked");
        }
    }

    public void postInit() {
        Multiblocked.LOGGER.info("post init");
        for (MultiblockCapability<?> capability : MbdCapabilities.CAPABILITY_REGISTRY.values()) {
            capability.getAnyBlock().definition.baseRenderer = new CycleBlockStateRenderer(capability.getCandidates());
        }
    }

    public static void registerComponents(){
        // register any capability block
        MbdCapabilities.registerAnyCapabilityBlocks();
        // register blueprint table
        BlueprintTableTileEntity.registerBlueprintTable();
        // register controller tester
        ControllerTileTesterEntity.registerTestController();
        // register part tester
        PartTileTesterEntity.registerTestPart();
        // register JsonBlockPatternBlock
        JsonBlockPatternWidget.registerBlock();
        // register JsonFiles
        MbdComponents.registerComponentFromFile(
                Multiblocked.GSON, 
                new File(Multiblocked.location, "definition/controller"),
                ControllerDefinition.class,
                CommonProxy::controllerPost);
        MbdComponents.registerComponentFromFile(
                Multiblocked.GSON,
                new File(Multiblocked.location, "definition/part"),
                PartDefinition.class,
                CommonProxy::partPost);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        registerComponents();
        IForgeRegistry<Block> registry = event.getRegistry();
        MbdComponents.registerBlocks(registry);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        MbdComponents.COMPONENT_ITEMS_REGISTRY.values().forEach(registry::register);
        MbdItems.registerItems(registry);
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        ForgeRegistries.RECIPES.register(new BuilderRecipeLogic().setRegistryName(Multiblocked.MODID, "builder"));
    }

    @SuppressWarnings("unchecked")
    private static void componentPost(ComponentDefinition definition, JsonObject config) {
        if (definition.baseRenderer instanceof BlockStateRenderer) {
            definition.baseRenderer = Multiblocked.GSON.fromJson(config.get("baseRenderer"), IRenderer.class);
        }
        if (definition.formedRenderer instanceof BlockStateRenderer) {
            definition.formedRenderer = Multiblocked.GSON.fromJson(config.get("formedRenderer"), IRenderer.class);
        }
        if (definition.workingRenderer instanceof BlockStateRenderer) {
            definition.workingRenderer = Multiblocked.GSON.fromJson(config.get("workingRenderer"), IRenderer.class);
        }
        List<CapabilityTrait> useInterfaceTraits = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : definition.traits.entrySet()) {
            MultiblockCapability<?> capability = MbdCapabilities.get(entry.getKey());
            if (capability != null && capability.hasTrait()) {
                CapabilityTrait trait = capability.createTrait();
                if (trait.getClass().isAnnotationPresent(InterfaceUser.class)) {
                    useInterfaceTraits.add(trait);
                }
            }
        }
        if (!useInterfaceTraits.isEmpty()) {
            Class<?> teClazz = new DynamicTileEntityGenerator(definition.location.getPath(), useInterfaceTraits, definition instanceof ControllerDefinition).generateClass();
            definition.setTileEntityClass(teClazz);
            GameRegistry.registerTileEntity(((Class<TileEntity>) teClazz), definition.location);
        }
    }

    public static void controllerPost(ControllerDefinition definition, JsonObject config) {
        definition.basePattern = Multiblocked.GSON.fromJson(config.get("basePattern"), JsonBlockPattern.class).build();
        definition.recipeMap = RecipeMap.RECIPE_MAP_REGISTRY.getOrDefault(config.get("recipeMap").getAsString(), RecipeMap.EMPTY);
        componentPost(definition, config);
    }

    public static void partPost(PartDefinition definition, JsonObject config) {
        componentPost(definition, config);
    }
}

