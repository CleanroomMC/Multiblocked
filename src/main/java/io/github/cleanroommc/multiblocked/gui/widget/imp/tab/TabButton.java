package io.github.cleanroommc.multiblocked.gui.widget.imp.tab;

import io.github.cleanroommc.multiblocked.gui.util.ClickData;
import io.github.cleanroommc.multiblocked.gui.widget.imp.SwitchWidget;

public class TabButton extends SwitchWidget {
    protected TabContainer container;

    public TabButton(int xPosition, int yPosition, int width, int height) {
        super(xPosition, yPosition, width, height, null);
        this.setOnPressCallback(this::onPressed);
    }

    public void setContainer(TabContainer container) {
        this.container = container;
    }

    public void onPressed(ClickData clickData, boolean isPressed) {
        if (container != null) {
            container.switchTag(container.tabs.get(this));
        }
    }
}
