package io.github.cleanroommc.multiblocked.client.renderer.impl;

import io.github.cleanroommc.multiblocked.Multiblocked;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;


public class OBJRenderer extends IModelRenderer {

    public OBJRenderer(ResourceLocation modelLocation) {
        super(modelLocation);
    }

    @Override
    protected IModel getModel() {
        try {
            return OBJLoader.INSTANCE.loadModel(modelLocation);
        } catch (Exception e) {
            Multiblocked.LOGGER.error(e);
        }
        return ModelLoaderRegistry.getMissingModel();
    }


}
