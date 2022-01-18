package io.github.cleanroommc.multiblocked.gui.widget.imp;


import io.github.cleanroommc.multiblocked.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.gui.util.ClickData;
import io.github.cleanroommc.multiblocked.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.util.Position;
import io.github.cleanroommc.multiblocked.util.Size;
import net.minecraft.network.PacketBuffer;

import java.util.function.BiConsumer;

public class SwitchWidget extends Widget {

    protected IGuiTexture baseTexture;
    protected IGuiTexture pressedTexture;
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

    @Override
    public void updateScreen() {
        if (baseTexture != null) {
            baseTexture.updateTick();
        }
        if (pressedTexture != null) {
            pressedTexture.updateTick();
        }
    }

    public boolean isPressed() {
        return isPressed;
    }

    public void setPressed(boolean isPressed) {
        if (this.isPressed == isPressed) return;
        this.isPressed = isPressed;
        if (gui == null) return;
        if (isRemote()) {
            writeClientAction(2, buffer -> buffer.writeBoolean(isPressed));
        } else {
            writeUpdateInfo(2, buffer -> buffer.writeBoolean(isPressed));
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        Position position = getPosition();
        Size size = getSize();
        if (baseTexture != null && !isPressed) {
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
