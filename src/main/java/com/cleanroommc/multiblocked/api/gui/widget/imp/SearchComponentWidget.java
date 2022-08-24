package com.cleanroommc.multiblocked.api.gui.widget.imp;

import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.Widget;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.util.ISearch;
import com.cleanroommc.multiblocked.util.SearchEngine;
import com.cleanroommc.multiblocked.util.Size;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author KilaBash
 * @date 2022/8/24
 * @implNote SearchComponentWidget
 */
public class SearchComponentWidget<T> extends WidgetGroup {
    public final SearchEngine<T> engine;
    public final IWidgetSearch<T> search;
    public final DraggableScrollableWidgetGroup popUp;
    public final TextFieldWidget textFieldWidget;
    private int capacity = 10;
    protected boolean isShow;

    public SearchComponentWidget(int x, int y, int width, int height, IWidgetSearch<T> search) {
        super(x, y, width, height);
        setClientSideWidget();
        this.addWidget(popUp = new DraggableScrollableWidgetGroup(0, height, width, 0));
        this.addWidget(textFieldWidget = new TextFieldWidget(0, 0, width, height, null, null){
            @Override
            public void setFocus(boolean focus) {
                super.setFocus(focus);
                setShow(focus);
            }
        });
        popUp.setBackground(new ColorRectTexture(0x8A000000));
        popUp.setVisible(false);
        popUp.setActive(false);
        this.search = search;
        this.engine = new SearchEngine<>(search, (r) -> {
            int size = popUp.getAllWidgetSize();
            popUp.setSize(new Size(getSize().width, Math.min(size + 1, capacity) * 15));
            popUp.waitToAdded(new ButtonWidget(0, size * 15, width,
                    15, new TextTexture(search.resultDisplay(r)).setWidth(width).setType(TextTexture.TextType.ROLL),
                    cd -> {
                        search.selectResult(r);
                        textFieldWidget.setCurrentString(search.resultDisplay(r));
                    }).setHoverBorderTexture(-1, -1));
        });

        textFieldWidget.setTextResponder(s -> {
            popUp.clearAllWidgets();
            popUp.setSize(new Size(getSize().width, 0));
            this.engine.searchWord(s);
        });
    }

    public SearchComponentWidget<T> setCapacity(int capacity) {
        this.capacity = capacity;
        popUp.setSize(new Size(getSize().width, Math.min(popUp.getAllWidgetSize(), capacity) * 15));
        return this;
    }

    public SearchComponentWidget<T> setCurrentString(String currentString) {
        textFieldWidget.setCurrentString(currentString);
        return this;
    }

    public String getCurrentString() {
        return textFieldWidget.getCurrentString();
    }

    public void setShow(boolean isShow) {
        this.isShow = isShow;
        popUp.setVisible(isShow);
        popUp.setActive(isShow);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        for (Widget widget : widgets) {
            if (widget.isVisible() && widget != popUp) {
                widget.drawInBackground(mouseX, mouseY, partialTicks);
                GlStateManager.color(1, 1, 1, 1);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY, float partialTicks) {
        for (Widget widget : widgets) {
            if (widget.isVisible()) {
                widget.drawInForeground(mouseX, mouseY, partialTicks);
                GlStateManager.color(1, 1, 1, 1);
            }
        }
        if(isShow) {
            GlStateManager.disableDepth();
            GlStateManager.translate(0, 0, 200);

            popUp.drawInBackground(mouseX, mouseY, partialTicks);
            popUp.drawInForeground(mouseX, mouseY, partialTicks);

            GlStateManager.translate(0, 0, -200);
            GlStateManager.enableDepth();
        }
    }

    public interface IWidgetSearch<T> extends ISearch<T> {
        String resultDisplay(T value);

        void selectResult(T value);
    }
}
