package io.github.cleanroommc.multiblocked.api.gui.texture;

public interface IGuiTexture {
    
    default void draw(double x, double y, int width, int height) {
        draw(0, 0, x, y, width, height);
    }
    
    void draw(int mouseX, int mouseY, double x, double y, int width, int height);
    
    default void updateTick() { }
    
    IGuiTexture EMPTY = (mouseX, mouseY, x, y, width, height) -> {};
}
