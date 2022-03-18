package com.cleanroommc.multiblocked.api.gui.texture;

public class GuiTextureGroup implements IGuiTexture{
    public IGuiTexture[] textures;

    public GuiTextureGroup(IGuiTexture... textures) {
        this.textures = textures;
    }

    public GuiTextureGroup setTextures(IGuiTexture[] textures) {
        this.textures = textures;
        return this;
    }

    @Override
    public void draw(double x, double y, int width, int height) {
        for (IGuiTexture texture : textures) {
            texture.draw(x, y, width, height);
        }
    }

    @Override
    public void updateTick() {
        for (IGuiTexture texture : textures) {
            texture.updateTick();
        }
    }

    @Override
    public void drawSubArea(double x, double y, int width, int height, double drawnU, double drawnV, double drawnWidth, double drawnHeight) {
        for (IGuiTexture texture : textures) {
            texture.drawSubArea(x, y, width, height, drawnU, drawnV, drawnWidth, drawnHeight);
        }
    }

    @Override
    public void draw(int mouseX, int mouseY, double x, double y, int width, int height) {
        for (IGuiTexture texture : textures) {
            texture.draw(mouseX, mouseY, x, y, width, height);
        }
    }
}
