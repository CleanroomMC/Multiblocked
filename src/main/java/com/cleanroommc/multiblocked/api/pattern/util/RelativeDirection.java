package com.cleanroommc.multiblocked.api.pattern.util;

import crafttweaker.annotations.ZenRegister;
import net.minecraft.util.EnumFacing;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenProperty;

import java.util.function.Function;

/**
 * Relative direction when facing horizontally
 */
@ZenClass("mods.multiblocked.pattern.RelativeDirection")
@ZenRegister
public enum RelativeDirection {
    @ZenProperty
    UP(f -> EnumFacing.UP, EnumFacing.Axis.Y),
    @ZenProperty
    DOWN(f -> EnumFacing.DOWN, EnumFacing.Axis.Y),
    @ZenProperty
    LEFT(EnumFacing::rotateYCCW, EnumFacing.Axis.X),
    @ZenProperty
    RIGHT(EnumFacing::rotateY, EnumFacing.Axis.X),
    @ZenProperty
    FRONT(Function.identity(), EnumFacing.Axis.Z),
    @ZenProperty
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
