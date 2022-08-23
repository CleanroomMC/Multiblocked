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
import com.cleanroommc.multiblocked.api.recipe.RecipeMap;
import com.cleanroommc.multiblocked.api.registry.*;
import com.cleanroommc.multiblocked.api.tile.BlueprintTableTileEntity;
import com.cleanroommc.multiblocked.api.tile.ControllerTileTesterEntity;
import com.cleanroommc.multiblocked.api.tile.part.PartTileTesterEntity;
import com.cleanroommc.multiblocked.client.renderer.impl.CycleBlockStateRenderer;
import com.cleanroommc.multiblocked.core.asm.DynamicTileEntityGenerator;
import com.cleanroommc.multiblocked.network.MultiblockedNetworking;
import com.cleanroommc.multiblocked.util.FileUtility;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import crafttweaker.CraftTweakerAPI;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
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
            capability.getAnyBlock().definition.getBaseStatus().setRenderer(new CycleBlockStateRenderer(capability.getCandidates()));
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
        // register builtin components
        MbdComponents.registerComponentFromResource(Multiblocked.class, new ResourceLocation(Multiblocked.MODID, "part/mbd_energy_input"), PartDefinition.class, CommonProxy::partPost);
        MbdComponents.registerComponentFromResource(Multiblocked.class, new ResourceLocation(Multiblocked.MODID, "part/mbd_energy_output"), PartDefinition.class, CommonProxy::partPost);
        MbdComponents.registerComponentFromResource(Multiblocked.class, new ResourceLocation(Multiblocked.MODID, "part/mbd_item_input"), PartDefinition.class, CommonProxy::partPost);
        MbdComponents.registerComponentFromResource(Multiblocked.class, new ResourceLocation(Multiblocked.MODID, "part/mbd_item_output"), PartDefinition.class, CommonProxy::partPost);
        MbdComponents.registerComponentFromResource(Multiblocked.class, new ResourceLocation(Multiblocked.MODID, "part/mbd_fluid_input"), PartDefinition.class, CommonProxy::partPost);
        MbdComponents.registerComponentFromResource(Multiblocked.class, new ResourceLocation(Multiblocked.MODID, "part/mbd_fluid_output"), PartDefinition.class, CommonProxy::partPost);
        MbdComponents.registerComponentFromResource(Multiblocked.class, new ResourceLocation(Multiblocked.MODID, "part/mbd_entity"), PartDefinition.class, CommonProxy::partPost);
        // register JsonFiles
        MbdComponents.registerComponentFromFile(
                new File(Multiblocked.location, "definition/controller"),
                ControllerDefinition.class,
                CommonProxy::controllerPost);
        MbdComponents.registerComponentFromFile(
                new File(Multiblocked.location, "definition/part"),
                PartDefinition.class,
                CommonProxy::partPost);
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        File file = new File(Multiblocked.location, "assets/multiblocked/sounds.json");
        if (file.exists() && file.isFile()) {
            JsonElement sounds = FileUtility.loadJson(file);
            if (sounds instanceof JsonObject) {
                IForgeRegistry<SoundEvent> registry = event.getRegistry();
                for (Map.Entry<String, JsonElement> sound : sounds.getAsJsonObject().entrySet()) {
                    SoundEvent soundEvent = new SoundEvent(new ResourceLocation(Multiblocked.MODID, sound.getKey()));
                    registry.register(soundEvent.setRegistryName(new ResourceLocation(Multiblocked.MODID, sound.getKey())));
                }
            }
        }
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
            Class<?> teClazz = new DynamicTileEntityGenerator(definition.location.getPath(), useInterfaceTraits, definition.clazz).generateClass();
            definition.setTileEntityClass(teClazz);
            GameRegistry.registerTileEntity(((Class<TileEntity>) teClazz), definition.location);
        }
    }

    public static void controllerPost(ControllerDefinition definition, JsonObject config) {
        componentPost(definition, config);
        if (definition.noNeedController) {
            ItemStack catalyst = definition.getCatalyst();
            if (catalyst != null && !catalyst.isEmpty()) {
                MbdComponents.registerNoNeedController(catalyst, definition);
            }
        }
    }

    public static void partPost(PartDefinition definition, JsonObject config) {
        componentPost(definition, config);
    }
}

