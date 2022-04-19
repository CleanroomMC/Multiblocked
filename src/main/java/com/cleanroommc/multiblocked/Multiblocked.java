package com.cleanroommc.multiblocked;

import com.cleanroommc.multiblocked.api.json.BlockTypeAdapterFactory;
import com.cleanroommc.multiblocked.api.json.FluidStackTypeAdapter;
import com.cleanroommc.multiblocked.api.json.IBlockStateTypeAdapterFactory;
import com.cleanroommc.multiblocked.api.json.IRendererTypeAdapterFactory;
import com.cleanroommc.multiblocked.api.json.ItemStackTypeAdapter;
import com.cleanroommc.multiblocked.api.json.RecipeMapTypeAdapter;
import com.cleanroommc.multiblocked.api.json.RecipeTypeAdapter;
import com.cleanroommc.multiblocked.api.json.SimplePredicateFactory;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.api.recipe.RecipeMap;
import com.cleanroommc.multiblocked.api.tile.BlueprintTableTileEntity;
import com.cleanroommc.multiblocked.command.CommandClient;
import com.cleanroommc.multiblocked.jei.JeiPlugin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
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
    public static final String NAME = "Multiblocked";
    public static final String VERSION = "0.4.0";
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

    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs("example_tab") {
        @Override
        @Nonnull
        public ItemStack createIcon() {
            return BlueprintTableTileEntity.tableDefinition.getStackForm();
        }
    };

    static {
        location = new File(Loader.instance().getConfigDir(), "multiblocked");
        location.mkdir();
        new File(location, "assets").mkdir();
    }


    public static boolean isClient() {
        return FMLLaunchHandler.side().isClient();
    }

    public static String prettyJson(String uglyJson) {
        return GSON_PRETTY.toJson(new JsonParser().parse(uglyJson));
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandClient("mbd_test"));
        event.registerServerCommand(new CommandClient("mbd_reload_shaders"));
        event.registerServerCommand(new CommandClient("mbd_tps"));
    }

    @EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        if (Multiblocked.isClient() && Loader.isModLoaded(Multiblocked.MODID_JEI)) {
            JeiPlugin.setupInputHandler();
        }
    }
}
