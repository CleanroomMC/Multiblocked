package com.cleanroommc.multiblocked.api.gui.widget.imp;

import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.util.Position;
import com.cleanroommc.multiblocked.util.Size;
import com.cleanroommc.multiblocked.api.gui.util.DrawerHelper;
import com.cleanroommc.multiblocked.api.gui.widget.Widget;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SelectorWidget extends WidgetGroup {
    protected ButtonWidget button;
    protected List<String> candidates;
    protected List<SelectableWidgetGroup> selects;
    protected String currentString;
    protected boolean isShow;
    private Consumer<String> onChanged;
    public final TextTexture textTexture;
    public final DraggableScrollableWidgetGroup popUp;

    public SelectorWidget(int x, int y, int width, int height, List<String> candidates, int fontColor) {
        super(new Position(x, y), new Size(width, height));
        this.button = new ButtonWidget(0,0, width, height, d -> setShow(!isShow));
        this.candidates = candidates;
        this.selects = new ArrayList<>();
        this.addWidget(button);
        this.addWidget(new ImageWidget(0,0,width, height, textTexture = new TextTexture("", fontColor).setWidth(width).setType(TextTexture.TextType.ROLL)));
        this.addWidget(popUp = new DraggableScrollableWidgetGroup(0, height, width, Math.min(5, candidates.size()) * 15));
        popUp.setBackground(new ColorRectTexture(0xAA000000));
        if (candidates.size() > 5) {
            popUp.setYScrollBarWidth(4).setYBarStyle(null, new ColorRectTexture(-1));
        }
        currentString = "";
        y = 0;
        width = candidates.size() > 5 ? width -4 : width;
        for (String candidate : candidates) {
            SelectableWidgetGroup select = new SelectableWidgetGroup(0, y, width, 15);
            select.addWidget(new ImageWidget(0, 0, width, 15, new TextTexture(candidate, fontColor).setWidth(width).setType(TextTexture.TextType.ROLL)));
            select.setSelectedTexture(-1, -1);
            select.setOnSelected(s -> {
                setValue(candidate);
                if (onChanged != null) {
                    onChanged.accept(candidate);
                }
                setValue(candidate);
                writeClientAction(2, buffer -> buffer.writeString(candidate));
                setShow(false);
            });
            popUp.addWidget(select);
            selects.add(select);
            y += 15;
        }

    }

    public SelectorWidget setIsUp(boolean isUp) {
        popUp.setSelfPosition(isUp ? new Position(0, - Math.min(candidates.size(), 5) * 15): new Position(0, getSize().height));
        return this;
    }

    public void setShow(boolean isShow) {
        this.isShow = isShow;
        popUp.setVisible(isShow);
        popUp.setActive(isShow);
    }

    public SelectorWidget setValue(String value) {
        int index = candidates.indexOf(value);
        if (index >= 0 && !value.equals(currentString)) {
            currentString = value;
            textTexture.updateText(value);
            for (int i = 0; i < selects.size(); i++) {
                selects.get(i).isSelected = index == i;
            }
        }
        return this;
    }

    public String getValue() {
        return currentString;
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
        popUp.setBackground(background);
        return this;
    }

    @Override
    public void setFocus(boolean focus) {
        super.setFocus(focus);
        if (!focus) {
            setShow(false);
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY, float particleTicks) {
        for (Widget widget : widgets) {
            if (widget.isVisible()) {
                widget.drawInForeground(mouseX, mouseY, particleTicks);
                GlStateManager.color(1, 1, 1, 1);
            }
        }
        if(isShow) {
            GlStateManager.disableDepth();
            GlStateManager.translate(0, 0, 200);

            popUp.drawInBackground(mouseX, mouseY, particleTicks);
            popUp.drawInForeground(mouseX, mouseY, particleTicks);

            GlStateManager.translate(0, 0, -200);
            GlStateManager.enableDepth();
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        for (Widget widget : widgets) {
            if (widget.isVisible() && widget != popUp) {
                widget.drawInBackground(mouseX, mouseY, partialTicks);
                GlStateManager.color(1, 1, 1, 1);
            }
        }
    }

    @Override
    public Widget mouseClicked(int mouseX, int mouseY, int button) {
        Widget widget = super.mouseClicked(mouseX, mouseY, button) == null ? null : this;
        return widget;
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