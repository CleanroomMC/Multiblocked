package io.github.cleanroommc.multiblocked.client.renderer.impl;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * It will toggles the rendered block each second, mainly for rendering of the Any Capability. {@link io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability#getCandidates(IO)} (IO)}
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

    @Override
    public boolean hasTESR() {
        return true;
    }

    @Override
    public boolean shouldRenderInPass(World world, BlockPos pos, int pass) {
        TileEntity tileEntity = getTileEntity(world, pos);
        return tileEntity != null && tileEntity.shouldRenderInPass(pass);
    }

    @Override
    public void renderTESR(@Nonnull TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        TileEntity tileEntity = getTileEntity(te.getWorld(), te.getPos());
        TileEntitySpecialRenderer<TileEntity> tesr = TileEntityRendererDispatcher.instance.getRenderer(tileEntity);
        if (tesr != null) {
            tesr.render(tileEntity, x, y, z, partialTicks, destroyStage, alpha);
        }
    }

    @Override
    public boolean isGlobalRenderer(@Nonnull TileEntity te) {
        return true;
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
