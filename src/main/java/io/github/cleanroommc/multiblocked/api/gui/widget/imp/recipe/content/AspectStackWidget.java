package io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe.content;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.gui.util.TextFormattingUtil;
import io.github.cleanroommc.multiblocked.common.recipe.content.AspectStack;
import io.github.cleanroommc.multiblocked.util.Position;
import io.github.cleanroommc.multiblocked.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public class AspectStackWidget extends ContentWidget<AspectStack> {
    @Override
    protected void onContentUpdate() {
        if (Multiblocked.isClient() && content != null) {
            this.setHoverTooltip(TextFormatting.AQUA + content.aspect.getLocalizedDescription() + "\nAmount: " + TextFormatting.YELLOW + content.amount);
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
            minecraft.renderEngine.bindTexture(content.aspect.getImage());
            GlStateManager.enableBlend();
            Color c = new Color(content.aspect.getColor());
            GL11.glColor4f((float) c.getRed() / 255.0F, (float) c.getGreen() / 255.0F, (float) c.getBlue() / 255.0F, 1.0F);
            Gui.drawModalRectWithCustomSizedTexture(pos.x + 2, pos.y + 2, 0, 0, 16, 16, 16, 16);
            GL11.glColor4f(1F, 1F, 1F, 1F);
            if (content.amount > 1) {
                GlStateManager.scale(0.5, 0.5, 1);
                String s = TextFormattingUtil.formatLongToCompactString(content.amount, 4);
                FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
                fontRenderer.drawStringWithShadow(s, (pos.x + (size.width / 3f)) * 2 - fontRenderer.getStringWidth(s) + 21, (pos.y + (size.height / 3f) + 6) * 2, 0xFFFFFF);
            }
            GlStateManager.popMatrix();
        }
        drawHoverOverlay(mouseX, mouseY);
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY) && content != null) {
            return content.toAspectList();
        }
        return null;
    }
}
