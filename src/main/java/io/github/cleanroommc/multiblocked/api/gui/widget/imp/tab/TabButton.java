package io.github.cleanroommc.multiblocked.api.gui.widget.imp.tab;

import io.github.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.api.gui.util.ClickData;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.SwitchWidget;

public class TabButton extends SwitchWidget {
    protected TabContainer container;

    public TabButton(int xPosition, int yPosition, int width, int height) {
        super(xPosition, yPosition, width, height, null);
        this.setOnPressCallback(this::onPressed);
    }

    @Override
    public TabButton setTexture(IGuiTexture baseTexture, IGuiTexture pressedTexture) {
        super.setTexture(baseTexture, pressedTexture);
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
