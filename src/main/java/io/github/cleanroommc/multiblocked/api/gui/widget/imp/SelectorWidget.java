package io.github.cleanroommc.multiblocked.api.gui.widget.imp;

import io.github.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import io.github.cleanroommc.multiblocked.api.gui.util.DrawerHelper;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import io.github.cleanroommc.multiblocked.util.Position;
import io.github.cleanroommc.multiblocked.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;

import java.util.List;
import java.util.function.Consumer;

public class SelectorWidget extends WidgetGroup {
    protected ButtonWidget button;
    protected List<String> candidates;
    protected boolean isShow;
    private IGuiTexture background;
    private Consumer<String> onChanged;
    private boolean isUp;
    public final TextTexture textTexture;
    public final int fontColor;

    public SelectorWidget(int x, int y, int width, int height, List<String> candidates, int fontColor) {
        super(new Position(x, y), new Size(width, height));
        this.button = new ButtonWidget(0,0,width, height, d -> isShow = !isShow);
        this.candidates = candidates;
        this.fontColor = fontColor;
        this.addWidget(button);
        this.addWidget(new ImageWidget(0,0,width, height, textTexture = new TextTexture("", fontColor).setWidth(width).setType(TextTexture.TextType.ROLL)));
    }

    public SelectorWidget setIsUp(boolean isUp) {
        this.isUp = isUp;
        return this;
    }

    public SelectorWidget setValue(String value) {
        textTexture.updateText(value);
        return this;
    }

    public String getValue() {
        return textTexture.text;
    }

    public SelectorWidget setOnChanged(Consumer<String> onChanged) {
        this.onChanged = onChanged;
        return this;
    }

    public SelectorWidget setButtonBackground(IGuiTexture... guiTexture) {
        button.setButtonTexture(guiTexture);
        return this;
    }

    public SelectorWidget setBackground(IGuiTexture background) {
        this.background = background;
        return this;
    }

    @Override
    public void setFocus(boolean focus) {
        super.setFocus(focus);
        if (!focus) {
            isShow = false;
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY, float particleTicks) {
        super.drawInForeground(mouseX, mouseY, particleTicks);
        if(isShow) {
            GlStateManager.disableDepth();
            GlStateManager.translate(0, 0, 200);

            int x = getPosition().x;
            int width = getSize().width;
            int height = getSize().height;
            int y = (isUp ? -candidates.size() : 1) * height + getPosition().y;
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            for (String candidate : candidates) {
                if (background != null) {
                    background.draw(x, y, width, height);
                } else {
                    DrawerHelper.drawSolidRect(x, y, width, height, 0xAA000000);
                }
                fontRenderer.drawString(I18n.format(candidate), x + 4, y + (height - fontRenderer.FONT_HEIGHT) / 2 + 1, fontColor);
                y += height;
            }
            y = (isUp ? -candidates.size() : 1) * height + getPosition().y;
            for (String ignored : candidates) {
                if (isMouseOver(x, y, width, height, mouseX, mouseY)) {
                    DrawerHelper.drawBorder(x, y, width, height, -1, 1);
                }
                y += height;
            }

            GlStateManager.translate(0, 0, -200);
            GlStateManager.enableDepth();
        }
    }

    @Override
    public Widget mouseClicked(int mouseX, int mouseY, int button) {
        if (isShow) {
            int x = getPosition().x;
            int width = getSize().width;
            int height = getSize().height;
            int y = (isUp ? -candidates.size() : 1) * height + getPosition().y;
            for (String candidate : candidates) {
                if (isMouseOver(x, y, width, height, mouseX, mouseY)) {
                    if (onChanged != null) {
                        onChanged.accept(candidate);
                    }
                    setValue(candidate);
                    writeClientAction(2, buffer -> buffer.writeString(candidate));
                    isShow = false;
                    return this;
                }
                y += height;
            }
        }
        isShow = false;
        return super.mouseClicked(mouseX, mouseY, button) == null ? null : this;
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 2) {
            setValue(buffer.readString(Short.MAX_VALUE));
            if (onChanged != null) {
               onChanged.accept(getValue()); 
            }
        }
    }

}