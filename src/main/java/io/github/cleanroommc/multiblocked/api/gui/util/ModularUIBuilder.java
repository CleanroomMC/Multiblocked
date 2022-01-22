package io.github.cleanroommc.multiblocked.api.gui.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableBiMap;
import io.github.cleanroommc.multiblocked.api.gui.modular.IUIHolder;
import io.github.cleanroommc.multiblocked.api.gui.modular.ModularUI;
import io.github.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.SlotWidget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.function.Supplier;

public class ModularUIBuilder {

    private final ImmutableBiMap.Builder<Integer, Widget> widgets = ImmutableBiMap.builder();
    private final IGuiTexture background;
    private final int width;
    private final int height;
    private int nextFreeWidgetId = 0;

    public ModularUIBuilder(IGuiTexture background, int width, int height) {
        Preconditions.checkNotNull(background);
        this.background = background;
        this.width = width;
        this.height = height;
    }

    public ModularUIBuilder widget(Widget widget) {
        Preconditions.checkNotNull(widget);
        widgets.put(nextFreeWidgetId++, widget);
        return this;
    }

    public ModularUIBuilder image(int x, int y, int width, int height, IGuiTexture area) {
        return widget(new ImageWidget(x, y, width, height, area));
    }

    public ModularUIBuilder label(int x, int y, Supplier<String> text, int color) {
        return widget(new LabelWidget(x, y, text).setTextColor(color));
    }

    public ModularUIBuilder label(int x, int y, String text, int color) {
        return widget(new LabelWidget(x, y, ()->text).setTextColor(color));
    }

    public ModularUIBuilder slot(IItemHandlerModifiable itemHandler, int slotIndex, int x, int y, IGuiTexture... overlays) {
        return widget(new SlotWidget(itemHandler, slotIndex, x, y).setBackgroundTexture(overlays));
    }

    public ModularUIBuilder slot(IItemHandlerModifiable itemHandler, int slotIndex, int x, int y, boolean canTakeItems, boolean canPutItems, IGuiTexture... overlays) {
        return widget(new SlotWidget(itemHandler, slotIndex, x, y, canTakeItems, canPutItems).setBackgroundTexture(overlays));
    }

    public ModularUIBuilder bindPlayerInventory(
            InventoryPlayer inventoryPlayer, IGuiTexture imageLocation, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.widget(new SlotWidget(inventoryPlayer, col + (row + 1) * 9, x + col * 18, y + row * 18)
                        .setBackgroundTexture(imageLocation)
                        .setLocationInfo(true, false));
            }
        }
        return bindPlayerHotbar(inventoryPlayer, imageLocation, x, y + 58);
    }

    public ModularUIBuilder bindPlayerHotbar(InventoryPlayer inventoryPlayer, IGuiTexture imageLocation, int x, int y) {
        for (int slot = 0; slot < 9; slot++) {
            this.widget(new SlotWidget(inventoryPlayer, slot, x + slot * 18, y)
                    .setBackgroundTexture(imageLocation)
                    .setLocationInfo(true, true));
        }
        return this;
    }

    public ModularUI build(IUIHolder holder, EntityPlayer player) {
        return new ModularUI(widgets.build(), background, width, height, holder, player);
    }
}
