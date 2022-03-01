package io.github.cleanroommc.multiblocked.api.gui.widget.imp;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.util.Position;
import io.github.cleanroommc.multiblocked.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;

public class TextBoxWidget extends Widget {

    // config
    public List<String> content;
    public int space = 1;
    public int fontSize = 9;
    public int fontColor = 0xff000000;
    public boolean isShadow = false;
    public boolean isCenter = false;

    private transient List<String> textLines;

    public TextBoxWidget(int x, int y, int width, List<String> content) {
        super(x, y, width, 0);
        this.content = content;
        this.calculate();
    }

    public TextBoxWidget setContent(List<String> content) {
        this.content = content;
        this.calculate();
        return this;
    }

    public TextBoxWidget setSpace(int space) {
        this.space = space;
        this.calculate();
        return this;
    }

    public TextBoxWidget setFontSize(int fontSize) {
        this.fontSize = fontSize;
        this.calculate();
        return this;
    }

    public TextBoxWidget setFontColor(int fontColor) {
        this.fontColor = fontColor;
        this.calculate();
        return this;
    }

    public TextBoxWidget setShadow(boolean shadow) {
        isShadow = shadow;
        this.calculate();
        return this;
    }

    public TextBoxWidget setCenter(boolean center) {
        isCenter = center;
        this.calculate();
        return this;
    }

    protected void calculate() {
        if (Multiblocked.isClient()) {
            this.textLines = new ArrayList<>();
            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            this.space = Math.max(space, 0);
            this.fontSize = Math.max(fontSize, 1);
            int wrapWidth = getSize().width * font.FONT_HEIGHT / fontSize;
            if (content != null) {
                for (String textLine : content) {
                    this.textLines.addAll(font.listFormattedStringToWidth(I18n.format(textLine), wrapWidth));
                }
            }
            this.setSize(new Size(this.getSize().width, this.textLines.size() * (fontSize + space)));
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(mouseX, mouseY, partialTicks);
        if (!textLines.isEmpty()) {
            Position position = getPosition();
            Size size = getSize();
            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            float scale = fontSize * 1.0f / font.FONT_HEIGHT;
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 1);
            GlStateManager.translate(position.x / scale, position.y / scale, 0);
            float x = 0;
            float y = 0;
            float ySpace = font.FONT_HEIGHT + space / scale;
            for (String textLine : textLines) {
                if (isCenter) {
                    x = (size.width / scale - font.getStringWidth(textLine)) / 2;
                }
                font.drawString(textLine, x, y, fontColor, isShadow);
                y += ySpace;
            }
            GlStateManager.popMatrix();
        }
    }
}