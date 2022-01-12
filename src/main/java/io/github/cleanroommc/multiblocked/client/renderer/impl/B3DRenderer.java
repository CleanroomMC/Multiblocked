package io.github.cleanroommc.multiblocked.client.renderer.impl;

import io.github.cleanroommc.multiblocked.Multiblocked;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.b3d.B3DLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class B3DRenderer extends IModelRenderer {
    @SideOnly(Side.CLIENT)
    private IModel model;

    public B3DRenderer(ResourceLocation modelLocation) {
        super(modelLocation);
    }

    @Override
    protected IModel getModel() {
        return model;
    }

    @Override
    public void register(TextureMap map) {
        try {
            model = B3DLoader.INSTANCE.loadModel(modelLocation);
        } catch (Exception e) {
            Multiblocked.LOGGER.error(e);
        }
    }

}
