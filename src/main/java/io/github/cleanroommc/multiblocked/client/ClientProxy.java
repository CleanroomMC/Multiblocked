package io.github.cleanroommc.multiblocked.client;

import io.github.cleanroommc.multiblocked.CommonProxy;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import io.github.cleanroommc.multiblocked.client.model.BakedModelHandler;
import net.minecraft.client.resources.IResourcePack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.util.List;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        super.preInit();
        try {
            Field resourcePackListField = FMLClientHandler.class.getDeclaredField("resourcePackList");
            resourcePackListField.setAccessible(true);
            List<IResourcePack> resourcePackList = (List<IResourcePack>) resourcePackListField.get(FMLClientHandler.instance());
            resourcePackList.add(MultiblockedResourceLoader.INSTANCE);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        MultiblockComponents.registerModels();
        MinecraftForge.EVENT_BUS.register(BakedModelHandler.INSTANCE);
    }
}
