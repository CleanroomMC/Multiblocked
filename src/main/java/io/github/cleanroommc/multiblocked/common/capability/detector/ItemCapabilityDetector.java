package io.github.cleanroommc.multiblocked.common.capability.detector;

import io.github.cleanroommc.multiblocked.api.capability.CapabilityDetector;
import io.github.cleanroommc.multiblocked.common.capability.proxy.ItemCapabilityProxy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class ItemCapabilityDetector extends CapabilityDetector<ItemCapabilityProxy> {

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
