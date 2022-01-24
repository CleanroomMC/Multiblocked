package io.github.cleanroommc.multiblocked.api.gui.widget.imp.content;

import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.util.Position;
import io.github.cleanroommc.multiblocked.util.Size;

import javax.annotation.Nonnull;

public class NumberContentWidget extends ContentWidget<Number>{
    protected final boolean isDecimal;
    protected IGuiTexture contentTexture;
    protected String unit;

    public NumberContentWidget(@Nonnull IO io, @Nonnull Number object) {
        super(io, object);
        isDecimal = object instanceof Float || object instanceof Double;
    }

    public NumberContentWidget setContentTexture(IGuiTexture contentTexture) {
        this.contentTexture = contentTexture;
        return this;
    }

    public NumberContentWidget setUnit(String unit) {
        this.unit = unit;
        return this;
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
