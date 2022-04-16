package com.cleanroommc.multiblocked.client.renderer.impl;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.BlockSelectorWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.client.renderer.ICustomRenderer;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.cleanroommc.multiblocked.client.util.FacadeBlockAccess;
import com.cleanroommc.multiblocked.client.util.FacadeBlockWorld;
import com.cleanroommc.multiblocked.client.util.TrackedDummyWorld;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.org.apache.xpath.internal.operations.Mult;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class BlockStateRenderer implements ICustomRenderer {
    public final static BlockStateRenderer INSTANCE = new BlockStateRenderer();

    public final BlockInfo blockInfo;
    @SideOnly(Side.CLIENT)
    private IBakedModel itemModel;
    @SideOnly(Side.CLIENT)
    public static ThreadLocal<FacadeBlockWorld> facadeBlockWorld;
    static {
        if (Multiblocked.isClient()) {
            facadeBlockWorld = ThreadLocal.withInitial(FacadeBlockWorld::new);
        }
    }

    private BlockStateRenderer() {
        blockInfo = null;
    }

    public BlockStateRenderer(IBlockState state) {
        this(BlockInfo.fromBlockState(state == null ? Blocks.BARRIER.getDefaultState() : state));
    }

    public BlockStateRenderer(BlockInfo blockInfo) {
        this.blockInfo = blockInfo == null ? new BlockInfo(Blocks.BARRIER) : blockInfo;
        if (Multiblocked.isClient()) {
            registerTextureSwitchEvent();
        }
    }

    public IBlockState getState() {
        return blockInfo.getBlockState();
    }

    public BlockInfo getBlockInfo() {
        return blockInfo;
    }

    @SideOnly(Side.CLIENT)
    protected IBakedModel getItemModel(ItemStack renderItem) {
        if (itemModel == null) {
            itemModel = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(renderItem, null, null);
        }
        return itemModel;
    }

    @Override
    public void renderItem(ItemStack stack) {
        IBlockState state = getState();
        RenderItem ri = Minecraft.getMinecraft().getRenderItem();
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        ItemStack renderItem = new ItemStack(Item.getItemFromBlock(state.getBlock()), 1, state.getBlock().damageDropped(state));
        ri.renderItem(renderItem, getItemModel(renderItem));
    }

    @Override
    public void renderBlockDamage(IBlockState state, BlockPos pos, TextureAtlasSprite texture, IBlockAccess blockAccess) {
        state = getState();
        if (state.getBlock().canRenderInLayer(state, MinecraftForgeClient.getRenderLayer())) {
            BlockRendererDispatcher brd  = Minecraft.getMinecraft().getBlockRendererDispatcher();
            IBlockAccess access = new FacadeBlockAccess(blockAccess, pos, state, blockAccess instanceof World ? getTileEntity((World) blockAccess, pos) : blockInfo.getTileEntity());
            brd.renderBlockDamage(state, pos, texture, access);
        }
    }

    @Override
    public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder buffer) {
        state = getState();
        if (state.getBlock().canRenderInLayer(state, MinecraftForgeClient.getRenderLayer())) {
            BlockRendererDispatcher brd  = Minecraft.getMinecraft().getBlockRendererDispatcher();
            IBlockAccess access = new FacadeBlockAccess(blockAccess, pos, state, blockAccess instanceof World ? getTileEntity((World) blockAccess, pos) : blockInfo.getTileEntity());
            return brd.renderBlock(state, pos, access, buffer);
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasTESR(World world, BlockPos pos) {
        TileEntity tileEntity = getTileEntity(world, pos);
        if (tileEntity == null) {
            return false;
        }
        return TileEntityRendererDispatcher.instance.getRenderer(tileEntity.getClass()) != null;
    }


    @SideOnly(Side.CLIENT)
    public TileEntity getTileEntity(World world, BlockPos pos) {
        TileEntity tile = getBlockInfo().getTileEntity();
        if (tile != null) {
            FacadeBlockWorld dummyWorld = facadeBlockWorld.get();
            dummyWorld.update(world, pos, getState(), tile);
            tile.setWorld(dummyWorld);
            tile.setPos(pos);
        }
        return tile;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean shouldRenderInPass(World world, BlockPos pos, int pass) {
        TileEntity tileEntity = getTileEntity(world, pos);
        return tileEntity != null && tileEntity.shouldRenderInPass(pass);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isGlobalRenderer(@Nonnull TileEntity te) {
        TileEntity tileEntity = getTileEntity(te.getWorld(), te.getPos());
        if (tileEntity == null) return false;
        TileEntitySpecialRenderer<TileEntity> tesr = TileEntityRendererDispatcher.instance.getRenderer(tileEntity.getClass());
        if (tesr != null) {
            return tesr.isGlobalRenderer(tileEntity);
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderTESR(@Nonnull TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        TileEntity tileEntity = getTileEntity(te.getWorld(), te.getPos());
        if (tileEntity == null) return;
        TileEntitySpecialRenderer<TileEntity> tesr = TileEntityRendererDispatcher.instance.getRenderer(tileEntity.getClass());
        if (tesr != null) {
            tesr.render(tileEntity, x, y, z, partialTicks, destroyStage, alpha);
        }
    }

    @Override
    public void onTextureSwitchEvent(TextureMap map) {
        itemModel = null;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        IBlockState state = getState();
        ItemStack renderItem = new ItemStack(Item.getItemFromBlock(state.getBlock()), 1, state.getBlock().damageDropped(state));
        return getItemModel(renderItem).getParticleTexture();
    }

    @Override
    public String getType() {
        return "blockstate";
    }

    @Override
    public IRenderer fromJson(Gson gson, JsonObject jsonObject) {
        return new BlockStateRenderer(gson.fromJson(jsonObject.get("state"), IBlockState.class));
    }

    @Override
    public JsonObject toJson(Gson gson, JsonObject jsonObject) {
        jsonObject.add("state", gson.toJsonTree(getState(), IBlockState.class));
        return jsonObject;
    }

    @Override
    public Supplier<IRenderer> createConfigurator(WidgetGroup parent, DraggableScrollableWidgetGroup group, IRenderer current) {
        BlockSelectorWidget blockSelectorWidget = new BlockSelectorWidget(0, 1, true);
        if (current instanceof BlockStateRenderer) {
            blockSelectorWidget.setBlock(((BlockStateRenderer) current).getState());
        }
        group.addWidget(blockSelectorWidget);
        return () -> {
            if (blockSelectorWidget.getBlock() == null) {
                return null;
            } else {
                return new BlockStateRenderer(blockSelectorWidget.getBlock());
            }
        };
    }
}
