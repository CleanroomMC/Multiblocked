package com.cleanroommc.multiblocked.api.gui.widget.imp;


import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import com.cleanroommc.multiblocked.util.Position;
import com.cleanroommc.multiblocked.util.Size;
import com.cleanroommc.multiblocked.api.gui.util.ClickData;
import com.cleanroommc.multiblocked.api.gui.widget.Widget;
import net.minecraft.network.PacketBuffer;

import java.util.function.BiConsumer;

public class SwitchWidget extends Widget {

    protected IGuiTexture[] baseTexture;
    protected IGuiTexture[] pressedTexture;
    protected IGuiTexture[] hoverTexture;
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
        setBaseTexture(baseTexture);
        setPressedTexture(pressedTexture);
        return this;
    }

    public SwitchWidget setBaseTexture(IGuiTexture... baseTexture) {
        this.baseTexture = baseTexture;
        return this;
    }

    public SwitchWidget setPressedTexture(IGuiTexture... pressedTexture) {
        this.pressedTexture = pressedTexture;
        return this;
    }

    public SwitchWidget setHoverTexture(IGuiTexture... hoverTexture) {
        this.hoverTexture = hoverTexture;
        return this;
    }

    public SwitchWidget setHoverBorderTexture(int border, int color) {
        this.hoverTexture = new IGuiTexture[]{new ColorBorderTexture(border, color)};
        return this;
    }

    @Override
    public void updateScreen() {
        if (baseTexture != null) {
            for (IGuiTexture texture : baseTexture) {
                texture.updateTick();
            }
        }
        if (pressedTexture != null) {
            for (IGuiTexture texture : pressedTexture) {
                texture.updateTick();
            }
        }
        if (hoverTexture != null) {
            for (IGuiTexture texture : hoverTexture) {
                texture.updateTick();
            }
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
        if (baseTexture != null && !isPressed) {
            for (IGuiTexture texture : baseTexture) {
                texture.draw(mouseX, mouseY, position.x, position.y, size.width, size.height);
            }
        } else if (pressedTexture != null && isPressed) {
            for (IGuiTexture texture : pressedTexture) {
                texture.draw(mouseX, mouseY, position.x, position.y, size.width, size.height);
            }
        }
        if (isMouseOverElement(mouseX, mouseY) && hoverTexture != null) {
            for (IGuiTexture texture : hoverTexture) {
                texture.draw(mouseX, mouseY, position.x, position.y, size.width, size.height);
            }
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
