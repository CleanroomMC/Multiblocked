package com.cleanroommc.multiblocked.api.block;

import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
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
        setRegistryName(block.getRegistryName());
    }

    public ComponentDefinition getDefinition() {
        return ((BlockComponent)block).definition;
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
