package io.github.cleanroommc.multiblocked.util;

import io.github.cleanroommc.multiblocked.Multiblocked;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

public class Utils {

    private static final MethodHandle nbtTagCompound$tagMap;

    static {
        MethodHandles.Lookup lookup = Multiblocked.LOOKUP;
        MethodHandle _nbtTagCompound$tagMap;
        try {
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            lookup.unreflectSetter(modifiers).invokeExact(modifiers, modifiers.getModifiers() & ~Modifier.FINAL);
            Field tagMap = ReflectionHelper.findField(NBTTagCompound.class, "tagMap", "field_74784_a");
            _nbtTagCompound$tagMap = lookup.unreflectGetter(tagMap);
        } catch (Throwable e) {
            e.printStackTrace();
            _nbtTagCompound$tagMap = null;
        }
        nbtTagCompound$tagMap = _nbtTagCompound$tagMap;
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
            if (forTag == null && againstTag == null) {
                return true;
            }
            if (forTag != null && againstTag != null) {
                try {
                    Map<String, NBTBase> forTagMap = (Map<String, NBTBase>) nbtTagCompound$tagMap.invoke(forTag);
                    Map<String, NBTBase> againstTagMap = (Map<String, NBTBase>) nbtTagCompound$tagMap.invoke(againstTag);
                    return forTagMap.entrySet().containsAll(againstTagMap.entrySet());
                } catch (Throwable e) {
                    e.printStackTrace();
                }
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
