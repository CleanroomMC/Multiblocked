package io.github.cleanroommc.multiblocked.util;

import io.github.cleanroommc.multiblocked.core.mixins.NBTTagCompoundMapExposer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.Map;

public class Utils {

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
