package team.chisel.ctm.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface IFacade {
    /** @deprecated */
    @Nonnull
    @Deprecated
    IBlockState getFacade(@Nonnull IBlockAccess var1, @Nonnull BlockPos var2, @Nullable EnumFacing var3);

    @Nonnull
    default IBlockState getFacade(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing side, @Nonnull BlockPos connection) {
        return this.getFacade(world, pos, side);
    }
}