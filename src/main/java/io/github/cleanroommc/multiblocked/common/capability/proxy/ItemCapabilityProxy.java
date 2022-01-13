package io.github.cleanroommc.multiblocked.common.capability.proxy;

import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import net.minecraft.tileentity.TileEntity;

public class ItemCapabilityProxy extends CapabilityProxy {
    public ItemCapabilityProxy(TileEntity tileEntity) {
        super(tileEntity);
    }

    @Override
    public void handleRecipeInput(Recipe recipe) {

    }
}
