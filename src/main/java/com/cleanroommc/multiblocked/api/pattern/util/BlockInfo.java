package com.cleanroommc.multiblocked.api.pattern.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockInfo {

    public static final BlockInfo EMPTY = new BlockInfo(Blocks.AIR);

    private final IBlockState blockState;
    private final TileEntity tileEntity;
    private final ItemStack itemStack;

    public BlockInfo(Block block) {
        this(block.getDefaultState());
    }

    public BlockInfo(IBlockState blockState) {
        this(blockState, null);
    }

    public BlockInfo(IBlockState blockState, TileEntity tileEntity) {
        this(blockState, tileEntity, null);
    }

    public BlockInfo(IBlockState blockState, TileEntity tileEntity, ItemStack itemStack) {
        this.blockState = blockState;
        this.tileEntity = tileEntity;
        this.itemStack = itemStack;
    }

    public IBlockState getBlockState() {
        return blockState;
    }

    public TileEntity getTileEntity() {
        return tileEntity;
    }

    public ItemStack getItemStackForm() {
        return itemStack == null ? new ItemStack(Item.getItemFromBlock(blockState.getBlock()), 1, blockState.getBlock().damageDropped(blockState)) : itemStack;
    }

    public void apply(World world, BlockPos pos) {
        world.setBlockState(pos, blockState);
        if (tileEntity != null) {
            world.setTileEntity(pos, tileEntity);
        }
    }
}
