package io.github.cleanroommc.multiblocked.api.gui.widget.imp.content;

import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import io.github.cleanroommc.multiblocked.util.Position;
import io.github.cleanroommc.multiblocked.util.Size;

import javax.annotation.Nonnull;

public class ContentWidget<T> extends WidgetGroup {
    protected T content;
    protected IO io;
    protected IGuiTexture background;

    public ContentWidget(@Nonnull IO io, @Nonnull T object) {
        super(0, 0, 20, 20);
        this.io = io;
        this.content = object;
    }

    public ContentWidget<?> setSelfPosition(int x, int y) {
        setSelfPosition(new Position(x, y));
        return this;
    }

    public ContentWidget<?> setContent(@Nonnull IO io, @Nonnull T content) {
        this.io = io;
        this.content = content;
        return this;
    }

    public ContentWidget<?> setBackground(IGuiTexture background) {
        this.background = background;
        return this;
    }

    @Override
    public ContentWidget<T> addWidget(Widget widget) {
        super.addWidget(widget);
        return this;
    }

    @Override
    public ContentWidget<T> setHoverTooltip(String tooltipText) {
        super.setHoverTooltip(tooltipText);
        return this;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (background != null) {
            background.updateTick();
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        Position position = getPosition();
        Size size = getSize();
        if (background != null) {
            background.draw(mouseX, mouseY, position.x, position.y, size.width, size.height);
        }
        super.drawInBackground(mouseX, mouseY, partialTicks);
    }
}
