package io.github.cleanroommc.multiblocked.api.gui.widget.imp;

import io.github.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import io.github.cleanroommc.multiblocked.client.util.RenderUtils;
import io.github.cleanroommc.multiblocked.util.Position;
import io.github.cleanroommc.multiblocked.util.Size;
import net.minecraft.util.math.MathHelper;

public class DraggableScrollableWidgetGroup extends WidgetGroup {
    protected int scrollXOffset;
    protected int scrollYOffset;
    protected int xBarHeight;
    protected int yBarWidth;
    protected boolean draggable;
    protected IGuiTexture background;
    protected int maxHeight;
    protected int maxWidth;
    protected IGuiTexture xBarB;
    protected IGuiTexture xBarF;
    protected IGuiTexture yBarB;
    protected IGuiTexture yBarF;
    protected boolean focus;
    protected Widget draggedWidget;
    protected boolean useScissor;

    private int lastMouseX;
    private int lastMouseY;
    private boolean draggedPanel;
    private boolean draggedOnXScrollBar;
    private boolean draggedOnYScrollBar;


    public DraggableScrollableWidgetGroup(int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
        maxHeight = height;
        maxWidth = width;
        useScissor = true;
    }

    public DraggableScrollableWidgetGroup setXScrollBarHeight(int xBar) {
        this.xBarHeight = xBar;
        return this;
    }

    public DraggableScrollableWidgetGroup setYScrollBarWidth(int yBar) {
        this.yBarWidth = yBar;
        return this;
    }

    public DraggableScrollableWidgetGroup setDraggable(boolean draggable) {
        this.draggable = draggable;
        return this;
    }

    public DraggableScrollableWidgetGroup setBackground(IGuiTexture background) {
        this.background = background;
        return this;
    }

    public DraggableScrollableWidgetGroup setXBarStyle(IGuiTexture background, IGuiTexture bar) {
        this.xBarB = background;
        this.xBarF = bar;
        return this;
    }

    public DraggableScrollableWidgetGroup setYBarStyle(IGuiTexture background, IGuiTexture bar) {
        this.yBarB = background;
        this.yBarF = bar;
        return this;
    }

    public void setUseScissor(boolean useScissor) {
        this.useScissor = useScissor;
    }

    public int getScrollYOffset() {
        return scrollYOffset;
    }

    public int getScrollXOffset() {
        return scrollXOffset;
    }

    @Override
    public WidgetGroup addWidget(Widget widget) {
        maxHeight = Math.max(maxHeight, widget.getSize().height + widget.getSelfPosition().y);
        maxWidth = Math.max(maxWidth, widget.getSize().width + widget.getSelfPosition().x);
        Position newPos = widget.addSelfPosition(- scrollXOffset, - scrollYOffset);
        widget.setVisible(newPos.x < getSize().width - yBarWidth && newPos.x + widget.getSize().width > 0);
        widget.setVisible(newPos.y < getSize().height - xBarHeight && newPos.y + widget.getSize().height > 0);
        return super.addWidget(widget);
    }

    @Override
    public void removeWidget(Widget widget) {
        super.removeWidget(widget);
        computeMax();
    }

    @Override
    public void clearAllWidgets() {
        super.clearAllWidgets();
        maxHeight = getSize().height;
        maxWidth = getSize().width;
        scrollXOffset = 0;
        scrollYOffset = 0;
    }

    @Override
    public void setSize(Size size) {
        super.setSize(size);
        maxHeight = Math.max(size.height, maxHeight);
        maxWidth = Math.max(size.width, maxWidth);
//        computeMax();
        for (Widget widget : widgets) {
            Position newPos = widget.getSelfPosition();
            widget.setVisible(newPos.x < getSize().width - yBarWidth && newPos.x + widget.getSize().width > 0);
            widget.setVisible(newPos.y < getSize().height - xBarHeight && newPos.y + widget.getSize().height > 0);
        }
    }

