package io.github.cleanroommc.multiblocked.api.gui.widget.imp;


import io.github.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.api.gui.util.ClickData;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.util.Position;
import io.github.cleanroommc.multiblocked.util.Size;
import net.minecraft.network.PacketBuffer;

import java.util.function.Consumer;

public class ButtonWidget extends Widget {

    protected IGuiTexture buttonTexture;
    protected IGuiTexture hoverTexture;
    protected final Consumer<ClickData> onPressCallback;

    public ButtonWidget(int xPosition, int yPosition, int width, int height, IGuiTexture buttonTexture, Consumer<ClickData> onPressed) {
        super(xPosition, yPosition, width, height);
        this.onPressCallback = onPressed;
    }

    public ButtonWidget setButtonTexture(IGuiTexture buttonTexture) {
        this.buttonTexture = buttonTexture;
        return this;
    }

    public ButtonWidget setHoverTexture(IGuiTexture hoverTexture) {
        this.hoverTexture = hoverTexture;
        return this;
    }

    @Override
    public void updateScreen() {
        if (buttonTexture != null) {
            buttonTexture.updateTick();
        }
        if (hoverTexture != null) {
            hoverTexture.updateTick();
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        Position position = getPosition();
        Size size = getSize();
        if (isMouseOverElement(mouseX, mouseY) && hoverTexture != null) {
            hoverTexture.draw(mouseX, mouseY, position.x, position.y, size.width, size.height);
        } else if (buttonTexture != null) {
            buttonTexture.draw(mouseX, mouseY, position.x, position.y, size.width, size.height);
        }
    }

    @Override
    public Widget mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            ClickData clickData = new ClickData();
            writeClientAction(1, clickData::writeToBuf);
            onPressCallback.accept(clickData);
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
            onPressCallback.accept(clickData);
        }
    }
}
