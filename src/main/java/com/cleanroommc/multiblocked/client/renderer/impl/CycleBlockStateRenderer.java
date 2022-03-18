package com.cleanroommc.multiblocked.client.renderer.impl;

import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.capability.IO;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * It will toggles the rendered block each second, mainly for rendering of the Any Capability. {@link MultiblockCapability#getCandidates(IO)} (IO)}
 *
 * Because you did not schedule the chunk compiling. So please don't use it in the world. Just for JEI or such dynamic rendering.
 */
public class CycleBlockStateRenderer extends BlockStateRenderer {
    public final IBlockState[] states;
    public final TileEntity[] tileEntities;
    public int index;
    public long lastTime;

    public CycleBlockStateRenderer(IBlockState[] states) {
        super(Blocks.AIR.getDefaultState());
        if (states.length == 0) states = new IBlockState[]{super.state};
        this.states = states;
        this.tileEntities = new TileEntity[states.length];
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected IBakedModel getItemModel(ItemStack renderItem) {
        return Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(renderItem, null, null);
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected IBlockState getState() {
        long time = System.currentTimeMillis();
        if (time - lastTime > 1000) {
            lastTime = time;
            index = Multiblocked.RNG.nextInt();
        }
        return states[Math.abs(index) % states.length];
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasTESR() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isGlobalRenderer(@Nonnull TileEntity te) {
        return true;
    }

    public TileEntity getTileEntity(World world, BlockPos pos) {
        try {
            int i = Math.abs(index) % states.length;
            IBlockState state = states[i];
            if (!state.getBlock().hasTileEntity(state)) return null;
            if (tileEntities[i] == null) {
                tileEntities[i] = state.getBlock().createTileEntity(world, state);
                if (tileEntities[i] != null) {
                    tileEntities[i].setPos(pos);
                    tileEntities[i].setWorld(world);
                }
            }
            return tileEntities[i];
        } catch (Throwable throwable) {
            return null;
        }
    }
}