    public void computeMax() {
        int mh = 0;
        int mw = 0;
        for (Widget widget : widgets) {
            mh = Math.max(mh, widget.getSize().height + widget.getSelfPosition().y + scrollYOffset);
            mw = Math.max(mw, widget.getSize().width + widget.getSelfPosition().x + scrollXOffset);
        }
        int offsetY = 0;
        int offsetX = 0;
        if (mh > getSize().height) {
            offsetY = maxHeight - mh;
            maxHeight = mh;
            if (scrollYOffset - offsetY < 0) {
                offsetY = scrollYOffset;
            }
            scrollYOffset -= offsetY;
        } else if (mh < getSize().height) {
            offsetY = maxHeight - getSize().height;
            maxHeight = getSize().height;
            if (scrollYOffset - offsetY < 0) {
                offsetY = scrollYOffset;
            }
            scrollYOffset -= offsetY;
        }
        if (mw > getSize().width) {
            offsetX = maxWidth - mw;
            maxWidth = mw;
            if (scrollXOffset - offsetX < 0) {
                offsetX = scrollXOffset;
            }
            scrollXOffset -= offsetX;
        }else if (mw < getSize().width) {
            offsetX = maxWidth - getSize().width;
            maxWidth = getSize().width;
            if (scrollXOffset - offsetX < 0) {
                offsetX = scrollXOffset;
            }
            scrollXOffset -= offsetX;
        }
        if (offsetX != 0 || offsetY != 0) {
            for (Widget widget : widgets) {
                Position newPos = widget.addSelfPosition(offsetX, offsetY);
                widget.setVisible(newPos.x < getSize().width - yBarWidth && newPos.x + widget.getSize().width > 0);
                widget.setVisible(newPos.y < getSize().height - xBarHeight && newPos.y + widget.getSize().height > 0);
            }
        }
    }

    protected int getMaxHeight() {
        return maxHeight + xBarHeight;
    }

    protected int getMaxWidth() {
        return maxWidth + yBarWidth;
    }

    public int getWidgetBottomHeight() {
        int y = 0;
        for (Widget widget : widgets) {
            y = Math.max(y, widget.getSize().height + widget.getSelfPosition().y);
        }
        return y;
    }

    protected void setScrollXOffset(int scrollXOffset) {
        if (scrollXOffset == this.scrollXOffset) return;
        int offset = scrollXOffset - this.scrollXOffset;
        this.scrollXOffset = scrollXOffset;
        for (Widget widget : widgets) {
            Position newPos = widget.addSelfPosition( - offset, 0);
            widget.setVisible(newPos.x < getSize().width - yBarWidth && newPos.x + widget.getSize().width > 0);
        }
    }

    protected void setScrollYOffset(int scrollYOffset) {
        if (scrollYOffset == this.scrollYOffset) return;
        if (scrollYOffset < 0) scrollYOffset = 0;
        int offset = scrollYOffset - this.scrollYOffset;
        this.scrollYOffset = scrollYOffset;
        for (Widget widget : widgets) {
            Position newPos = widget.addSelfPosition(0, - offset);
            widget.setVisible(newPos.y < getSize().height - xBarHeight && newPos.y + widget.getSize().height > 0);
        }
    }

    private boolean isOnXScrollPane(int mouseX, int mouseY) {
        Position pos = getPosition();
        Size size = getSize();
        return isMouseOver(pos.x, pos.y + size.height - xBarHeight, size.width, xBarHeight, mouseX, mouseY);
    }

    private boolean isOnYScrollPane(int mouseX, int mouseY) {
        Position pos = getPosition();
        Size size = getSize();
        return isMouseOver(pos.x + size.width - yBarWidth, pos.y, yBarWidth, size.height, mouseX, mouseY);
    }

    protected boolean hookDrawInBackground(int mouseX, int mouseY, float partialTicks) {
        return false;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        int x = getPosition().x;
        int y = getPosition().y;
        int width = getSize().width;
        int height = getSize().height;
        if (background != null) {
            background.draw(x, y, width, height);
        }
        if (useScissor) {
            RenderUtils.useScissor(x, y, width - yBarWidth, height - xBarHeight, ()->{
                if(!hookDrawInBackground(mouseX, mouseY, partialTicks)) {
                    super.drawInBackground(mouseX, mouseY, partialTicks);
                }
            });
        } else {
            if(!hookDrawInBackground(mouseX, mouseY, partialTicks)) {
                super.drawInBackground(mouseX, mouseY, partialTicks);
            }
        }

        if (xBarHeight > 0) {
            if (xBarB != null) {
                xBarB.draw(x, y - xBarHeight, width, xBarHeight);
            }
            if (xBarF != null) {
                int barWidth = (int) (width * 1.0f / getMaxWidth() * width);
                xBarF.draw(x + scrollXOffset * width * 1.0f / getMaxWidth(), y + height - xBarHeight, barWidth, xBarHeight);
            }
        }
        if (yBarWidth > 0) {
            if (yBarB != null) {
                yBarB.draw(x + width  - yBarWidth, y, yBarWidth, height);
            }
            if (yBarF != null) {
                int barHeight = (int) (height * 1.0f / getMaxHeight() * height);
                yBarF.draw(x + width  - yBarWidth, y + scrollYOffset * height * 1.0f / getMaxHeight(), yBarWidth, barHeight);
            }
        }
    }

