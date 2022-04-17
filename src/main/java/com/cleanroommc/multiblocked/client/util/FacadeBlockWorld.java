package com.cleanroommc.multiblocked.client.util;

import com.cleanroommc.multiblocked.util.world.DummyWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class FacadeBlockWorld extends DummyWorld {

    public final World world;
    public final BlockPos pos;
    public final IBlockState state;
    public final TileEntity tile;

    public FacadeBlockWorld(World world, BlockPos pos, IBlockState state, TileEntity tile) {
        this.world = world;
        this.pos = pos;
        this.state = state;
        this.tile = tile;
    }

    public enum Result {
        ORIGINAL, BASE
    }

    public Result getAction(BlockPos pos) {
        if (this.pos == pos) {
            return Result.BASE;
        }
        return Result.ORIGINAL;
    }

    @Nonnull
    @Override
    public IBlockState getBlockState(@Nonnull BlockPos pos) {
        IBlockState ret;
        Result action = getAction(pos);
        if (action == Result.ORIGINAL) {
            ret = world.getBlockState(pos);
        } else {
            ret = state;
        }
        return ret;
    }

    @Override
    public TileEntity getTileEntity(@Nonnull BlockPos pos) {
        return getAction(pos) == Result.ORIGINAL ? world.getTileEntity(pos) : tile;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getCombinedLight(@Nonnull BlockPos pos, int t) {
        return world.getCombinedLight(pos, t);
    }

    @Override
    public int getStrongPower(@Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        return world.getStrongPower(pos, side);
    }

    @Nonnull
    @Override
    public WorldType getWorldType() {
        return world.getWorldType();
    }

    @Override
    public boolean isAirBlock(@Nonnull BlockPos pos) {
        Result action = getAction(pos);
        return (action == Result.ORIGINAL && world.isAirBlock(pos));
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public Biome getBiome(@Nonnull BlockPos pos) {
        return world.getBiome(pos);
    }

    @Override
    public boolean isSideSolid(BlockPos pos, @Nonnull EnumFacing side, boolean _default) {
        if (pos.getX() < -30000000 ||
                pos.getZ() < -30000000 ||
                pos.getX() >= 30000000 ||
                pos.getZ() >= 30000000) {
            return _default;
        } else {
            return getBlockState(pos).isSideSolid(this, pos, side);
        }
    }
}
