package com.cleanroommc.multiblocked.client;

import com.cleanroommc.multiblocked.CommonProxy;
import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.block.BlockComponent;
import com.cleanroommc.multiblocked.api.registry.MbdComponents;
import com.cleanroommc.multiblocked.api.registry.MbdItems;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.cleanroommc.multiblocked.client.model.custommodel.MetadataSectionEmissive;
import com.cleanroommc.multiblocked.client.particle.ParticleManager;
import com.cleanroommc.multiblocked.client.renderer.BlueprintRegionRenderer;
import com.cleanroommc.multiblocked.client.renderer.ComponentRenderer;
import com.cleanroommc.multiblocked.client.renderer.ComponentTESR;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.cleanroommc.multiblocked.client.shader.Shaders;
import com.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        super.preInit();
        Shaders.init();
        MetadataSerializer metadataSerializer = ObfuscationReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "field_110452_an");
        metadataSerializer.registerMetadataSectionType(new MetadataSectionEmissive.Serializer(), MetadataSectionEmissive.class);
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        MbdComponents.registerModels();
        MbdItems.registerModels();
        ClientRegistry.bindTileEntitySpecialRenderer(ComponentTileEntity.class, new ComponentTESR());
    }

    @SubscribeEvent
    public static void registerTextures(TextureStitchEvent.Pre event) {
        TextureMap map = event.getMap();
        map.registerSprite(new ResourceLocation("multiblocked:void"));
        map.registerSprite(new ResourceLocation("multiblocked:blocks/gregtech_base"));
        map.registerSprite(new ResourceLocation("multiblocked:blocks/gregtech_front"));
        for (IRenderer renderer : IRenderer.registerNeeds) {
            renderer.onTextureSwitchEvent(map);
        }
    }

    @SubscribeEvent
    public static void onModelsBake(ModelBakeEvent event) {
        event.getModelRegistry().putObject(BlockComponent.MODEL_LOCATION, ComponentRenderer.INSTANCE);
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        MultiblockWorldSavedData.clearDisabled();
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        BlueprintRegionRenderer.render(event);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        ParticleManager.clientTick(event);
    }

    @SubscribeEvent
    public static void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event) {
        if (event instanceof RenderGameOverlayEvent.Text) {
            ParticleManager.debugOverlay((RenderGameOverlayEvent.Text) event);
        }
    }
}
