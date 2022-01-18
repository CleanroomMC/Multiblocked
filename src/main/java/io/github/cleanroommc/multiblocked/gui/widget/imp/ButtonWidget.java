package io.github.cleanroommc.multiblocked.gui.widget.imp;


import io.github.cleanroommc.multiblocked.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.gui.util.ClickData;
import io.github.cleanroommc.multiblocked.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.util.Position;
import io.github.cleanroommc.multiblocked.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;

import java.util.function.Consumer;


public class ButtonWidget extends Widget {

    protected IGuiTexture buttonTexture;
    protected final Consumer<ClickData> onPressCallback;

    public ButtonWidget(int xPosition, int yPosition, int width, int height, IGuiTexture buttonTexture,Consumer<ClickData> onPressed) {
        super(xPosition, yPosition, width, height);
        this.onPressCallback = onPressed;
    }

    public ButtonWidget setButtonTexture(IGuiTexture buttonTexture) {
        this.buttonTexture = buttonTexture;
        return this;
    }

    @Override
    public void updateScreen() {
        if (buttonTexture != null) {
            buttonTexture.updateTick();
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        Position position = getPosition();
        Size size = getSize();
        if (buttonTexture != null) {
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
