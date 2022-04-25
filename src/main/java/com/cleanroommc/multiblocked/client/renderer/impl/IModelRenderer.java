package com.cleanroommc.multiblocked.client.renderer.impl;

import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DialogWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.client.renderer.ICustomRenderer;
import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.cleanroommc.multiblocked.client.model.ModelFactory;
import com.cleanroommc.multiblocked.client.model.custommodel.CustomBakedModel;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class IModelRenderer implements ICustomRenderer {
    public static final IModelRenderer INSTANCE = new IModelRenderer();

    protected static final Set<ResourceLocation> CACHE = new HashSet<>();

    public final ResourceLocation modelLocation;
    @SideOnly(Side.CLIENT)
    protected IBakedModel itemModel;
    @SideOnly(Side.CLIENT)
    protected Map<EnumFacing, CustomBakedModel> blockModels;

    protected IModelRenderer() {
        modelLocation = null;
    }

    public IModelRenderer(ResourceLocation modelLocation) {
        this.modelLocation = modelLocation;
        if (Multiblocked.isClient()) {
            if (isRaw()) {
                registerTextureSwitchEvent();
                CACHE.add(modelLocation);
            }
            blockModels = new EnumMap<>(EnumFacing.class);
        }
    }

    @SideOnly(Side.CLIENT)
    protected IModel getModel() {
        return ModelFactory.getModel(modelLocation);
    }

    @Override
    public void renderItem(ItemStack stack) {
        RenderItem ri = Minecraft.getMinecraft().getRenderItem();
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        ri.renderItem(stack, getItemBakedModel());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder buffer) {
        CustomBakedModel model = getBlockBakedModel(pos, blockAccess);
        if (!model.shouldRenderInLayer(state, MathHelper.getPositionRandom(pos))) return false;
        BlockModelRenderer blockModelRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
        blockModelRenderer.renderModel(blockAccess, model, state, pos, buffer, true);
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderBlockDamage(IBlockState state, BlockPos pos, TextureAtlasSprite texture, IBlockAccess blockAccess) {
        BlockModelRenderer blockModelRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
        IBakedModel bakedModel = net.minecraftforge.client.ForgeHooksClient.getDamageModel(getBlockBakedModel(pos, blockAccess), texture, state, blockAccess, pos);
        blockModelRenderer.renderModel(blockAccess, bakedModel, state, pos, Tessellator.getInstance().getBuffer(), true);
    }

    @SideOnly(Side.CLIENT)
    protected IBakedModel getItemBakedModel() {
        if (itemModel == null) {
            itemModel = getModel().bake(
                    TRSRTransformation.identity(),
                    DefaultVertexFormats.ITEM,
                    ModelLoader.defaultTextureGetter());
        }
        return itemModel;
    }

    @SideOnly(Side.CLIENT)
    protected CustomBakedModel getBlockBakedModel(BlockPos pos, IBlockAccess blockAccess) {
        TileEntity tileEntity = blockAccess.getTileEntity(pos);
        EnumFacing frontFacing = EnumFacing.NORTH;
        if (tileEntity instanceof  ComponentTileEntity<?>) {
            frontFacing = ((ComponentTileEntity<?>) tileEntity).getFrontFacing();
        }
        return blockModels.computeIfAbsent(frontFacing, facing -> new CustomBakedModel(getModel().bake(
                TRSRTransformation.from(facing),
                DefaultVertexFormats.BLOCK,
                ModelLoader.defaultTextureGetter())));
    }

    @Override
    public boolean isRaw() {
        return !CACHE.contains(modelLocation);
    }

    @Override
    public void onTextureSwitchEvent(TextureMap map) {
        blockModels.clear();
        itemModel = null;
        IModel model = getModel();
        for (ResourceLocation texture : model.getTextures()) {
            map.registerSprite(texture);
        }
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return getItemBakedModel().getParticleTexture();
    }

    @Override
    public String getType() {
        return "imodel";
    }

    @Override
    public IRenderer fromJson(Gson gson, JsonObject jsonObject) {
        return new IModelRenderer(gson.fromJson(jsonObject.get("modelLocation"), ResourceLocation.class));
    }

    @Override
    public JsonObject toJson(Gson gson, JsonObject jsonObject) {
        jsonObject.add("modelLocation", gson.toJsonTree(modelLocation, ResourceLocation.class));
        return jsonObject;
    }

    @Override
    public Supplier<IRenderer> createConfigurator(WidgetGroup parent, DraggableScrollableWidgetGroup group, IRenderer current) {
        TextFieldWidget tfw = new TextFieldWidget(1,1,150,20,true, null, null);
        group.addWidget(tfw);
        File path = new File(Multiblocked.location, "assets/multiblocked/models");
        group.addWidget(new ButtonWidget(155, 1, 20, 20, cd -> DialogWidget.showFileDialog(parent, "select a java model", path, true,
                DialogWidget.suffixFilter(".json"), r -> {
                    if (r != null && r.isFile()) {
                        tfw.setCurrentString("multiblocked:" + r.getPath().replace(path.getPath(), "").substring(1).replace(".json", "").replace('\\', '/'));
                    }
                })).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/darkened_slot.png"), new TextTexture("F", -1)).setHoverTooltip("multiblocked.gui.tips.file_selector"));
        if (current instanceof IModelRenderer && ((IModelRenderer) current).modelLocation != null) {
            tfw.setCurrentString(((IModelRenderer) current).modelLocation.toString());
        }
        return () -> {
            if (tfw.getCurrentString().isEmpty()) {
                return null;
            } else {
                return new IModelRenderer(new ResourceLocation(tfw.getCurrentString()));
            }
        };
    }
}
