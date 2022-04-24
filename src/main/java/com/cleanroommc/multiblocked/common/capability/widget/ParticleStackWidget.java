package com.cleanroommc.multiblocked.common.capability.widget;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.util.TextFormattingUtil;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.util.Position;
import com.cleanroommc.multiblocked.util.Size;
import lach_01298.qmd.particle.Particle;
import lach_01298.qmd.particle.ParticleStack;
import lach_01298.qmd.util.Units;
import nc.util.Lang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

public class ParticleStackWidget extends ContentWidget<ParticleStack> {

    @Override
    protected void onContentUpdate() {
        if (Multiblocked.isClient() && content != null) {
            String tips = Lang.localise(content.getParticle().getUnlocalizedName()) + '\n' +
                            TextFormatting.YELLOW + Lang.localise("gui.qmd.particlestack.amount", Units.getSIFormat(content.getAmount(), "pu")) + '\n' +
                            TextFormatting.DARK_GREEN + Lang.localise("gui.qmd.particlestack.mean_energy", Units.getParticleEnergy(content.getMeanEnergy())) + '\n' +
                            TextFormatting.RED + Lang.localise("gui.qmd.particlestack.focus", Units.getSIFormat(content.getFocus(), ""));
            this.setHoverTooltip(tips);
        }
    }

    @Override
    public ParticleStack getJEIContent(Object content) {
        if (content instanceof ParticleStack) {
            return new ParticleStack(((ParticleStack) content).getParticle(), this.content.getAmount(), this.content.getMeanEnergy(), this.content.getFocus());
        }
        return null;
    }

    @Override
    public void drawHookBackground(int mouseX, int mouseY, float partialTicks) {
        if (content != null) {
            Position pos = getPosition();
            Size size = getSize();
            Particle particle = content.getParticle();
            if (particle != null) {
                Minecraft.getMinecraft().renderEngine.bindTexture(content.getParticle().getTexture());
                GL11.glColor4f(1F, 1F, 1F, 1F);
                GlStateManager.enableBlend();
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferBuilder = tessellator.getBuffer();
                bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                bufferBuilder.pos(pos.x + 2, pos.y + 18, 0).tex(0, 1).endVertex();
                bufferBuilder.pos(pos.x + 18, (pos.y + 18), 0).tex(1, 1).endVertex();
                bufferBuilder.pos(pos.x + 18, pos.y + 2, 0).tex(1, 0).endVertex();
                bufferBuilder.pos(pos.x + 2, pos.y + 2, 0).tex(0, 0).endVertex();
                tessellator.draw();
                if (content.getAmount() > 1) {
                    GlStateManager.pushMatrix();
                    GlStateManager.scale(0.5, 0.5, 1);
                    String s = TextFormattingUtil.formatLongToCompactString(content.getAmount(), 4);
                    FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
                    fontRenderer.drawStringWithShadow(s, (pos.x + (size.width / 3f)) * 2 - fontRenderer.getStringWidth(s) + 21, (pos.y + (size.height / 3f) + 6) * 2, 0xFFFFFF);
                    GlStateManager.popMatrix();
                }
            }
        }
    }

    @Override
    public void openConfigurator(WidgetGroup dialog) {
        super.openConfigurator(dialog);
        int x = 125 - 60;
        int y = 25;
        dialog.addWidget(new LabelWidget(5, y + 3, "multiblocked.gui.label.amount"));
        dialog.addWidget(new TextFieldWidget(x, y, 60, 15, true, null, number -> {
            content = new ParticleStack(content.getParticle(), Integer.parseInt(number), content.getMeanEnergy(), content.getFocus());
            onContentUpdate();
        }).setNumbersOnly(1, Integer.MAX_VALUE).setCurrentString(content.getAmount()+""));

        dialog.addWidget(new LabelWidget(5, y + 21, "multiblocked.gui.label.energy"));
        dialog.addWidget(new TextFieldWidget(x, y + 18, 60, 15, true, null, number -> {
            content = new ParticleStack(content.getParticle(), content.getAmount(), Long.parseLong(number), content.getFocus());
            onContentUpdate();
        }).setNumbersOnly(0, Long.MAX_VALUE).setCurrentString(content.getMeanEnergy()+""));

        dialog.addWidget(new LabelWidget(5, y + 39, "multiblocked.gui.label.focus"));
        dialog.addWidget(new TextFieldWidget(x, y + 36, 60, 15, true, null, number -> {
            content = new ParticleStack(content.getParticle(),  content.getAmount(), content.getMeanEnergy(), Float.parseFloat(number));
            onContentUpdate();
        }).setNumbersOnly(1f, Float.MAX_VALUE).setCurrentString(content.getFocus()+""));

    }

}
