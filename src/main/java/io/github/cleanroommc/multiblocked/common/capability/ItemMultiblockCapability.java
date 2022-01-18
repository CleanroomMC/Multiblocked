package io.github.cleanroommc.multiblocked.common.capability;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.recipe.ItemsIngredient;
import io.github.cleanroommc.multiblocked.common.capability.proxy.ItemCapabilityProxy;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class ItemMultiblockCapability extends MultiblockCapability<ItemsIngredient> {

    private static IBlockState[] scannedStates;

    private static IBlockState[] scanForCandidates() {
        if (scannedStates == null) {
            scannedStates = ForgeRegistries.BLOCKS.getValuesCollection()
                    .stream()
                    .map(Block::getBlockState)
                    .map(BlockStateContainer::getValidStates)
                    .flatMap(Collection::stream)
                    .filter(s -> {
                        TileEntity tile = s.getBlock().createTileEntity(Minecraft.getMinecraft().world, s);
                        if (tile == null) {
                            return false;
                        }
                        if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                            return true;
                        }
                        for (EnumFacing facing : EnumFacing.VALUES) {
                            if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .toArray(IBlockState[]::new);
        }
        Multiblocked.LOGGER.info("Available blocks for item capability: {}", Arrays.stream(scannedStates).map(s -> s.getBlock()).distinct().map(b -> b.getLocalizedName()).collect(Collectors.joining(", ")));
        return scannedStates;
    }

    public ItemMultiblockCapability(String name) {
        super(name);
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    }

    @Override
    public ItemCapabilityProxy createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new ItemCapabilityProxy(tileEntity);
    }

    @Override
    public IBlockState[] getCandidates(IO io) {
        return scanForCandidates();
        // return new IBlockState[]{
                // Blocks.CHEST.getDefaultState(),
                // Blocks.FURNACE.getDefaultState(),
                // Blocks.WHITE_SHULKER_BOX.getDefaultState()};
    }
}
