package io.github.cleanroommc.multiblocked.util;


import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class DistRayTraceResult extends RayTraceResult {
    public double dist;
    public DistRayTraceResult(Vec3d hitVecIn, EnumFacing sideHitIn, BlockPos blockPosIn, double dist) {
        super(hitVecIn, sideHitIn, blockPosIn);
        this.dist = dist;
    }
}
