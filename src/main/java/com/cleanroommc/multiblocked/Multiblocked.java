package com.cleanroommc.multiblocked;

import com.cleanroommc.multiblocked.api.json.*;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.api.recipe.RecipeMap;
import com.cleanroommc.multiblocked.api.tile.BlueprintTableTileEntity;
import com.cleanroommc.multiblocked.command.CommandMbdTree;
import com.cleanroommc.multiblocked.integration.InfoProviders;
import com.cleanroommc.multiblocked.jei.JeiPlugin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Random;

@Mod(modid = Multiblocked.MODID,
        name = Multiblocked.NAME,
        version = Multiblocked.VERSION,
        acceptedMinecraftVersions = "1.12.2",
        dependencies = "required:mixinbooter@[4.2,);after:jei@[4.15.0,);after:crafttweaker")
public class Multiblocked {

    public static final String MODID = "multiblocked";
    public static final String MODID_CT = "crafttweaker";
    public static final String MODID_JEI = "jei";
    public static final String MODID_BOT = "botania";
    public static final String MODID_QMD = "qmd";
    public static final String MODID_TC6 = "thaumcraft";
    public static final String MODID_THAUMJEI = "thaumicjei";
    public static final String MODID_MEK = "mekanism";
    public static final String MODID_GEO = "geckolib3";
    public static final String MODID_GTCE = "gregtech";
    public static final String MODID_LC = "lightningcraft";
    public static final String MODID_TOP = "theoneprobe";
    public static final String MODID_PRODIGY = "prodigytech";
    public static final String MODID_NA = "naturesaura";
    public static final String MODID_EU2 = "extrautils2";
    public static final String MODID_PE = "projecte";
    public static final String MODID_BG = "bloodmagic";
    public static final String MODID_EMBERS = "embers";
    public static final String MODID_TA = "thaumicaugmentation";
    public static final String MODID_PNC = "pneumaticcraft";
    public static final String MODID_AS = "astralsorcery";
    public static final String NAME = "Multiblocked";
    public static final String VERSION = "0.6.6";
    public static final Logger LOGGER = LogManager.getLogger(NAME);
    public static final Random RNG = new Random();
    public static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(IBlockStateTypeAdapterFactory.INSTANCE)
            .registerTypeAdapterFactory(IRendererTypeAdapterFactory.INSTANCE)
            .registerTypeAdapterFactory(BlockTypeAdapterFactory.INSTANCE)
            .registerTypeAdapterFactory(SimplePredicateFactory.INSTANCE)
            .registerTypeAdapter(ItemStack.class, ItemStackTypeAdapter.INSTANCE)
            .registerTypeAdapter(FluidStack.class, FluidStackTypeAdapter.INSTANCE)
            .registerTypeAdapter(Recipe.class, RecipeTypeAdapter.INSTANCE)
            .registerTypeAdapter(RecipeMap.class, RecipeMapTypeAdapter.INSTANCE)
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .create();
    public static File location;

    @Mod.Instance(Multiblocked.MODID)
    public static Multiblocked instance;

    @SidedProxy(modId = MODID, clientSide = "com.cleanroommc.multiblocked.client.ClientProxy", serverSide = "com.cleanroommc.multiblocked.CommonProxy")
    public static CommonProxy proxy;

    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs("multiblocked") {
        @Override
        @Nonnull
        public ItemStack createIcon() {
            return BlueprintTableTileEntity.tableDefinition.getStackForm();
        }
    };


    public static boolean isClient() {
        return FMLLaunchHandler.side().isClient();
    }

    public static String prettyJson(String uglyJson) {
        return GSON_PRETTY.toJson(new JsonParser().parse(uglyJson));
    }

    public static boolean isSinglePlayer() {
        return isClient() && Minecraft.getMinecraft().isSingleplayer();
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        location = new File(Loader.instance().getConfigDir(), MbdConfig.location);
        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
        if (Loader.isModLoaded(MODID_TOP)) {
            InfoProviders.registerTOP();
        }
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandMbdTree());
    }

    @EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        if (Multiblocked.isClient() && Loader.isModLoaded(Multiblocked.MODID_JEI)) {
            JeiPlugin.setupInputHandler();
        }
    }
}
