package io.github.cleanroommc.multiblocked.api.gui.widget.imp;

import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import net.minecraft.client.renderer.GlStateManager;

public class DialogWidget extends WidgetGroup {
    public final WidgetGroup parent;
    protected Runnable onClosed;

    public DialogWidget(WidgetGroup parent, boolean isClient) {
        super(0, 0, parent.getSize().width, parent.getSize().height);
        this.parent = parent;
        if (isClient) setClientSideWidget();
        parent.addWidget(this);
    }

    public DialogWidget setOnClosed(Runnable onClosed) {
        this.onClosed = onClosed;
        return this;
    }

    public void close() {
        parent.waitToRemoved(this);
        if (onClosed != null) {
            onClosed.run();
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.disableDepth();
        GlStateManager.translate(0, 0, 200);
        super.drawInBackground(mouseX, mouseY, partialTicks);
        GlStateManager.translate(0, 0, -200);
        GlStateManager.enableDepth();
    }

    @Override
    public Widget mouseClicked(int mouseX, int mouseY, int button) {
        Widget widget = super.mouseClicked(mouseX, mouseY, button);
        return widget == null ? this : widget;
    }

    @Override
    public Widget keyTyped(char charTyped, int keyCode) {
        if (keyCode == 1) {
            close();
        }
        Widget widget = super.keyTyped(charTyped, keyCode);
        return widget == null ? this : widget;
    }
}
