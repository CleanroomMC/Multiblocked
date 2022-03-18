package io.github.cleanroommc.multiblocked.client.renderer.impl;

import io.github.cleanroommc.multiblocked.Multiblocked;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.b3d.B3DLoader;


public class B3DRenderer extends IModelRenderer {

    public B3DRenderer(ResourceLocation modelLocation) {
        super(modelLocation);
    }

    @Override
    protected IModel getModel() {
        try {
            return B3DLoader.INSTANCE.loadModel(modelLocation);
        } catch (Exception e) {
            Multiblocked.LOGGER.error(e);
        }
        return null;
    }

}
