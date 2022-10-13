package com.cleanroommc.multiblocked.api.gui.texture;

import com.cleanroommc.multiblocked.api.gui.util.DrawerHelper;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public class ColorRectTexture implements IGuiTexture{
    public int color;

    public ColorRectTexture(int color) {
        this.color = color;
    }

    public ColorRectTexture(Color color) {
        this.color = color.getRGB();
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    @Override
    public void draw(int mouseX, int mouseY, double x, double y, int width, int height) {
        DrawerHelper.drawSolidRect((int) x, (int) y, width, height, color);
    }
}
