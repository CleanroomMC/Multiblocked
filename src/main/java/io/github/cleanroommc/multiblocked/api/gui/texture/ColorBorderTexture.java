package io.github.cleanroommc.multiblocked.api.gui.texture;

import io.github.cleanroommc.multiblocked.api.gui.util.DrawerHelper;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.awt.Color;

public class ColorBorderTexture implements IGuiTexture{
    public int color;
    public int border;

    public ColorBorderTexture(int border, int color) {
        this.color = color;
        this.border = border;
    }

    public ColorBorderTexture(int border, Color color) {
        this.color = color.getRGB();
        this.border = border;
    }

    public ColorBorderTexture setBorder(int border) {
        this.border = border;
        return this;
    }
    
    

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    @Override
    public void draw(int mouseX, int mouseY, double x, double y, int width, int height) {
        DrawerHelper.drawBorder((int)x, (int)y, width, height, color, border);
    }
}
