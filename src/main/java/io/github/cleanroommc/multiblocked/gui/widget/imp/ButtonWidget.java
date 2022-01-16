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
    protected final String displayText;
    protected int textColor = 0xFFFFFF;
    protected final Consumer<ClickData> onPressCallback;

    public ButtonWidget(int xPosition, int yPosition, int width, int height, String displayText, Consumer<ClickData> onPressed) {
        super(xPosition, yPosition, width, height);
        this.displayText = displayText;
        this.onPressCallback = onPressed;
    }

    public ButtonWidget setButtonTexture(IGuiTexture buttonTexture) {
        this.buttonTexture = buttonTexture;
        return this;
    }

    public ButtonWidget setTextColor(int textColor) {
        this.textColor = textColor;
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
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        String text = I18n.format(displayText);
        fontRenderer.drawString(text,
                position.x + size.width / 2 - fontRenderer.getStringWidth(text) / 2,
                position.y + size.height / 2 - fontRenderer.FONT_HEIGHT / 2, textColor);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            ClickData clickData = new ClickData();
            writeClientAction(1, clickData::writeToBuf);
            onPressCallback.accept(clickData);
            playButtonClickSound();
            return true;
        }
        return false;
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
