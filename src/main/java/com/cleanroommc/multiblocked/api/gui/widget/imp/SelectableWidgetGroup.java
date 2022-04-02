package com.cleanroommc.multiblocked.api.gui.widget.imp;

import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.util.Position;
import com.cleanroommc.multiblocked.util.Size;

import java.util.function.Consumer;

public class SelectableWidgetGroup extends WidgetGroup implements DraggableScrollableWidgetGroup.ISelected {
    protected boolean isSelected;
    protected IGuiTexture selectedTexture;
    protected Consumer<SelectableWidgetGroup> onSelected;
    protected Consumer<SelectableWidgetGroup> onUnSelected;

    public SelectableWidgetGroup(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public SelectableWidgetGroup(Position position) {
        super(position);
    }

    public SelectableWidgetGroup(Position position, Size size) {
        super(position, size);
    }

    public boolean isSelected() {
        return isSelected;
    }

    public SelectableWidgetGroup setOnSelected(Consumer<SelectableWidgetGroup> onSelected) {
        this.onSelected = onSelected;
        return this;
    }

    public SelectableWidgetGroup setOnUnSelected(Consumer<SelectableWidgetGroup> onUnSelected) {
        this.onUnSelected = onUnSelected;
        return this;
    }

    public SelectableWidgetGroup setSelectedTexture(IGuiTexture selectedTexture) {
        this.selectedTexture = selectedTexture;
        return this;
    }

    public SelectableWidgetGroup setSelectedTexture(int border, int color) {
        this.selectedTexture = new ColorBorderTexture(border, color);
        return this;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(mouseX, mouseY, partialTicks);
        if (isSelected && selectedTexture != null) {
            selectedTexture.draw(mouseX, mouseY, getPosition().x, getPosition().y, getSize().width, getSize().height);
        }
    }

    @Override
    public boolean allowSelected(int mouseX, int mouseY, int button) {
        return isMouseOverElement(mouseX, mouseY);
    }

    @Override
    public void onSelected() {
        isSelected = true;
        if (onSelected != null) onSelected.accept(this);
    }

    @Override
    public void onUnSelected() {
        isSelected = false;
        if (onUnSelected != null) onUnSelected.accept(this);
    }
}
