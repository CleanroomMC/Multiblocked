package com.cleanroommc.multiblocked.api.gui.widget.imp;

import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.util.Position;
import com.cleanroommc.multiblocked.util.Size;

import java.util.function.Consumer;

public class DraggableWidgetGroup extends WidgetGroup implements DraggableScrollableWidgetGroup.IDraggable {
    protected boolean isSelected;
    protected IGuiTexture selectedTexture;
    protected Consumer<DraggableWidgetGroup> onSelected;
    protected Consumer<DraggableWidgetGroup> onUnSelected;
    protected Consumer<DraggableWidgetGroup> onStartDrag;
    protected Consumer<DraggableWidgetGroup> onDragging;
    protected Consumer<DraggableWidgetGroup> onEndDrag;

    public DraggableWidgetGroup(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public DraggableWidgetGroup(Position position) {
        super(position);
    }

    public DraggableWidgetGroup(Position position, Size size) {
        super(position, size);
    }

    public boolean isSelected() {
        return isSelected;
    }

    public DraggableWidgetGroup setOnSelected(Consumer<DraggableWidgetGroup> onSelected) {
        this.onSelected = onSelected;
        return this;
    }

    public DraggableWidgetGroup setOnUnSelected(Consumer<DraggableWidgetGroup> onUnSelected) {
        this.onUnSelected = onUnSelected;
        return this;
    }

    public DraggableWidgetGroup setOnStartDrag(Consumer<DraggableWidgetGroup> onStartDrag) {
        this.onStartDrag = onStartDrag;
        return this;
    }

    public DraggableWidgetGroup setOnDragging(Consumer<DraggableWidgetGroup> onDragging) {
        this.onDragging = onDragging;
        return this;
    }

    public DraggableWidgetGroup setOnEndDrag(Consumer<DraggableWidgetGroup> onEndDrag) {
        this.onEndDrag = onEndDrag;
        return this;
    }

    public DraggableWidgetGroup setSelectedTexture(IGuiTexture selectedTexture) {
        this.selectedTexture = selectedTexture;
        return this;
    }

    public DraggableWidgetGroup setSelectedTexture(int border, int color) {
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

    @Override
    public void startDrag(int mouseX, int mouseY) {
        if (onStartDrag != null) {
            onStartDrag.accept(this);
        }
    }

    @Override
    public boolean dragging(int mouseX, int mouseY, int deltaX, int deltaY) {
        if (onDragging != null) {
            onDragging.accept(this);
        }
        return true;
    }

    @Override
    public void endDrag(int mouseX, int mouseY) {
        if (onEndDrag != null) {
            onEndDrag.accept(this);
        }
    }
}
