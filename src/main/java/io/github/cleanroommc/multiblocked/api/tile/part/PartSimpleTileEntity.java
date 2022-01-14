package io.github.cleanroommc.multiblocked.api.tile.part;

import io.github.cleanroommc.multiblocked.api.definition.PartDefinition;
import net.minecraft.util.EnumFacing;

/**
 * Simple component of the multiblock. Used for rendering formed rendering.
 */
public class PartSimpleTileEntity extends PartTileEntity<PartDefinition> {
    @Override
    public boolean isFormed() {
        return false;
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return false;
    }
}
