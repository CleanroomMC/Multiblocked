package io.github.cleanroommc.multiblocked;

import io.github.cleanroommc.multiblocked.command.CommandReloadDefinitions;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Multiblocked.MODID, name = Multiblocked.NAME, version = Multiblocked.VERSION)
public class Multiblocked {

    public static final String MODID = "multiblocked";
    public static final String NAME = "Multiblock'd";
    public static final String VERSION = "1.0";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @SidedProxy(modId = MODID, clientSide = "io.github.cleanroommc.multiblocked.client.ClientProxy", serverSide = "io.github.cleanroommc.multiblocked.CommonProxy")
    public static CommonProxy proxy;

    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs("example_tab") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Items.DIAMOND);
        }
    };

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }


    @EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
//        Multiblock.loadMultiblocks(); TODO
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandReloadDefinitions());
    }
}
