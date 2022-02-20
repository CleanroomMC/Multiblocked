package io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe.content;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.gui.util.DrawerHelper;
import io.github.cleanroommc.multiblocked.api.gui.util.TextFormattingUtil;
import io.github.cleanroommc.multiblocked.util.Position;
import io.github.cleanroommc.multiblocked.util.Size;
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
            this.setHoverTooltip(TextFormatting.AQUA + content.getGas().getLocalizedName() + " Gas\nAmount: " + TextFormatting.YELLOW + content.amount);
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(mouseX, mouseY, partialTicks);
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
        drawHoverOverlay(mouseX, mouseY);
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
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY)) {
            return content;
        }
        return null;
    }
}
