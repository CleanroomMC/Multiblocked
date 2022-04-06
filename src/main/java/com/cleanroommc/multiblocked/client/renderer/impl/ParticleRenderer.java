package com.cleanroommc.multiblocked.client.renderer.impl;

import com.cleanroommc.multiblocked.client.particle.IParticle;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ParticleRenderer implements IRenderer {
    IParticle particle;
    
    
    @Override
    public void renderItem(ItemStack stack) {
        
    }

    @Override
    public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder buffer) {
        return false;
    }

    @Override
    public boolean shouldRenderInPass(World world, BlockPos pos, int pass) {
        return false;
    }

    @Override
    public boolean hasTESR() {
        return false;
    }

    @Override
    public void renderTESR(@Nonnull TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        BufferBuilder builder = Tessellator.getInstance().getBuffer();
//        particle.renderParticle();
    }

    @Override
    public boolean isGlobalRenderer(@Nonnull TileEntity te) {
        return false;
    }
}
