package io.github.cleanroommc.multiblocked.api.gui.widget;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.gui.ingredient.IGhostIngredientTarget;
import io.github.cleanroommc.multiblocked.api.gui.ingredient.IIngredientSlot;
import io.github.cleanroommc.multiblocked.api.gui.modular.WidgetUIAccess;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.SlotWidget;
import io.github.cleanroommc.multiblocked.util.Position;
import io.github.cleanroommc.multiblocked.util.Size;
import mezz.jei.api.gui.IGhostIngredientHandler.Target;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class WidgetGroup extends Widget implements IGhostIngredientTarget, IIngredientSlot {

    public transient final List<Widget> widgets = new ArrayList<>();
    private transient final WidgetGroupUIAccess groupUIAccess = new WidgetGroupUIAccess();
    private transient final boolean isDynamicSized;
    private transient boolean initialized = false;
    protected transient List<Widget> waitToRemoved;

    public WidgetGroup(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.isDynamicSized = false;
    }

    public WidgetGroup(Position position) {
        super(position, Size.ZERO);
        this.isDynamicSized = true;
    }

    public WidgetGroup(Position position, Size size) {
        super(position, size);
        this.isDynamicSized = false;
    }

    public List<Widget> getContainedWidgets(boolean includeHidden) {
        ArrayList<Widget> containedWidgets = new ArrayList<>(widgets.size());

        for (Widget widget : widgets) {
            if (!widget.isVisible() && !includeHidden) continue;
            containedWidgets.add(widget);
            if (widget instanceof WidgetGroup)
                containedWidgets.addAll(((WidgetGroup) widget).getContainedWidgets(includeHidden));
        }

        return containedWidgets;
    }

    @Override
    protected void onPositionUpdate() {
        Position selfPosition = getPosition();
        for (Widget widget : widgets) {
            widget.setParentPosition(selfPosition);
        }
        recomputeSize();
    }

    protected boolean recomputeSize() {
        if (isDynamicSized) {
            Size currentSize = getSize();
            Size dynamicSize = computeDynamicSize();
            if (!currentSize.equals(dynamicSize)) {
                setSize(dynamicSize);
                if (uiAccess != null)
                    uiAccess.notifySizeChange();
                return true;
            }
        }
        return false;
    }

    protected Size computeDynamicSize() {
        Position selfPosition = getPosition();
        Size currentSize = getSize();
        for (Widget widget : widgets) {
            Position size = widget.getPosition().add(widget.getSize()).subtract(selfPosition);
            if (size.x > currentSize.width) {
                currentSize = new Size(size.x, currentSize.height);
            }
            if (size.y > currentSize.height) {
                currentSize = new Size(currentSize.width, size.y);
            }
        }
        return currentSize;
    }

    public void setVisible(boolean visible) {
        if (this.isVisible() == visible) {
            return;
        }
        super.setVisible(visible);
    }

    public WidgetGroup addWidget(Widget widget) {
        if (widget == this) {
            throw new IllegalArgumentException("Cannot add self");
        }
        if (widgets.contains(widget)) {
            throw new IllegalArgumentException("Already added");
        }
        this.widgets.add(widget);
        widget.setUiAccess(groupUIAccess);
        widget.setGui(gui);
        widget.setParentPosition(getPosition());
        if (initialized) {
            widget.initWidget();
        }
        recomputeSize();
        if (uiAccess != null && !isClientSideWidget) {
            uiAccess.notifyWidgetChange();
        }
        return this;
    }

    protected WidgetGroup addWidget(int index, Widget widget) {
        if (widget == this) {
            throw new IllegalArgumentException("Cannot add self");
        }
        if (widgets.contains(widget)) {
            throw new IllegalArgumentException("Already added");
        }
        this.widgets.add(index, widget);
        widget.setUiAccess(groupUIAccess);
        widget.setGui(gui);
        widget.setParentPosition(getPosition());
        if (initialized) {
            widget.initWidget();
        }
        recomputeSize();
        if (uiAccess != null) {
            uiAccess.notifyWidgetChange();
        }
        return this;
    }

    protected void waitToRemoved(Widget widget) {
        if (waitToRemoved == null) {
            waitToRemoved = new ArrayList<>();
        }
        waitToRemoved.add(widget);
    }

    protected void removeWidget(Widget widget) {
        if (!widgets.contains(widget)) {
            return;
        }
        this.widgets.remove(widget);
        widget.setUiAccess(null);
        widget.setGui(null);
        widget.setParentPosition(Position.ORIGIN);
        recomputeSize();
        if (uiAccess != null && !isClientSideWidget) {
            this.uiAccess.notifyWidgetChange();
        }
    }

    protected void clearAllWidgets() {
        this.widgets.forEach(it -> {
            it.setUiAccess(null);
            it.setGui(null);
            it.setParentPosition(Position.ORIGIN);
        });
        this.widgets.clear();
        recomputeSize();
        if (uiAccess != null) {
            this.uiAccess.notifyWidgetChange();
        }
    }

    public boolean isWidgetClickable(Widget widget) {
        return isVisible();
    }

    @Override
    public void initWidget() {
        this.initialized = true;
        for (Widget widget : widgets) {
            widget.setGui(gui);
            widget.initWidget();
        }
    }

    @Override
    public List<SlotWidget> getNativeWidgets() {
        ArrayList<SlotWidget> nativeWidgets = new ArrayList<>();
        for (Widget widget : widgets) {
            nativeWidgets.addAll(widget.getNativeWidgets());
        }
        return nativeWidgets;
    }

    @Override
    public List<Target<?>> getPhantomTargets(Object ingredient) {
        if (!isVisible()) {
            return Collections.emptyList();
        }
        ArrayList<Target<?>> targets = new ArrayList<>();
        for (Widget widget : widgets) {
            if (widget.isVisible() && widget instanceof IGhostIngredientTarget) {
                targets.addAll(((IGhostIngredientTarget) widget).getPhantomTargets(ingredient));
            }
        }
        return targets;
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        if (!isVisible()) {
            return Collections.emptyList();
        }
        for (Widget widget : widgets) {
            if (widget.isVisible() && widget instanceof IIngredientSlot) {
                IIngredientSlot ingredientSlot = (IIngredientSlot) widget;
                Object result = ingredientSlot.getIngredientOverMouse(mouseX, mouseY);
                if (result != null) return result;
            }
        }
        return null;
    }

    @Override
    public void detectAndSendChanges() {
        for (Widget widget : widgets) {
            if (widget.isActive()) {
                widget.detectAndSendChanges();
            }
        }
        if (waitToRemoved != null) {
            waitToRemoved.forEach(this::removeWidget);
            waitToRemoved = null;
        }
    }

    @Override
    public void updateScreen() {
        for (Widget widget : widgets) {
            if (widget.isActive()) {
                widget.updateScreen();
            }
        }
        if (waitToRemoved != null) {
            waitToRemoved.forEach(this::removeWidget);
            waitToRemoved = null;
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY, float partialTicks) {
        super.drawInForeground(mouseX, mouseY, partialTicks);
        for (Widget widget : widgets) {
            if (widget.isVisible()) {
                widget.drawInForeground(mouseX, mouseY, partialTicks);
                GlStateManager.color(1, 1, 1, 1);
            }
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        for (Widget widget : widgets) {
            if (widget.isVisible()) {
                widget.drawInBackground(mouseX, mouseY, partialTicks);
                GlStateManager.color(1, 1, 1, 1);
            }
        }
    }

    @Override
    public Widget mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);
            if(widget.isVisible() && widget.isActive() && (widget = widget.mouseWheelMove(mouseX, mouseY, wheelDelta)) != null) {
                return widget;
            }
        }
        return null;
    }

    @Override
    public Widget mouseClicked(int mouseX, int mouseY, int button) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);
            if(widget.isVisible() && widget.isActive() && (widget = widget.mouseClicked(mouseX, mouseY, button)) != null) {
                return widget;
            }
        }
        return null;
    }

    @Override
    public Widget mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);
            if(widget.isVisible() && widget.isActive() && (widget = widget.mouseDragged(mouseX, mouseY, button, timeDragged)) != null) {
                return widget;
            }
        }
        return null;
    }

    @Override
    public Widget mouseReleased(int mouseX, int mouseY, int button) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);
            if(widget.isVisible() && widget.isActive() && (widget = widget.mouseReleased(mouseX, mouseY, button)) != null) {
                return widget;
            }
        }
        return null;
    }

    @Override
    public Widget keyTyped(char charTyped, int keyCode) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);
            if(widget.isVisible() && widget.isActive() && (widget = widget.keyTyped(charTyped, keyCode)) != null) {
                return widget;
            }
        }
        return null;
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 1) {
            int widgetIndex = buffer.readVarInt();
            int widgetUpdateId = buffer.readVarInt();
            Widget widget = widgets.get(widgetIndex);
            widget.readUpdateInfo(widgetUpdateId, buffer);
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            int widgetIndex = buffer.readVarInt();
            int widgetUpdateId = buffer.readVarInt();
            if (widgetIndex < widgets.size()) {
                Widget widget = widgets.get(widgetIndex);
                widget.handleClientAction(widgetUpdateId, buffer);
            } else {
                Multiblocked.LOGGER.error("xxx");
            }
        }
    }

    private class WidgetGroupUIAccess implements WidgetUIAccess {

        @Override
        public void notifySizeChange() {
            WidgetUIAccess uiAccess = WidgetGroup.this.uiAccess;
            recomputeSize();
            if (uiAccess != null) {
                uiAccess.notifySizeChange();
            }
        }

        @Override
        public boolean attemptMergeStack(ItemStack itemStack, boolean fromContainer, boolean simulate) {
            WidgetUIAccess uiAccess = WidgetGroup.this.uiAccess;
            if (uiAccess != null) {
                return uiAccess.attemptMergeStack(itemStack, fromContainer, simulate);
            }
            return false;
        }

        @Override
        public void sendSlotUpdate(SlotWidget slot) {
            WidgetUIAccess uiAccess = WidgetGroup.this.uiAccess;
            if (uiAccess != null) {
                uiAccess.sendSlotUpdate(slot);
            }
        }

        @Override
        public void sendHeldItemUpdate() {
            WidgetUIAccess uiAccess = WidgetGroup.this.uiAccess;
            if (uiAccess != null) {
                uiAccess.sendHeldItemUpdate();
            }
        }

        @Override
        public void notifyWidgetChange() {
            WidgetUIAccess uiAccess = WidgetGroup.this.uiAccess;
            if (uiAccess != null) {
                uiAccess.notifyWidgetChange();
            }
            recomputeSize();
        }

        @Override
        public void writeClientAction(Widget widget, int updateId, Consumer<PacketBuffer> dataWriter) {
            WidgetGroup.this.writeClientAction(1, buffer -> {
                buffer.writeVarInt(widgets.indexOf(widget));
                buffer.writeVarInt(updateId);
                dataWriter.accept(buffer);
            });
        }

        @Override
        public void writeUpdateInfo(Widget widget, int updateId, Consumer<PacketBuffer> dataWriter) {
            WidgetGroup.this.writeUpdateInfo(1, buffer -> {
                buffer.writeVarInt(widgets.indexOf(widget));
                buffer.writeVarInt(updateId);
                dataWriter.accept(buffer);
            });
        }

    }
}
