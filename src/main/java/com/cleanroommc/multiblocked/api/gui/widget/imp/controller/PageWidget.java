package com.cleanroommc.multiblocked.api.gui.widget.imp.controller;

import com.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabButton;
import com.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabContainer;

public abstract class PageWidget extends WidgetGroup {
    protected final ResourceTexture page;
    protected final IGuiTexture background;

    public PageWidget(ResourceTexture page, TabContainer tabContainer) {
        super(20, 0, 176, 256);
        this.page = page;
        this.background = page.getSubTexture(0, 0, 176 / 256.0, 1);
        tabContainer.addTab(new TabButton(0, tabContainer.containerGroup.widgets.size() * 20, 20, 20)
                        .setTexture(page.getSubTexture(176 / 256.0, 216 / 256.0, 20 / 256.0, 20 / 256.0),
                                page.getSubTexture(176 / 256.0, 236 / 256.0, 20 / 256.0, 20 / 256.0)),
                this);
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        int x = getPosition().x;
        int y = getPosition().y;
        int width = getSize().width;
        int height = getSize().height;
        background.draw(mouseX, mouseY, x, y, width, height);
        super.drawInBackground(mouseX, mouseY, partialTicks);
    }
}
