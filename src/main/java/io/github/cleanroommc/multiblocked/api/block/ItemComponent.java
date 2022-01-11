package io.github.cleanroommc.multiblocked.api.block;

import io.github.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemComponent extends ItemBlock {

    public ItemComponent(BlockComponent block) {
        super(block);
        setHasSubtypes(true);
    }

    public ComponentDefinition getDefinition() {
        return ((BlockComponent)block).definition;
    }

    @Nonnull
    @Override
    public String getTranslationKey(@Nonnull ItemStack stack) {
        return getDefinition().location.getPath();
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        return super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState.withProperty(BlockComponent.OPAQUE, getDefinition().isOpaqueCube));
    }

    @Nullable
    @Override
    public String getCreatorModId(@Nonnull ItemStack itemStack) {
        return getDefinition().location.getNamespace();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
