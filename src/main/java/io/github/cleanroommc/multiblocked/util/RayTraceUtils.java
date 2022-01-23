package io.github.cleanroommc.multiblocked.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class RayTraceUtils {

    public static AxisAlignedBB rotateAABB(AxisAlignedBB aaBB, EnumFacing facing) {
        Vector3 min = new Vector3(aaBB.minX, aaBB.minY, aaBB.minZ).subtract(0.5);
        Vector3 max = new Vector3(aaBB.maxX, aaBB.maxY, aaBB.maxZ).subtract(0.5);
        double radians;
        switch (facing) {
            case SOUTH:
                radians = Math.toRadians(180);
                min.rotate(radians, new Vector3(0, 1, 0));
                max.rotate(radians, new Vector3(0, 1, 0));
                break;
            case EAST:
                radians = Math.toRadians(-90);
                min.rotate(radians, new Vector3(0, 1, 0));
                max.rotate(radians, new Vector3(0, 1, 0));
                break;
            case WEST:
                radians = Math.toRadians(90);
                min.rotate(radians, new Vector3(0, 1, 0));
                max.rotate(radians, new Vector3(0, 1, 0));
                break;
            case UP:
                radians = Math.toRadians(90);
                min.rotate(radians, new Vector3(0, 0, 1));
                max.rotate(radians, new Vector3(0, 0, 1));
                break;
            case DOWN:
                radians = Math.toRadians(-90);
                min.rotate(radians, new Vector3(0, 0, 1));
                max.rotate(radians, new Vector3(0, 0, 1));
                break;
        }
        min.add(0.5);
        max.add(0.5);
        return new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z);
    }

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
