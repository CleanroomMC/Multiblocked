package io.github.cleanroommc.multiblocked.api.tile;

import io.github.cleanroommc.multiblocked.api.definition.PartDefinition;
import io.github.cleanroommc.multiblocked.api.tile.part.PartTileEntity;
import io.github.cleanroommc.multiblocked.client.renderer.IRenderer;

public class DummyComponentTileEntity extends PartTileEntity<PartDefinition> {
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
