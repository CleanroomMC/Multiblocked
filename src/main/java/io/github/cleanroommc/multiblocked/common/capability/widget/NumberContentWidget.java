package io.github.cleanroommc.multiblocked.common.capability.widget;

import io.github.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.api.gui.util.TextFormattingUtil;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import io.github.cleanroommc.multiblocked.util.Position;
import io.github.cleanroommc.multiblocked.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class NumberContentWidget extends ContentWidget<Number> {
    protected boolean isDecimal;
    protected IGuiTexture contentTexture;
    protected String unit;

    public NumberContentWidget setContentTexture(IGuiTexture contentTexture) {
        this.contentTexture = contentTexture;
        return this;
    }

    public NumberContentWidget setUnit(String unit) {
        this.unit = unit;
        return this;
    }

    @Override
    protected void onContentUpdate() {
        isDecimal = content instanceof Float || content instanceof Double;
        this.setHoverTooltip(content + " " + unit);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (contentTexture != null) {
            contentTexture.updateTick();
        }
    }

    @Override
    public void drawHookBackground(int mouseX, int mouseY, float partialTicks) {
        Position position = getPosition();
        Size size = getSize();
        if (contentTexture != null) {
            contentTexture.draw(mouseX, mouseY, position.x + 1, position.y + 1, size.width - 2, size.height - 2);
        }
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5, 0.5, 1);
        String s = TextFormattingUtil.formatLongToCompactString(content.intValue(), 4);
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        fontRenderer.drawStringWithShadow(s, (position.x + (size.width / 3f)) * 2 - fontRenderer.getStringWidth(s) + 21, (position.y + (size.height / 3f) + 6) * 2, 0xFFFFFF);
        GlStateManager.popMatrix();
    }
}
