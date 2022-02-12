package io.github.cleanroommc.multiblocked.api.gui.texture;

import mezz.jei.api.gui.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public interface IGuiTexture {

    @Deprecated
    default void draw(double x, double y, int width, int height) {
        draw(0, 0, x, y, width, height);
    }
    
    void draw(int mouseX, int mouseY, double x, double y, int width, int height);
    
    default void updateTick() { }
    
    IGuiTexture EMPTY = (mouseX, mouseY, x, y, width, height) -> {};

    default void drawSubArea(double x, double y, int width, int height, double drawnU, double drawnV, double drawnWidth, double drawnHeight) {
        draw(x, y, width, height);
    }


    @SideOnly(Side.CLIENT)
    @Optional.Method(modid = "jei")
    default IDrawable toDrawable(final int width, final int height) {
        return new IDrawable() {
            @Override
            public int getWidth() {
                return width;
            }

            @Override
            public int getHeight() {
                return height;
            }

            @Override
            public void draw(@Nonnull Minecraft minecraft, int x, int y) {
                IGuiTexture.this.draw(0, 0, x, y, width, height);
            }
        };
    }
}