    @Override
    public Widget mouseClicked(int mouseX, int mouseY, int button) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        Widget widget;
        if (xBarHeight > 0 && isOnXScrollPane(mouseX, mouseY)) {
            this.draggedOnXScrollBar = true;
            focus = true;
            return this;
        }
        else if (yBarWidth > 0 && isOnYScrollPane(mouseX, mouseY)) {
            this.draggedOnYScrollBar = true;
            focus = true;
            return this;
        } else if(isMouseOverElement(mouseX, mouseY)){
            focus = true;
            if ((widget = checkClickedDragged(mouseX, mouseY, button)) != null) {
                return widget;
            }
            if (draggable) {
                this.draggedPanel = true;
                return this;
            }
            return null;
        } else if ((widget = checkClickedDragged(mouseX, mouseY, button)) != null) {
            return widget;
        }
        focus = false;
        return null;
    }

    protected Widget checkClickedDragged(int mouseX, int mouseY, int button) {
        draggedWidget = null;
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);
            if(widget.isVisible()) {
                Widget w;
                if((w = widget.mouseClicked(mouseX, mouseY, button)) != null) {
                    return w;
                } else if (widget instanceof IDraggable && ((IDraggable) widget).allowDrag(mouseX, mouseY, button)) {
                    draggedWidget = widget;
                    ((IDraggable) widget).startDrag(mouseX, mouseY);
                    return widget;
                }
            }
        }
        return null;
    }

    @Override
    public Widget mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            Widget widget;
            if ((widget = super.mouseWheelMove(mouseX, mouseY, wheelDelta)) != null) {
                return widget;
            }
            int moveDelta = -MathHelper.clamp(wheelDelta, -1, 1) * 13;
            if (getMaxHeight() - getSize().height > 0 || scrollYOffset > getMaxHeight() - getSize().height) {
                setScrollYOffset(MathHelper.clamp(scrollYOffset + moveDelta, 0, getMaxHeight() - getSize().height));
            }
            return this;
        }
        focus = false;
        return null;
    }

    @Override
    public Widget mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        int deltaX = mouseX - lastMouseX;
        int deltaY = mouseY - lastMouseY;
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        if (draggedOnXScrollBar && (getMaxWidth() - getSize().width > 0 || scrollYOffset > getMaxWidth() - getSize().width)) {
            setScrollXOffset(MathHelper.clamp(scrollXOffset + deltaX * getMaxWidth() / getSize().width, 0, getMaxWidth() - getSize().width));
            return this;
        } else if (draggedOnYScrollBar && (getMaxHeight() - getSize().height > 0 || scrollYOffset > getMaxHeight() - getSize().height)) {
            setScrollYOffset(MathHelper.clamp(scrollYOffset + deltaY * getMaxHeight() / getSize().height, 0, getMaxHeight() - getSize().height));
            return this;
        } else if (draggedWidget != null) {
            if (((IDraggable)draggedWidget).dragging(mouseX, mouseY, deltaX, deltaY)) {
                draggedWidget.addSelfPosition(deltaX, deltaY);
            }
            computeMax();
            return this;
        } else if (draggedPanel) {
            setScrollXOffset(MathHelper.clamp(scrollXOffset - deltaX, 0, Math.max(getMaxWidth() - yBarWidth - getSize().width, 0)));
            setScrollYOffset(MathHelper.clamp(scrollYOffset - deltaY, 0, Math.max(getMaxHeight() - xBarHeight - getSize().height, 0)));
            return this;
        }
        return super.mouseDragged(mouseX, mouseY, button, timeDragged);
    }

    @Override
    public Widget mouseReleased(int mouseX, int mouseY, int button) {
        if (draggedOnXScrollBar) {
            draggedOnXScrollBar = false;
        } else if (draggedOnYScrollBar) {
            draggedOnYScrollBar = false;
        } else if (draggedWidget != null) {
            ((IDraggable)draggedWidget).endDrag(mouseX, mouseY);
            draggedWidget = null;
        } else if (draggedPanel) {
            draggedPanel = false;
        } else {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        return this;
    }

    public interface IDraggable {
        boolean allowDrag(int mouseX, int mouseY, int button);
        default void startDrag(int mouseX, int mouseY) {}
        default boolean dragging(int mouseX, int mouseY, int deltaX, int deltaY) {return true;}
        default void endDrag(int mouseX, int mouseY) {}
    }
}
