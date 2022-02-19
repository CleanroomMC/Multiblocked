package io.github.cleanroommc.multiblocked.api.item;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.pattern.MultiblockState;
import io.github.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public class ItemMultiblockBuilder extends Item {

    public ItemMultiblockBuilder() {
        setRegistryName(Multiblocked.MODID, "multiblock_builder");
        setCreativeTab(Multiblocked.CREATIVE_TAB);
        setTranslationKey(Multiblocked.MODID + "multiblock_builder");
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof ControllerTileEntity) {
                ((ControllerTileEntity) tileEntity).getPattern().autoBuild(player, new MultiblockState(world, pos));
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }

}
