package io.github.cleanroommc.multiblocked.client;

import io.github.cleanroommc.multiblocked.CommonProxy;
import io.github.cleanroommc.multiblocked.api.block.BlockComponent;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import io.github.cleanroommc.multiblocked.client.renderer.ComponentRenderer;
import io.github.cleanroommc.multiblocked.client.renderer.IRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    public static List<IRenderer> registerNeeds = new ArrayList<>();

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        MultiblockComponents.registerModels();
    }

    @SubscribeEvent
    public static void registerTextures(TextureStitchEvent.Pre event) {
        TextureMap map = event.getMap();
        for (IRenderer renderer : registerNeeds) {
            renderer.register(map);
        }
    }

    @SubscribeEvent
    public static void onModelsBake(ModelBakeEvent event) {
        event.getModelRegistry().putObject(BlockComponent.MODEL_LOCATION, ComponentRenderer.INSTANCE);
    }
}