package com.cleanroommc.multiblocked.api.block;

import crafttweaker.annotations.ZenRegister;
import net.minecraft.util.EnumFacing;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenProperty;

import java.util.function.Predicate;

/**
 * Author: KilaBash
 * Date: 2022/04/27
 * Description:
 */
@ZenClass("mods.multiblocked.definition.CustomProperties")
@ZenRegister
public class CustomProperties {
    @ZenProperty
    public RotationState rotationState;
    @ZenProperty
    public boolean showInJei;
    @ZenProperty
    public boolean isOpaque;
    @ZenProperty
    public boolean hasCollision;
    @ZenProperty
    public float destroyTime;
    @ZenProperty
    public float explosionResistance;
    @ZenProperty
    public int harvestLevel;
    @ZenProperty
    public int stackSize;
    @ZenProperty
    public String tabGroup;

    public CustomProperties() {
        this.isOpaque = true;
        this.destroyTime = 1.5f;
        this.explosionResistance = 10f;
        this.harvestLevel = 1;
        this.hasCollision = true;
        this.tabGroup = "multiblocked";
        this.stackSize = 64;
        this.rotationState = RotationState.ALL;
        this.showInJei = true;
    }

    @ZenClass("mods.multiblocked.definition.RotationState")
    @ZenRegister
    public enum RotationState implements Predicate<EnumFacing> {
        @ZenProperty
        ALL(dir -> true),
        @ZenProperty
        NONE(dir -> false),
        @ZenProperty
        Y_AXIS(dir -> dir.getAxis() == EnumFacing.Axis.Y),
        @ZenProperty
        NON_Y_AXIS(dir -> dir.getAxis() != EnumFacing.Axis.Y);

        final Predicate<EnumFacing> predicate;

        RotationState(Predicate<EnumFacing> predicate){
            this.predicate = predicate;
        }

        @Override
        public boolean test(EnumFacing dir) {
            return predicate.test(dir);
        }
    }
}
