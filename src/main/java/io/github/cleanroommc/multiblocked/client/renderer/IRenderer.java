package io.github.cleanroommc.multiblocked.client.renderer;

import crafttweaker.annotations.ZenRegister;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stanhebben.zenscript.annotations.ZenClass;

import javax.annotation.Nonnull;

@ZenClass("mods.multiblocked.client.IRenderer")
@ZenRegister
public interface IRenderer {

    @SideOnly(Side.CLIENT)
    default boolean isRaw() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    void renderItem(ItemStack stack);

    @SideOnly(Side.CLIENT)
    default void renderBlockDamage(IBlockState state, BlockPos pos, TextureAtlasSprite texture, IBlockAccess blockAccess) {

    }

    @SideOnly(Side.CLIENT)
    boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder buffer);

    @SideOnly(Side.CLIENT)
    default void register(TextureMap map) {

    }

    @SideOnly(Side.CLIENT)
    default TextureAtlasSprite getParticleTexture() {
        return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
    }

    //************* TESR *************//

    @SideOnly(Side.CLIENT)
    default boolean shouldRenderInPass(World world, BlockPos pos, int pass) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    default boolean hasTESR() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    default void renderTESR(@Nonnull TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
    }

    @SideOnly(Side.CLIENT)
    default boolean isGlobalRenderer(@Nonnull TileEntity te) {
        return false;
    }

}
