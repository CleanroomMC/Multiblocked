package io.github.cleanroommc.multiblocked.api.gui.modular;

import com.google.common.collect.ImmutableBiMap;
import io.github.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import io.github.cleanroommc.multiblocked.util.Position;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public final class ModularUI {

    public final ImmutableBiMap<Integer, Widget> guiWidgets;

    public final IGuiTexture backgroundPath;
    private int screenWidth, screenHeight;
    private int width, height;
    @SideOnly(Side.CLIENT)
    private ModularUIGuiContainer guiContainer;
    private ModularUIContainer container;

    /**
     * UIHolder of this modular UI
     */
    public final IUIHolder holder;
    public final EntityPlayer entityPlayer;

    public ModularUI(ImmutableBiMap<Integer, Widget> guiWidgets, IGuiTexture backgroundPath, int width, int height, IUIHolder holder, EntityPlayer entityPlayer) {
        this.guiWidgets = guiWidgets;
        this.backgroundPath = backgroundPath == null ? IGuiTexture.EMPTY : backgroundPath;
        this.width = width;
        this.height = height;
        this.holder = holder;
        this.entityPlayer = entityPlayer;
    }

    public ModularUIContainer getModularUIContainer() {
        return container;
    }

    public void setModularUIContainer(ModularUIContainer container) {
        this.container = container;
    }

    @SideOnly(Side.CLIENT)
    public ModularUIGuiContainer getModularUIGui() {
        return guiContainer;
    }

    @SideOnly(Side.CLIENT)
    public void setModularUIGui(ModularUIGuiContainer modularUIGuiContainer) {
        this.guiContainer = modularUIGuiContainer;
        if (guiContainer.inventorySlots instanceof ModularUIContainer) {
            setModularUIContainer((ModularUIContainer) guiContainer.inventorySlots);
        }
    }

    public List<Widget> getFlatVisibleWidgetCollection() {
        List<Widget> widgetList = new ArrayList<>(guiWidgets.size());

        for (Widget widget : guiWidgets.values()) {
            if (!widget.isVisible()) continue;
            widgetList.add(widget);

            if (widget instanceof WidgetGroup)
                widgetList.addAll(((WidgetGroup) widget).getContainedWidgets(false));
        }

        return widgetList;
    }

    @SideOnly(Side.CLIENT)
    public void setSize(int width, int height) {
        if (this.width != width || this.height != height) {
            this.width = width;
            this.height = height;
            getModularUIGui().initGui();
        }
    }
    
    public void updateScreenSize(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        Position displayOffset = new Position(getGuiLeft(), getGuiTop());
        guiWidgets.values().forEach(widget -> widget.setParentPosition(displayOffset));
    }

    public void initWidgets() {
        guiWidgets.values().forEach(widget -> {
            widget.setGui(this);
            widget.initWidget();
        });
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getGuiLeft() {
        return (getScreenWidth() - getWidth()) / 2;
    }

    public int getGuiTop() {
        return (getScreenHeight() - getHeight()) / 2;
    }

    public Rectangle toScreenCoords(Rectangle widgetRect) {
        return new Rectangle(getGuiLeft() + widgetRect.x, getGuiTop() + widgetRect.y, widgetRect.width, widgetRect.height);
    }

}
