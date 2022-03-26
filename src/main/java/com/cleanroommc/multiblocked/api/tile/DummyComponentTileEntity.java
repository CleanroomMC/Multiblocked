package com.cleanroommc.multiblocked.api.tile;

import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.cleanroommc.multiblocked.api.tile.part.PartTileEntity;

public class DummyComponentTileEntity extends ComponentTileEntity<ComponentDefinition> {
    public boolean isFormed;

    public IRenderer renderer;

    @Override
    public boolean isFormed() {
        return isFormed;
    }

    public DummyComponentTileEntity setRenderer(IRenderer renderer) {
        this.renderer = renderer;
        return this;
    }

    @Override
    public IRenderer getRenderer() {
        return renderer == null ? super.getRenderer() : renderer;
    }
}
