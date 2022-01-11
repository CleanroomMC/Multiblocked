package io.github.cleanroommc.multiblocked.client;

import io.github.cleanroommc.multiblocked.CommonProxy;
import io.github.cleanroommc.multiblocked.api.block.BlockComponent;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import io.github.cleanroommc.multiblocked.client.custom.CustomBlockRendererDispatcher;
import io.github.cleanroommc.multiblocked.client.custom.CustomRenderItem;
import io.github.cleanroommc.multiblocked.client.model.modelfactories.BlockComponentBakedModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.util.List;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    private static boolean initialized = false;

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

    @Override
    public void init() {
        super.init();
        if (!initialized) {
            Minecraft mc = Minecraft.getMinecraft();

            CustomBlockRendererDispatcher dispatcher = new CustomBlockRendererDispatcher(mc.getBlockRendererDispatcher());
            ObfuscationReflectionHelper.setPrivateValue(Minecraft.class, mc, dispatcher, "field_175618_aM");

            CustomRenderItem customRenderItem = new CustomRenderItem(mc.getRenderItem());
            ObfuscationReflectionHelper.setPrivateValue(Minecraft.class, mc, customRenderItem, "field_175621_X");
        }
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        MultiblockComponents.registerModels();
    }

    @SubscribeEvent
    public void onModelsBake(ModelBakeEvent event) {
        event.getModelRegistry().putObject(BlockComponent.MODEL_LOCATION, BlockComponentBakedModel.INSTANCE);
    }
}
