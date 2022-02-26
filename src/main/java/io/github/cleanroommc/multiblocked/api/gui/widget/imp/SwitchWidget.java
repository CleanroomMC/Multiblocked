package io.github.cleanroommc.multiblocked.api.gui.widget.imp;


import io.github.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.api.gui.util.ClickData;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.util.Position;
import io.github.cleanroommc.multiblocked.util.Size;
import net.minecraft.network.PacketBuffer;

import java.util.function.BiConsumer;

public class SwitchWidget extends Widget {

    protected IGuiTexture baseTexture;
    protected IGuiTexture pressedTexture;
    protected IGuiTexture hoverTexture;
    protected boolean isPressed;
    protected BiConsumer<ClickData, Boolean> onPressCallback;

    public SwitchWidget(int xPosition, int yPosition, int width, int height, BiConsumer<ClickData, Boolean> onPressed) {
        super(xPosition, yPosition, width, height);
        this.onPressCallback = onPressed;
    }

    public void setOnPressCallback(BiConsumer<ClickData, Boolean> onPressCallback) {
        this.onPressCallback = onPressCallback;
    }

    public SwitchWidget setTexture(IGuiTexture baseTexture, IGuiTexture pressedTexture) {
        this.baseTexture = baseTexture;
        this.pressedTexture = pressedTexture;
        return this;
    }

    public SwitchWidget setHoverTexture(IGuiTexture hoverTexture) {
        this.hoverTexture = hoverTexture;
        return this;
    }

    @Override
    public void updateScreen() {
        if (baseTexture != null) {
            baseTexture.updateTick();
        }
        if (pressedTexture != null) {
            pressedTexture.updateTick();
        }
        if (hoverTexture != null) {
            hoverTexture.updateTick();
        }
    }

    public boolean isPressed() {
        return isPressed;
    }

    public SwitchWidget setPressed(boolean isPressed) {
        if (this.isPressed == isPressed) return this;
        this.isPressed = isPressed;
        if (gui == null) return this;
        if (isRemote()) {
            writeClientAction(2, buffer -> buffer.writeBoolean(isPressed));
        } else {
            writeUpdateInfo(2, buffer -> buffer.writeBoolean(isPressed));
        }
        return this;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        Position position = getPosition();
        Size size = getSize();
        if (isMouseOverElement(mouseX, mouseY) && hoverTexture != null) {
            hoverTexture.draw(mouseX, mouseY, position.x, position.y, size.width, size.height);
        } else if (baseTexture != null && !isPressed) {
            baseTexture.draw(mouseX, mouseY, position.x, position.y, size.width, size.height);
        } else if (pressedTexture != null && isPressed) {
            pressedTexture.draw(mouseX, mouseY, position.x, position.y, size.width, size.height);
        }
    }

    @Override
    public Widget mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            ClickData clickData = new ClickData();
            isPressed = !isPressed;
            writeClientAction(1, buffer -> {
                clickData.writeToBuf(buffer);
                buffer.writeBoolean(isPressed);
            });
            if (onPressCallback != null) {
                onPressCallback.accept(clickData, isPressed);
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
            if (onPressCallback != null) {
                onPressCallback.accept(ClickData.readFromBuf(buffer), isPressed = buffer.readBoolean());
            }
        } else if (id == 2) {
            isPressed = buffer.readBoolean();
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 2) {
            isPressed= buffer.readBoolean();
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }
}
