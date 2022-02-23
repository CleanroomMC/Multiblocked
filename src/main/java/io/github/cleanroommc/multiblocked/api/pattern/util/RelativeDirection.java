package io.github.cleanroommc.multiblocked.api.pattern.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;

import java.util.function.Function;

/**
 * Relative direction when facing horizontally
 */
public enum RelativeDirection {
    UP(f -> EnumFacing.UP, EnumFacing.Axis.Y),
    DOWN(f -> EnumFacing.DOWN, EnumFacing.Axis.Y),
    LEFT(EnumFacing::rotateYCCW, EnumFacing.Axis.X),
    RIGHT(EnumFacing::rotateY, EnumFacing.Axis.X),
    FRONT(Function.identity(), EnumFacing.Axis.Z),
    BACK(EnumFacing::getOpposite, EnumFacing.Axis.Z);

    final Function<EnumFacing, EnumFacing> actualFacing;
    public final EnumFacing.Axis axis;

    RelativeDirection(Function<EnumFacing, EnumFacing> actualFacing, EnumFacing.Axis axis) {
        this.actualFacing = actualFacing;
        this.axis = axis;
    }

    public EnumFacing getActualFacing(EnumFacing facing) {
        return actualFacing.apply(facing);
    }

    public boolean isSameAxis(RelativeDirection dir) {
        return this.axis == dir.axis;
    }

}
