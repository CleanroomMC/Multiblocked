package io.github.cleanroommc.multiblocked.client.renderer.impl;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.client.renderer.IRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.b3d.B3DLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;


public class B3DRenderer extends IModelRenderer {
    @SideOnly(Side.CLIENT)
    protected static Map<ResourceLocation, IModel> CACHE;

    static {
        CACHE = new HashMap<>();
    }

    @SideOnly(Side.CLIENT)
    private transient IModel model;

    public B3DRenderer(ResourceLocation modelLocation, BlockRenderLayer... renderLayers) {
        super(modelLocation, renderLayers);
    }

    public IRenderer fromJson() {
        return new B3DRenderer(modelLocation, renderLayer.toArray(new BlockRenderLayer[0]));
    }

    @Override
    public boolean isRaw() {
        return (model = CACHE.get(modelLocation)) == null;
    }

    @Override
    protected IModel getModel() {
        return model;
    }

    @Override
    public void register(TextureMap map) {
        try {
            model = B3DLoader.INSTANCE.loadModel(modelLocation);
            CACHE.put(modelLocation, model);
        } catch (Exception e) {
            Multiblocked.LOGGER.error(e);
        }
    }

}
