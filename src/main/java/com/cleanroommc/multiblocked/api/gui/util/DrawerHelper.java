package com.cleanroommc.multiblocked.api.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class DrawerHelper {

    @SideOnly(Side.CLIENT)
    public static void drawFluidTexture(double xCoord, double yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, double zLevel) {
        double uMin = textureSprite.getMinU();
        double uMax = textureSprite.getMaxU();
        double vMin = textureSprite.getMinV();
        double vMax = textureSprite.getMaxV();
        uMax = uMax - maskRight / 16.0 * (uMax - uMin);
        vMax = vMax - maskTop / 16.0 * (vMax - vMin);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(xCoord, yCoord + 16, zLevel).tex(uMin, vMax).endVertex();
        buffer.pos(xCoord + 16 - maskRight, yCoord + 16, zLevel).tex(uMax, vMax).endVertex();
        buffer.pos(xCoord + 16 - maskRight, yCoord + maskTop, zLevel).tex(uMax, vMin).endVertex();
        buffer.pos(xCoord, yCoord + maskTop, zLevel).tex(uMin, vMin).endVertex();
        tessellator.draw();
    }

    @SideOnly(Side.CLIENT)
    public static void drawFluidForGui(FluidStack contents, int tankCapacity, int startX, int startY, int widthT, int heightT) {
        Fluid fluid = contents.getFluid();
        ResourceLocation fluidStill = fluid.getStill();
        TextureAtlasSprite fluidStillSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluidStill.toString());
        int fluidColor = fluid.getColor(contents);
        int scaledAmount = contents.amount * heightT / tankCapacity;
        if (contents.amount > 0 && scaledAmount < 1) {
            scaledAmount = 1;
        }
        if (scaledAmount > heightT || contents.amount == tankCapacity) {
            scaledAmount = heightT;
        }
        GlStateManager.enableBlend();
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        int i = (fluidColor & 0xFF0000) >> 16;
        int j = (fluidColor & 0xFF00) >> 8;
        int k = (fluidColor & 0xFF);
        int opacity = (fluidColor & 0x000000FF);
        GlStateManager.color(i / 255.0f, j / 255.0f, k / 255.0f, opacity / 255.0f);

        final int xTileCount = widthT / 16;
        final int xRemainder = widthT - xTileCount * 16;
        final int yTileCount = scaledAmount / 16;
        final int yRemainder = scaledAmount - yTileCount * 16;

        final int yStart = startY + heightT;

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                int width = xTile == xTileCount ? xRemainder : 16;
                int height = yTile == yTileCount ? yRemainder : 16;
                int x = startX + xTile * 16;
                int y = yStart - (yTile + 1) * 16;
                if (width > 0 && height > 0) {
                    int maskTop = 16 - height;
                    int maskRight = 16 - width;

                    drawFluidTexture(x, y, fluidStillSprite, maskTop, maskRight, 0.0);
                }
            }
        }
        GlStateManager.disableBlend();
    }


    @SideOnly(Side.CLIENT)
    public static void drawHoveringText(ItemStack itemStack, List<String> tooltip, int maxTextWidth, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getMinecraft();
        GuiUtils.drawHoveringText(itemStack == null ? ItemStack.EMPTY : itemStack, tooltip, mouseX, mouseY, screenWidth, screenHeight, maxTextWidth, mc.fontRenderer);
        GlStateManager.disableLighting();
    }

    @SideOnly(Side.CLIENT)
    public static void drawBorder(int x, int y, int width, int height, int color, int border) {
        drawSolidRect(x - border, y - border, width + 2 * border, border, color);
        drawSolidRect(x - border, y + height, width + 2 * border, border, color);
        drawSolidRect(x - border, y, border, height, color);
        drawSolidRect(x + width, y, border, height, color);
    }

    @SideOnly(Side.CLIENT)
    public static void drawStringSized(String text, double x, double y, int color, boolean dropShadow, float scale, boolean center) {
        GlStateManager.pushMatrix();
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        double scaledTextWidth = center ? fontRenderer.getStringWidth(text) * scale : 0.0;
        GlStateManager.translate(x - scaledTextWidth / 2.0, y, 0.0f);
        GlStateManager.scale(scale, scale, scale);
        fontRenderer.drawString(text, 0, 0, color, dropShadow);
        GlStateManager.popMatrix();
    }

    @SideOnly(Side.CLIENT)
    public static void drawStringFixedCorner(String text, double x, double y, int color, boolean dropShadow, float scale) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        double scaledWidth = fontRenderer.getStringWidth(text) * scale;
        double scaledHeight = fontRenderer.FONT_HEIGHT * scale;
        drawStringSized(text, x - scaledWidth, y - scaledHeight, color, dropShadow, scale, false);
    }

    @SideOnly(Side.CLIENT)
    public static void drawText(String text, float x, float y, float scale, int color) {
        drawText(text, x, y, scale, color, false);
    }

    @SideOnly(Side.CLIENT)
    public static void drawText(String text, float x, float y, float scale, int color, boolean shadow) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        GlStateManager.disableBlend();
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 0f);
        float sf = 1 / scale;
        fontRenderer.drawString(text, x * sf, y * sf, color, shadow);
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
    }

    @SideOnly(Side.CLIENT)
    public static void drawItemStack(ItemStack itemStack, int x, int y, @Nullable
            String altTxt) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 0.0F, 32.0F);
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableLighting();
        RenderHelper.enableGUIStandardItemLighting();
        OpenGlHelper
                .setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);
        Minecraft mc = Minecraft.getMinecraft();
        RenderItem itemRender = mc.getRenderItem();
        itemRender.renderItemAndEffectIntoGUI(itemStack, x, y);
        itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, x, y, altTxt);
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
    }

    @SideOnly(Side.CLIENT)
    public static List<String> getItemToolTip(ItemStack itemStack) {
        Minecraft mc = Minecraft.getMinecraft();
        ITooltipFlag flag = mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL;
        List<String> tooltip = itemStack.getTooltip(mc.player, flag);
        for (int i = 0; i < tooltip.size(); ++i) {
            if (i == 0) {
                tooltip.set(i, itemStack.getItem().getForgeRarity(itemStack).getColor() + tooltip.get(i));
            } else {
                tooltip.set(i, TextFormatting.GRAY + tooltip.get(i));
            }
        }
        return tooltip;
    }

    @SideOnly(Side.CLIENT)
    public static void drawSelectionOverlay(int x, int y, int width, int height) {
        GlStateManager.disableDepth();
        GlStateManager.colorMask(true, true, true, false);
        drawGradientRect(x, y, width, height, -2130706433, -2130706433);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();
    }

    @SideOnly(Side.CLIENT)
    public static void drawSolidRect(int x, int y, int width, int height, int color) {
        Gui.drawRect(x, y, x + width, y + height, color);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableBlend();
    }

    @SideOnly(Side.CLIENT)
    public static void drawRectShadow(int x, int y, int width, int height, int distance) {
        drawGradientRect(x + distance, y + height, width - distance, distance, 0x4f000000, 0, false);
        drawGradientRect(x + width, y + distance, distance, height - distance, 0x4f000000, 0, true);

        float startAlpha = (float) (0x4f) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
        x += width;
        y += height;
        buffer.pos(x, y, 0).color(0, 0, 0, startAlpha).endVertex();
        buffer.pos(x, y + distance, 0).color(0, 0, 0, 0).endVertex();
        buffer.pos(x + distance, y + distance, 0).color(0, 0, 0, 0).endVertex();

        buffer.pos(x, y, 0).color(0, 0, 0, startAlpha).endVertex();
        buffer.pos(x + distance, y + distance, 0).color(0, 0, 0, 0).endVertex();
        buffer.pos(x + distance, y, 0).color(0, 0, 0, 0).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    @SideOnly(Side.CLIENT)
    public static void drawGradientRect(int x, int y, int width, int height, int startColor, int endColor) {
        drawGradientRect(x, y, width, height, startColor, endColor, false);
    }

    @SideOnly(Side.CLIENT)
    public static void drawGradientRect(float x, float y, float width, float height, int startColor, int endColor, boolean horizontal) {
        float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
        float startRed = (float) (startColor >> 16 & 255) / 255.0F;
        float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
        float startBlue = (float) (startColor & 255) / 255.0F;
        float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
        float endRed = (float) (endColor >> 16 & 255) / 255.0F;
        float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
        float endBlue = (float) (endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        if (horizontal) {
            buffer.pos(x + width, y, 0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
            buffer.pos(x, y, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.pos(x, y + height, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.pos(x + width, y + height, 0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
            tessellator.draw();
        } else {
            buffer.pos(x + width, y, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.pos(x, y, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.pos(x, y + height, 0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
            buffer.pos(x + width, y + height, 0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
            tessellator.draw();
        }
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    @SideOnly(Side.CLIENT)
    public static void setColor(int color) { // ARGB
        GlStateManager.color((color >> 16 & 255) / 255.0F,
                (color >> 8 & 255) / 255.0F,
                (color & 255) / 255.0F,
                (color >> 24 & 255) / 255.0F);
    }

    @SideOnly(Side.CLIENT)
    public static void drawCircle(float x, float y, float r, int color, int segments) {
        if (color == 0) return;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        setColor(color);
        bufferbuilder.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
        for (int i = 0; i < segments; i++) {
            bufferbuilder.pos(x + r * Math.cos(-2 * Math.PI * i / segments), y + r * Math.sin(-2 * Math.PI * i / segments), 0.0D).endVertex();
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1,1,1,1);
    }

    @SideOnly(Side.CLIENT)
    public static void drawSector(float x, float y, float r, int color, int segments, int from, int to) {
        if (from > to || from < 0 || color == 0) return;
        if(to > segments) to = segments;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        setColor(color);
        bufferbuilder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION);
        for (int i = from; i < to; i++) {
            bufferbuilder.pos(x + r * Math.cos(-2 * Math.PI * i / segments), y + r * Math.sin(-2 * Math.PI * i / segments), 0.0D).endVertex();
            bufferbuilder.pos(x + r * Math.cos(-2 * Math.PI * (i + 1) / segments), y + r * Math.sin(-2 * Math.PI * (i + 1) / segments), 0.0D).endVertex();
            bufferbuilder.pos(x, y, 0.0D).endVertex();
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
    }

    public static void drawTorus(float x, float y, float outer, float inner, int color, int segments, int from, int to) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        setColor(color);
        bufferbuilder.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION);
        for (int i = from; i <= to; i++) {
            float angle = (i / (float) segments) * 3.14159f * 2.0f;
            bufferbuilder.pos(x + inner * Math.cos(-angle), y + inner * Math.sin(-angle), 0).endVertex();
            bufferbuilder.pos(x + outer * Math.cos(-angle), y + outer * Math.sin(-angle), 0).endVertex();
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
    }

    @SideOnly(Side.CLIENT)
    public static void drawLines(List<Vec2f> points, int startColor, int endColor, float width) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(width);
        if (startColor == endColor) {
            setColor(startColor);
            bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
            for (Vec2f point : points) {
                bufferbuilder.pos(point.x, point.y, 0).endVertex();
            }
        } else {
            float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
            float startRed = (float) (startColor >> 16 & 255) / 255.0F;
            float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
            float startBlue = (float) (startColor & 255) / 255.0F;
            float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
            float endRed = (float) (endColor >> 16 & 255) / 255.0F;
            float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
            float endBlue = (float) (endColor & 255) / 255.0F;
            bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            int size = points.size();

            for (int i = 0; i < size; i++) {
                float p = i * 1.0f / size;
                bufferbuilder.pos(points.get(i).x, points.get(i).y, 0)
                        .color(startRed + (endRed - startRed) * p,
                                startGreen + (endGreen - startGreen) * p,
                                startBlue + (endBlue - startBlue) * p,
                                startAlpha + (endAlpha - startAlpha) * p)
                        .endVertex();
            }
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
    }

    @SideOnly(Side.CLIENT)
    public static void drawTextureRect(double x, double y, double width, double height) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, 0.0D).tex(0, 0).endVertex();
        buffer.pos(x + width, y + height, 0.0D).tex(1, 0).endVertex();
        buffer.pos(x + width, y, 0.0D).tex(1, 1).endVertex();
        buffer.pos(x, y, 0.0D).tex(0, 1).endVertex();
        tessellator.draw();
    }

}
