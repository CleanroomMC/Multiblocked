package com.cleanroommc.multiblocked.core.asm.hooks;

import com.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;

public class CCLHooks {
    public static boolean renderBlockDamage(IBlockState state, BlockPos pos, TextureAtlasSprite texture, IBlockAccess blockAccess) {
        return blockAccess instanceof ChunkCache &&
                ((ChunkCache) blockAccess).world ==
                        Minecraft.getMinecraft().world &&
                MultiblockWorldSavedData.isModelDisabled(pos);
    }

    public static boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder bufferBuilderIn) {
        return blockAccess instanceof ChunkCache &&
                ((ChunkCache) blockAccess).world ==
                        Minecraft.getMinecraft().world &&
                MultiblockWorldSavedData.isModelDisabled(pos);
    }
}
