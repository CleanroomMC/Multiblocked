package io.github.cleanroommc.multiblocked.util;

import io.github.cleanroommc.multiblocked.core.mixins.NBTTagCompoundMapExposer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Utils {


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

    public static boolean areStacksSimilar(ItemStack checkFor, ItemStack checkAgainst) {
        if (checkFor.isEmpty() && checkAgainst.isEmpty()) {
            return true;
        }
        if (!checkFor.isEmpty() && !checkAgainst.isEmpty()) {
            if (checkFor.getItem() != checkAgainst.getItem()) {
                return false;
            }
            if (checkFor.getItemDamage() != checkAgainst.getItemDamage()) {
                return false;
            }
            NBTTagCompound forTag = checkFor.getTagCompound();
            NBTTagCompound againstTag = checkAgainst.getTagCompound();
            if (forTag == null) {
                return againstTag == null;
            }
            if (againstTag == null) {
                return false;
            }
            try {
                Map<String, NBTBase> forTagMap = ((NBTTagCompoundMapExposer) forTag).getTagMap();
                Map<String, NBTBase> againstTagMap = ((NBTTagCompoundMapExposer) againstTag).getTagMap();
                return forTagMap.entrySet().containsAll(againstTagMap.entrySet());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static BlockPos rotate(BlockPos pos, Vec3i with, EnumFacing facing) {
        switch (facing) {
            case EAST:
                return pos.add(-with.getZ(), with.getY(), with.getX());
            case SOUTH:
                return pos.add(-with.getX(), with.getY(), -with.getZ());
            case WEST:
                return pos.add(with.getZ(), with.getY(), -with.getX());
            default:
                return pos.add(with);
        }
    }

    private Utils() { }

}
