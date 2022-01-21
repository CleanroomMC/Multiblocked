package io.github.cleanroommc.multiblocked.jei;

import io.github.cleanroommc.multiblocked.gui.modular.ModularUI;
import io.github.cleanroommc.multiblocked.gui.modular.ModularUIGuiContainer;
import io.github.cleanroommc.multiblocked.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.gui.widget.imp.SlotWidget;
import io.github.cleanroommc.multiblocked.jei.multipage.MultiblockInfoRecipeWrapper;
import io.github.cleanroommc.multiblocked.util.Position;
import mezz.jei.gui.recipes.RecipeLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class JEIModularUIGuiContainer extends ModularUIGuiContainer {
    private static int lastTick;
    private RecipeLayout layout;

    public JEIModularUIGuiContainer(ModularUI modularUI) {
        super(modularUI);
        this.mc = Minecraft.getMinecraft();
        this.itemRender = mc.getRenderItem();
        this.fontRenderer = mc.fontRenderer;
    }

    public void setRecipeLayout(RecipeLayout layout) {
        modularUI.initWidgets();
        this.layout = layout;
        ScaledResolution resolution = new ScaledResolution(mc);
        this.width = resolution.getScaledWidth();
        this.height = resolution.getScaledHeight();
        modularUI.updateScreenSize(this.width, this.height);
        Position displayOffset = new Position(modularUI.getGuiLeft(), layout.getPosY());
        modularUI.guiWidgets.values().forEach(widget -> widget.setParentPosition(displayOffset));
        for (Widget widget : modularUI.getFlatVisibleWidgetCollection()) {
            if (widget instanceof SlotWidget) {
                this.inventorySlots.inventorySlots.add(((SlotWidget) widget).getHandle());
            }
        }
    }

    public void drawInfo(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
        if (minecraft.player.ticksExisted != lastTick) {
            updateScreen();
            lastTick = minecraft.player.ticksExisted;
        }
        GlStateManager.translate(-layout.getPosX(), -layout.getPosY(),0);
        drawScreen(mouseX + layout.getPosX(), mouseY + layout.getPosY(), minecraft.getRenderPartialTicks());
        GlStateManager.translate(layout.getPosX(), layout.getPosY(),0);
    }

    @Override
    public void updateScreen() {
        modularUI.guiWidgets.values().forEach(Widget::updateScreen);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.hoveredSlot = null;
        drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        for (int i = 0; i < this.inventorySlots.inventorySlots.size(); ++i) {
            Slot slot = this.inventorySlots.inventorySlots.get(i);
            if (slot instanceof SlotWidget.ISlotWidget) {
                if (((SlotWidget.ISlotWidget) slot).isHover()) {
                    setHoveredSlot(slot);
                    ItemStack stack = slot.getStack();
                    if (!stack.isEmpty()) {
                        MultiblockInfoRecipeWrapper.setTooltipBlockStack(stack);
                    }
                }
            }
        }
        drawGuiContainerForegroundLayer(partialTicks, mouseX, mouseY);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    public void superMouseClicked(int mouseX, int mouseY, int mouseButton) {
    }

    @Override
    public void superMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
    }

    @Override
    public void superMouseReleased(int mouseX, int mouseY, int state) {
    }
}
