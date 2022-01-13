package io.github.cleanroommc.multiblocked.common.capability;

import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.recipe.ItemsIngredient;
import io.github.cleanroommc.multiblocked.common.capability.proxy.ItemCapabilityProxy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;

public class ItemMultiblockCapability extends MultiblockCapability<ItemsIngredient> {

    public ItemMultiblockCapability(IO io) {
        super(io);
    }

    @Override
    public ItemsIngredient copyContent(ItemsIngredient content) {
        return content.copy();
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull TileEntity tileEntity) {
        return tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    }

    @Override
    public ItemCapabilityProxy createProxy(@Nonnull TileEntity tileEntity) {
        return new ItemCapabilityProxy(tileEntity);
    }

    @Override
    public IBlockState getCandidate() {
        return null;
    }
}
