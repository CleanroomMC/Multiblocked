package com.cleanroommc.multiblocked.common.capability.widget;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.util.TextFormattingUtil;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.common.recipe.content.AspectStack;
import com.cleanroommc.multiblocked.util.LocalizationUtils;
import com.cleanroommc.multiblocked.util.Position;
import com.cleanroommc.multiblocked.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.AspectList;

import java.awt.Color;

public class AspectStackWidget extends ContentWidget<AspectStack> {
    @Override
    protected void onContentUpdate() {
        if (Multiblocked.isClient() && content != null) {
            this.setHoverTooltip(TextFormatting.AQUA + content.aspect.getLocalizedDescription() + TextFormatting.RESET + "\n" + LocalizationUtils.format("multiblocked.gui.trait.aspect.amount") + " "  + content.amount);
        }
    }

    @Override
    public void drawHookBackground(int mouseX, int mouseY, float partialTicks) {
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
    }

    @Override
    public AspectStack getJEIContent(Object content) {
        if (content instanceof AspectList) {
            return new AspectStack(((AspectList) content).getAspects()[0], this.content.amount);
        }
        return null;
    }

    @Override
    public Object getJEIIngredient(AspectStack content) {
        return content.toAspectList();
    }

    @Override
    public void openConfigurator(WidgetGroup dialog) {
        super.openConfigurator(dialog);
        int x = 5;
        int y = 25;
        dialog.addWidget(new LabelWidget(5, y + 3, "multiblocked.gui.label.amount"));
        dialog.addWidget(new TextFieldWidget(125 - 60, y, 60, 15, true, null, number -> {
            content = new AspectStack(content.aspect, Integer.parseInt(number));
            onContentUpdate();
        }).setNumbersOnly(1, Integer.MAX_VALUE).setCurrentString(content.amount+""));
    }
}
