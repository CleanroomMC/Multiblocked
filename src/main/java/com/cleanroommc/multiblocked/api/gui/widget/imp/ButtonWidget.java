package com.cleanroommc.multiblocked.api.gui.widget.imp;


import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import com.cleanroommc.multiblocked.util.Position;
import com.cleanroommc.multiblocked.util.Size;
import com.cleanroommc.multiblocked.api.gui.util.ClickData;
import com.cleanroommc.multiblocked.api.gui.widget.Widget;
import net.minecraft.network.PacketBuffer;
import org.apache.commons.lang3.ArrayUtils;

import java.util.function.Consumer;

public class ButtonWidget extends Widget {

    protected IGuiTexture[] buttonTexture;
    protected IGuiTexture[] hoverTexture;
    protected Consumer<ClickData> onPressCallback;

    public ButtonWidget(int xPosition, int yPosition, int width, int height, IGuiTexture buttonTexture, Consumer<ClickData> onPressed) {
        super(xPosition, yPosition, width, height);
        this.onPressCallback = onPressed;
        this.buttonTexture = buttonTexture == null ? null : new IGuiTexture[]{buttonTexture};
    }

    public ButtonWidget(int xPosition, int yPosition, int width, int height, Consumer<ClickData> onPressed) {
        super(xPosition, yPosition, width, height);
        this.onPressCallback = onPressed;
    }

    public ButtonWidget setOnPressCallback(Consumer<ClickData> onPressCallback) {
        this.onPressCallback = onPressCallback;
        return this;
    }

    public ButtonWidget setButtonTexture(IGuiTexture... buttonTexture) {
        this.buttonTexture = buttonTexture;
        return this;
    }

    public ButtonWidget setHoverTexture(IGuiTexture... hoverTexture) {
        this.hoverTexture = hoverTexture;
        return this;
    }

    public ButtonWidget setHoverBorderTexture(int border, int color) {
        this.hoverTexture = ArrayUtils.add(buttonTexture, new ColorBorderTexture(border, color));
        return this;
    }

    @Override
    public void updateScreen() {
        if (buttonTexture != null) {
            for (IGuiTexture texture : buttonTexture) {
                texture.updateTick();
            }
        }
        if (hoverTexture != null) {
            for (IGuiTexture texture : hoverTexture) {
                texture.updateTick();
            }
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        Position position = getPosition();
        Size size = getSize();
        if (isMouseOverElement(mouseX, mouseY) && hoverTexture != null) {
            for (IGuiTexture texture : hoverTexture) {
                texture.draw(mouseX, mouseY, position.x, position.y, size.width, size.height);
            }
        } else if (buttonTexture != null) {
            for (IGuiTexture texture : buttonTexture) {
                texture.draw(mouseX, mouseY, position.x, position.y, size.width, size.height);
            }
        }
    }

    @Override
    public Widget mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            ClickData clickData = new ClickData();
            writeClientAction(1, clickData::writeToBuf);
            if (onPressCallback != null) {
                onPressCallback.accept(clickData);
            }
            playButtonClickSound();
            return this;
        }
        return null;
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            ClickData clickData = ClickData.readFromBuf(buffer);
            if (onPressCallback != null) {
                onPressCallback.accept(clickData);
            }
        }
    }
}
