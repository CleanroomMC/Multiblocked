package com.cleanroommc.multiblocked.api.block;

import net.minecraft.util.EnumFacing;

import java.util.function.Predicate;

/**
 * Author: KilaBash
 * Date: 2022/04/27
 * Description:
 */
public class CustomProperties {
    public RotationState rotationState;
    public boolean showInJei;
    public boolean isOpaque;
    public boolean hasCollision;
    public float destroyTime;
    public float explosionResistance;
    public int harvestLevel;
    public int stackSize;
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

    public enum RotationState implements Predicate<EnumFacing> {
        ALL(dir -> true),
        NONE(dir -> false),
        Y_AXIS(dir -> dir.getAxis() == EnumFacing.Axis.Y),
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
