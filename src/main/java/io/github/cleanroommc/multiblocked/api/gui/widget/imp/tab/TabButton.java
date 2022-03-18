package io.github.cleanroommc.multiblocked.api.gui.widget.imp.tab;

import io.github.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.api.gui.util.ClickData;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
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

    @Override
    public TabButton setBaseTexture(IGuiTexture... baseTexture) {
        return (TabButton) super.setBaseTexture(baseTexture);
    }

    @Override
    public TabButton setPressedTexture(IGuiTexture... pressedTexture) {
        return (TabButton) super.setPressedTexture(pressedTexture);
    }

    @Override
    public TabButton setHoverTexture(IGuiTexture... hoverTexture) {
        return (TabButton) super.setHoverTexture(hoverTexture);
    }

    @Override
    public TabButton setHoverBorderTexture(int border, int color) {
        return (TabButton) super.setHoverBorderTexture(border, color);
    }

    @Override
    public TabButton setHoverTooltip(String tooltipText) {
        return (TabButton) super.setHoverTooltip(tooltipText);
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
