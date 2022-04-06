package com.cleanroommc.multiblocked.api.tile;

import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;

public class DummyComponentTileEntity extends ComponentTileEntity<ComponentDefinition> {
    public boolean isFormed;

    @Override
    public boolean isFormed() {
        return isFormed;
    }
}
