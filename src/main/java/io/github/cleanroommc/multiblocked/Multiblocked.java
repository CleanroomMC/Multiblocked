package io.github.cleanroommc.multiblocked;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import io.github.cleanroommc.multiblocked.api.json.BlockTypeAdapterFactory;
import io.github.cleanroommc.multiblocked.api.json.FluidStackTypeAdapter;
import io.github.cleanroommc.multiblocked.api.json.GeoComponentRendererTypeAdapter;
import io.github.cleanroommc.multiblocked.api.json.IBlockStateTypeAdapterFactory;
import io.github.cleanroommc.multiblocked.api.json.IRendererTypeAdapterFactory;
import io.github.cleanroommc.multiblocked.api.json.ItemStackTypeAdapter;
import io.github.cleanroommc.multiblocked.api.json.SimplePredicateTypeAdapter;
import io.github.cleanroommc.multiblocked.api.pattern.predicates.SimplePredicate;
import io.github.cleanroommc.multiblocked.api.tile.BlueprintTableTileEntity;
import io.github.cleanroommc.multiblocked.client.renderer.impl.GeoComponentRenderer;
import io.github.cleanroommc.multiblocked.command.CommandReload;
import io.github.cleanroommc.multiblocked.jei.JeiPlugin;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Mod(modid = Multiblocked.MODID,
        name = Multiblocked.NAME,
        version = Multiblocked.VERSION,
        dependencies = "after:jei@[4.15.0,);after:crafttweaker")
public class Multiblocked {

    public static final String MODID = "multiblocked";
    public static final String MODID_CT = "crafttweaker";
    public static final String MODID_JEI = "jei";
    public static final String MODID_BOT = "botania";
    public static final String MODID_QMD = "qmd";
    public static final String MODID_TC6 = "thaumcraft";
    public static final String MODID_MEK = "mekanism";
    public static final String MODID_GEO = "geckolib3";
    public static final String NAME = "Multiblocked";
    public static final String VERSION = "1.0";
    public static final Logger LOGGER = LogManager.getLogger(NAME);
    public static final Random RNG = new Random();
    public static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();
    public static Gson GSON;

    @SidedProxy(modId = MODID, clientSide = "io.github.cleanroommc.multiblocked.client.ClientProxy", serverSide = "io.github.cleanroommc.multiblocked.CommonProxy")
    public static CommonProxy proxy;

    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs("example_tab") {
        @Override
        @Nonnull
        public ItemStack createIcon() {
            return BlueprintTableTileEntity.tableDefinition.getStackForm();
        }
    };

    private static Boolean isClient;

    public static boolean isClient() {
        if (isClient == null) isClient = FMLCommonHandler.instance().getSide().isClient();
        return isClient;
    }

    public static String prettyJson(String uglyJson) {
        return GSON_PRETTY.toJson(new JsonParser().parse(uglyJson));
    }

    private static final ConcurrentMap<String, Boolean> loadedCache = new ConcurrentHashMap<>();

    public static boolean isModLoaded(String modid) {
        return loadedCache.computeIfAbsent(modid, id -> Loader.instance().getIndexedModList().containsKey(modid));
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .registerTypeAdapterFactory(IBlockStateTypeAdapterFactory.INSTANCE)
                .registerTypeAdapterFactory(IRendererTypeAdapterFactory.INSTANCE)
                .registerTypeAdapterFactory(BlockTypeAdapterFactory.INSTANCE)
                .registerTypeAdapter(ItemStack.class, ItemStackTypeAdapter.INSTANCE)
                .registerTypeAdapter(FluidStack.class, FluidStackTypeAdapter.INSTANCE)
                .registerTypeAdapter(SimplePredicate.class, SimplePredicateTypeAdapter.INSTANCE)
                .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer());
        if (isModLoaded(MODID_GEO)) {
            gsonBuilder.registerTypeAdapter(GeoComponentRenderer.class, GeoComponentRendererTypeAdapter.INSTANCE);
        }
        GSON = gsonBuilder.setLenient().create();
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
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
//        Multiblock.loadMultiblocks(); TODO
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandReload());
    }

    @EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        if (Multiblocked.isModLoaded(Multiblocked.MODID_JEI)) {
            JeiPlugin.setupInputHandler();
        }
    }
}
