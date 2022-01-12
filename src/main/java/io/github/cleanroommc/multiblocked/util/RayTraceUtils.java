package io.github.cleanroommc.multiblocked.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class RayTraceUtils {

    public static DistRayTraceResult rayTrace(BlockPos pos, Vector3 start, Vector3 end, AxisAlignedBB aaBB) {
        Vector3 startRay = start.copy().subtract(pos);
        Vector3 endRay = end.copy().subtract(pos);
        RayTraceResult bbResult = aaBB.calculateIntercept(startRay.vec3(), endRay.vec3());
        if (bbResult != null) {
            Vector3 hitVec = (new Vector3(bbResult.hitVec)).add(pos);
            EnumFacing sideHit = bbResult.sideHit;
            double dist = hitVec.copy().subtract(start).magSquared();
            return new DistRayTraceResult(hitVec.vec3(), sideHit, pos, dist);
        } else {
            return null;
        }
    }

    public static DistRayTraceResult rayTraceClosest(BlockPos pos, Vector3 start, Vector3 end, List<AxisAlignedBB> aaBBs) {
        DistRayTraceResult closestHit = null;
        double curClosest = Double.MAX_VALUE;
        for (AxisAlignedBB aaBB : aaBBs) {
            DistRayTraceResult hit = rayTrace(pos, start, end, aaBB);
            if (hit != null && curClosest > hit.dist) {
                closestHit = hit;
                curClosest = hit.dist;
            }
        }
        return closestHit;
    }

    public static class DistRayTraceResult extends RayTraceResult {
        public double dist;
        public DistRayTraceResult(Vec3d hitVecIn, EnumFacing sideHitIn, BlockPos blockPosIn, double dist) {
            super(hitVecIn, sideHitIn, blockPosIn);
            this.dist = dist;
        }
    }

}
