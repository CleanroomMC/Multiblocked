package io.github.cleanroommc.multiblocked.gui.widget.imp.tab;

import io.github.cleanroommc.multiblocked.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.gui.util.ClickData;
import io.github.cleanroommc.multiblocked.gui.widget.imp.SwitchWidget;

import java.util.function.BiConsumer;

public class TabButton extends SwitchWidget {
    protected TabContainer container;

    public TabButton(int xPosition, int yPosition, int width, int height) {
        super(xPosition, yPosition, width, height, null);
        this.setOnPressCallback(this::onPressed);
    }

    @Override
    public TabButton setTexture(IGuiTexture baseTexture, IGuiTexture pressedTexture) {
        this.baseTexture = baseTexture;
        this.pressedTexture = pressedTexture;
        return this;
    }

    public void setContainer(TabContainer container) {
        this.container = container;
    }

    public void onPressed(ClickData clickData, boolean isPressed) {
        this.isPressed = true;
        if (container != null) {
            container.switchTag(container.tabs.get(this));
        }
    }
}
