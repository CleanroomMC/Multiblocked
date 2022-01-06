package io.github.cleanroommc.multiblocked;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.cleanroommc.multiblocked.api.framework.structure.Layout;
import io.github.cleanroommc.multiblocked.api.framework.structure.Multiblock;
import io.github.cleanroommc.multiblocked.api.framework.structure.definition.IDefinition;
import io.github.cleanroommc.multiblocked.command.CommandReloadDefinitions;
import io.github.cleanroommc.multiblocked.events.Listeners;
import io.github.cleanroommc.multiblocked.network.MultiblockedNetworking;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.github.cleanroommc.multiblocked.util.ItemStackDeserializer;

import java.lang.invoke.MethodHandles;

@Mod(modid = Multiblocked.MODID, name = Multiblocked.NAME, version = Multiblocked.VERSION)
public class Multiblocked {

    public static final String MODID = "multiblocked";
    public static final String NAME = "Multiblock'd";
    public static final String VERSION = "1.0";

    public static final Logger LOGGER = LogManager.getLogger(NAME);
    public static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Multiblock.class, Multiblock.DESERIALIZER)
            .registerTypeAdapter(Layout.class, Layout.DESERIALIZER)
            .registerTypeAdapter(IDefinition.class, IDefinition.DESERIALIZER)
            .registerTypeAdapter(ItemStack.class, ItemStackDeserializer.INSTANCE)
            .setLenient()
            .create();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(Listeners.class);
        MultiblockedNetworking.initializeC2S();
        MultiblockedNetworking.initializeS2C();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {

    }

    @EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        Multiblock.loadMultiblocks();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandReloadDefinitions());
    }
}
