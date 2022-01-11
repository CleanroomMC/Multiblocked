package io.github.cleanroommc.multiblocked.api.definition;

import io.github.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import io.github.cleanroommc.multiblocked.client.renderer.IRenderer;
import net.minecraft.util.ResourceLocation;

/**
 * Definition of a component.
 */
public abstract class ComponentDefinition {
    public final ResourceLocation location;
    public final Class<? extends ComponentTileEntity<?>> clazz;
    public IRenderer baseRenderer;
    public boolean isOpaqueCube;

    protected ComponentDefinition(ResourceLocation location, Class<? extends ComponentTileEntity<?>> clazz) {
        this.location = location;
        this.clazz = clazz;
        this.baseRenderer = null;
        this.isOpaqueCube = true;
    }

    public ComponentTileEntity<?> createNewTileEntity(){
        try {
            ComponentTileEntity<?> component = clazz.newInstance();
            component.setDefinition(this);
            return component;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public IRenderer getRenderer() {
        return baseRenderer;
    }
}
