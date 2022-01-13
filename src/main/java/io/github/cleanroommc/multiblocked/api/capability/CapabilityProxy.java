package io.github.cleanroommc.multiblocked.api.capability;

import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import net.minecraft.tileentity.TileEntity;

/**
 * The Proxy of a specific capability that has been detected {@link CapabilityDetector}. Providing I/O and such features to a controller.
 */
public abstract class CapabilityProxy {
    protected TileEntity tileEntity;

    public CapabilityProxy(TileEntity tileEntity) {
        this.tileEntity = tileEntity;
    }

    public TileEntity getTileEntity() {
        return tileEntity;
    }

    public abstract boolean isRecipeAvailable(Recipe recipe);

    public abstract void handleRecipeInput(Recipe recipe);

    public abstract void handleRecipeOutput(Recipe recipe);
}
