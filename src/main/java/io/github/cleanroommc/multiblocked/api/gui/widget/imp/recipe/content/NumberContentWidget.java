package io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe.content;

import io.github.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.util.Position;
import io.github.cleanroommc.multiblocked.util.Size;

public class NumberContentWidget extends ContentWidget<Number>{
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
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (contentTexture != null) {
            contentTexture.updateTick();
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(mouseX, mouseY, partialTicks);
        Position position = getPosition();
        Size size = getSize();
        if (contentTexture != null) {
            contentTexture.draw(mouseX, mouseY, position.x + 2, position.y + 2, size.width - 4, size.height - 4);
        }
    }
}
