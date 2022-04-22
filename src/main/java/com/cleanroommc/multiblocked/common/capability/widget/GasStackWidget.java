package com.cleanroommc.multiblocked.common.capability.widget;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.util.DrawerHelper;
import com.cleanroommc.multiblocked.api.gui.util.TextFormattingUtil;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.util.LocalizationUtils;
import com.cleanroommc.multiblocked.util.Position;
import com.cleanroommc.multiblocked.util.Size;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;

public class GasStackWidget extends ContentWidget<GasStack> {

    @Override
    protected void onContentUpdate() {
        if (Multiblocked.isClient() && content != null) {
            this.setHoverTooltip(TextFormatting.AQUA + content.getGas().getLocalizedName() + TextFormatting.RESET + "\n" + LocalizationUtils
                    .format("multiblocked.gui.trait.gas.amount") + " "  + + content.amount);
        }
    }

    @Override
    public GasStack getJEIContent(Object content) {
        return super.getJEIContent(content);
    }

    @Override
    public void drawHookBackground(int mouseX, int mouseY, float partialTicks) {
        if (content != null) {
            Position pos = getPosition();
            Size size = getSize();
            Minecraft minecraft = Minecraft.getMinecraft();
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            drawGas(minecraft, pos.x + 1, pos.y + 1, content);
            GlStateManager.scale(0.5, 0.5, 1);
            String s = TextFormattingUtil.formatLongToCompactString(content.amount, 4);
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            fontRenderer.drawStringWithShadow(s, (pos.x + (size.width / 3f)) * 2 - fontRenderer.getStringWidth(s) + 21, (pos.y + (size.height / 3f) + 6) * 2, 0xFFFFFF);
            GlStateManager.popMatrix();
        }
    }

    private void drawGas(Minecraft minecraft, final int xPosition, final int yPosition, @Nullable GasStack gasStack) {
        Gas gas = gasStack == null ? null : gasStack.getGas();
        if (gas == null) {
            return;
        }
        int widthT = 18;
        int heightT = 18;
        TextureAtlasSprite fluidStillSprite = getStillGasSprite(minecraft, gas);
        GlStateManager.enableBlend();
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        MekanismRenderer.color(gas);

        final int xTileCount = widthT / 16;
        final int xRemainder = widthT - xTileCount * 16;
        final int yTileCount = heightT / 16;
        final int yRemainder = heightT - yTileCount * 16;

        final int yStart = yPosition + heightT;

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                int width = xTile == xTileCount ? xRemainder : 16;
                int height = yTile == yTileCount ? yRemainder : 16;
                int x = xPosition + xTile * 16;
                int y = yStart - (yTile + 1) * 16;
                int maskTop = 16 - height;
                int maskRight = 16 - width;
                DrawerHelper.drawFluidTexture(x, y, fluidStillSprite, maskTop, maskRight, 0.0);
            }
        }
        GlStateManager.disableBlend();
        MekanismRenderer.resetColor();
    }

    private static TextureAtlasSprite getStillGasSprite(Minecraft minecraft, Gas gas) {
        TextureMap textureMapBlocks = minecraft.getTextureMapBlocks();
        ResourceLocation gasStill = gas.getIcon();
        TextureAtlasSprite gasStillSprite = null;
        if (gasStill != null) {
            gasStillSprite = textureMapBlocks.getTextureExtry(gasStill.toString());
        }
        if (gasStillSprite == null) {
            gasStillSprite = textureMapBlocks.getMissingSprite();
        }
        return gasStillSprite;
    }

    @Override
    public void openConfigurator(WidgetGroup dialog) {
        super.openConfigurator(dialog);
        int x = 5;
        int y = 25;
        dialog.addWidget(new LabelWidget(5, y + 3, "multiblocked.gui.label.amount"));
        dialog.addWidget(new TextFieldWidget(125 - 60, y, 60, 15, true, null, number -> {
            content = new GasStack(content.getGas(), Integer.parseInt(number));
            onContentUpdate();
        }).setNumbersOnly(1, Integer.MAX_VALUE).setCurrentString(content.amount+""));
    }

}
