package com.cleanroommc.multiblocked.client.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

import static net.minecraft.util.EnumFacing.DOWN;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.NORTH;
import static net.minecraft.util.EnumFacing.SOUTH;
import static net.minecraft.util.EnumFacing.UP;
import static net.minecraft.util.EnumFacing.WEST;

/**
 * Mostly based on and (copied from) ThermalDynamics with minor tweaks
 * https://github.com/CoFH/ThermalDynamics/
 */
@SideOnly(Side.CLIENT)
public class FacadeBlockAccess implements IBlockAccess {

    public final IBlockAccess world;
    public final BlockPos pos;
    public final EnumFacing side;
    public final IBlockState state;

    public FacadeBlockAccess(IBlockAccess world, BlockPos pos, EnumFacing side, IBlockState state) {

        this.world = world;
        this.pos = pos;
        this.side = side;
        this.state = state;
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
        return getAction(pos) == Result.ORIGINAL ? world.getTileEntity(pos) : null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getCombinedLight(@Nonnull BlockPos pos, int t) {
        if (((side == DOWN && pos.getY() > this.pos.getY()) ||
                (side == UP && pos.getY() < this.pos.getY()) ||
                (side == NORTH && pos.getZ() > this.pos.getZ()) ||
                (side == SOUTH && pos.getZ() < this.pos.getZ()) ||
                (side == WEST && pos.getX() > this.pos.getX()) ||
                (side == EAST && pos.getX() < this.pos.getX()))) {
            return world.getCombinedLight(this.pos, t);
        }
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
