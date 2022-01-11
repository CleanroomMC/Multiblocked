package io.github.cleanroommc.multiblocked.client.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@SideOnly(Side.CLIENT)
public class CustomRenderItem extends RenderItem {
    RenderItem parent;

    public CustomRenderItem(RenderItem parent) {
        super(Minecraft.getMinecraft().renderEngine, ObfuscationReflectionHelper.getPrivateValue(RenderItem.class, parent, "field_175059_m"), Minecraft.getMinecraft().getItemColors());
        this.parent = parent;
    }

    @Override
    @Nonnull
    public ItemModelMesher getItemModelMesher() {
        return parent.getItemModelMesher();
    }

    @Override
    public void registerItem(@Nonnull Item itm, int subType, @Nonnull String identifier) {
        parent.registerItem(itm, subType, identifier);
    }

    @Override
    public void renderModel(@Nonnull IBakedModel model, @Nonnull ItemStack stack) {
        parent.renderModel(model, stack);
    }

    @Override
    public void renderModel(@Nonnull IBakedModel model, int color) {
        parent.renderModel(model, color);
    }

    @Override
    public void renderItem(@Nonnull ItemStack stack, @Nonnull IBakedModel model) {
        if (!stack.isEmpty() && model instanceof ICustomItemRenderer) {
            ICustomItemRenderer renderer = (ICustomItemRenderer) model;
            GlStateManager.pushMatrix();
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableRescaleNormal();
//            GlStateTracker.pushState();
            renderer.renderItem(stack);
//            GlStateTracker.popState();
            GlStateManager.popMatrix();
            return;

        }
        parent.renderItem(stack, model);
    }

    @Override
    public void renderQuads(@Nonnull BufferBuilder renderer, @Nonnull List<BakedQuad> quads, int color, @Nonnull ItemStack stack) {
        parent.renderQuads(renderer, quads, color, stack);
    }

    @Override
    public boolean shouldRenderItemIn3D(@Nonnull ItemStack stack) {
        return parent.shouldRenderItemIn3D(stack);
    }

    @Override
    public void renderItem(@Nonnull ItemStack stack, @Nonnull ItemCameraTransforms.TransformType cameraTransformType) {
        parent.renderItem(stack, cameraTransformType);
    }

    @Override
    @Nonnull
    public IBakedModel getItemModelWithOverrides(@Nonnull ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entitylivingbaseIn) {
        return parent
                .getItemModelWithOverrides(stack, worldIn, entitylivingbaseIn);
    }

    @Override
    public void renderItem(@Nonnull ItemStack stack, @Nonnull EntityLivingBase entitylivingbaseIn,
                           @Nonnull ItemCameraTransforms.TransformType transform,
                           boolean leftHanded) {
        parent.renderItem(stack, entitylivingbaseIn, transform, leftHanded);
    }

    @Override
    public void renderItemModel(@Nonnull ItemStack stack, @Nonnull IBakedModel bakedmodel,
                                @Nonnull ItemCameraTransforms.TransformType transform,
                                   boolean leftHanded) {
        parent.renderItemModel(stack, bakedmodel, transform, leftHanded);
    }

    @Override
    public void renderItemIntoGUI(@Nonnull ItemStack stack, int x, int y) {
        parent.renderItemIntoGUI(stack, x, y);
    }

    @Override
    public void renderItemModelIntoGUI(@Nonnull ItemStack stack, int x, int y, @Nonnull IBakedModel bakedmodel) {
        parent.renderItemModelIntoGUI(stack, x, y, bakedmodel);
    }

    @Override
    public void renderItemAndEffectIntoGUI(@Nonnull ItemStack stack, int xPosition,
                                           int yPosition) {
        parent.renderItemAndEffectIntoGUI(stack, xPosition, yPosition);
    }

    @Override
    public void renderItemAndEffectIntoGUI(@Nullable EntityLivingBase p_184391_1_, @Nonnull ItemStack p_184391_2_, int p_184391_3_, int p_184391_4_) {
        parent.renderItemAndEffectIntoGUI(p_184391_1_, p_184391_2_, p_184391_3_,
                p_184391_4_);
    }

    @Override
    public void renderItemOverlays(@Nonnull FontRenderer fr, @Nonnull ItemStack stack, int xPosition, int yPosition) {
        parent.renderItemOverlays(fr, stack, xPosition, yPosition);
    }

    @Override
    public void renderItemOverlayIntoGUI(@Nonnull FontRenderer fr, @Nonnull ItemStack stack, int xPosition, int yPosition, @Nullable String text) {
        parent.renderItemOverlayIntoGUI(fr, stack, xPosition, yPosition, text);
    }
}

