package io.github.cleanroommc.multiblocked.api.item;

import io.github.cleanroommc.multiblocked.Multiblocked;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public class ItemBlueprint extends Item {

    public ItemBlueprint(String registryName) {
        setRegistryName(Multiblocked.MODID, registryName);
        setCreativeTab(Multiblocked.CREATIVE_TAB);
        setTranslationKey(Multiblocked.MODID + registryName);
    }

    public static BlockPos[] getPos(ItemStack stack) {
        NBTTagCompound tag = stack.getOrCreateSubCompound("blueprint");
        if (!tag.hasKey("minX")) return null;
        return new BlockPos[]{
                new BlockPos(tag.getInteger("minX"), tag.getInteger("minY"), tag.getInteger("minZ")),
                new BlockPos(tag.getInteger("maxX"), tag.getInteger("maxY"), tag.getInteger("maxZ"))
        };
    }


    public static void addPos(ItemStack stack, BlockPos pos) {
        NBTTagCompound tag = stack.getOrCreateSubCompound("blueprint");
        if (!tag.hasKey("minX") || tag.getInteger("minX") > pos.getX()) {
            tag.setInteger("minX", pos.getX());
        }
        if (!tag.hasKey("maxX") || tag.getInteger("maxX") < pos.getX()) {
            tag.setInteger("maxX", pos.getX());
        }

        if (!tag.hasKey("minY") || tag.getInteger("minY") > pos.getY()) {
            tag.setInteger("minY", pos.getY());
        }
        if (!tag.hasKey("maxY") || tag.getInteger("maxY") < pos.getY()) {
            tag.setInteger("maxY", pos.getY());
        }

        if (!tag.hasKey("minZ") || tag.getInteger("minZ") > pos.getZ()) {
            tag.setInteger("minZ", pos.getZ());
        }
        if (!tag.hasKey("maxZ") || tag.getInteger("maxZ") < pos.getZ()) {
            tag.setInteger("maxZ", pos.getZ());
        }
    }

    public static void removePos(ItemStack stack) {
        NBTTagCompound tag = stack.getOrCreateSubCompound("blueprint");
        tag.removeTag("minX");
        tag.removeTag("maxX");
        tag.removeTag("minY");
        tag.removeTag("maxY");
        tag.removeTag("minZ");
        tag.removeTag("maxZ");
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.isSneaking()) {
            addPos(stack, pos);
        } else {
            removePos(stack);
        }
        return EnumActionResult.SUCCESS;
    }


    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            removePos(stack);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }
}
