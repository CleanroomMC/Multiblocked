package io.github.cleanroommc.multiblocked.client.renderer.impl;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * It will toggles the rendered block each second, mainly for rendering of the Any Capability. {@link io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability#getCandidates(IO)}
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

    @Override
    protected IBakedModel getItemModel(ItemStack renderItem) {
        return Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(renderItem, null, null);
    }

    @Override
    protected IBlockState getState() {
        long time = System.currentTimeMillis();
        if (time - lastTime > 1000) {
            lastTime = time;
            index = Multiblocked.RNG.nextInt();
        }
        return states[Math.abs(index) % states.length];
    }

    public TileEntity getTileEntity(World world, BlockPos pos) {
        int i = Math.abs(index) % states.length;
        IBlockState state = states[i];
        if (!state.getBlock().hasTileEntity(state)) return null;
        if (tileEntities[i] == null) {
            tileEntities[i] = state.getBlock().createTileEntity(world, state);
            tileEntities[i].setPos(pos);
            tileEntities[i].setWorld(world);
        }
        return tileEntities[i];
    }
}
